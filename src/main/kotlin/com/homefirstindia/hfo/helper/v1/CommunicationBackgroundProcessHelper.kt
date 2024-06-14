package com.homefirstindia.hfo.helper.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.homefirstindia.hfo.clients.AmazonClient
import com.homefirstindia.hfo.dto.v1.CallLogExport
import com.homefirstindia.hfo.dto.v1.MFile
import com.homefirstindia.hfo.model.v1.*
import com.homefirstindia.hfo.networking.v1.CommonNetworkingClient
import com.homefirstindia.hfo.repository.v1.CommunicationRepository
import com.homefirstindia.hfo.security.AppProperty
import com.homefirstindia.hfo.service.v1.CommunicationService
import com.homefirstindia.hfo.utils.*
import com.opencsv.CSVWriter
import com.opencsv.bean.StatefulBeanToCsvBuilder
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileWriter
import javax.transaction.Transactional

@Service
@EnableAsync
class CommunicationBackgroundProcessHelper(
    @Autowired private val communicationHelper: CommunicationHelper,
    @Autowired private val communicationRepository: CommunicationRepository,
    @Autowired private val commonNetworkingClient: CommonNetworkingClient,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val appProperty: AppProperty,
    @Autowired private val cryptoUtils: CryptoUtils,
    @Autowired private val amazonClient: AmazonClient,
    @Autowired val mailHelper: MailHelper,
    @Autowired private val oneResponse: OneResponse
) {

    private fun printLog(value: String) = LoggerUtils.printLog("v1/${this.javaClass.simpleName}.$value")

    private fun log(value: String) = LoggerUtils.log("v1/${this.javaClass.simpleName}.$value")

    @Transactional
    @Async(THREAD_POOL_TASK_EXECUTOR)
    fun sendBulkSMS(bulkSMSRequest: SMSLog, approvedTemplate: SMSTemplate) {
        try {

            bulkSMSRequest.jsonMessage!!.chunked(CommunicationService.TOP_LEVEL_BATCH_SIZE).forEach {
                val validSMS = ArrayList<SMSBody>()
                it.forEach { sms ->
                    sms.mandatoryFieldsCheck().let {
                        if (!it.isSuccess) {
                            return@let
                        }
                        validSMS.add(sms)
                    }
                }
                log("sendBulkSMS :- valid sms count ${validSMS.size} ")
                if (validSMS.isNotEmpty())
                    processBulkSMSSend(validSMS, approvedTemplate, bulkSMSRequest)

            }

        } catch (e: Exception) {

            log("sendBulkSMS - Error while sending bulk sms: ${e.message}")
            e.printStackTrace()

            if (appProperty.isProduction()) {

                mailHelper.sendMimeMessage(
                    arrayOf("anuj.bhelkar@homefirstindia.com"),
                    "Send Bulk SMS Failed",
                    "Error while sending bulk sms --> ${e.message}\n\n${e.stackTraceToString()}",
                    false,
                    null,
//                    arrayOf("ayush.maurya@homefirstindia.com") TODO: uncommecnt brfore uploading to prod
                )

            }

        }

    }

    @Async(THREAD_POOL_TASK_EXECUTOR)
    fun processBulkSMSSend(
        smses: MutableList<SMSBody>,
        approvedTemplate: SMSTemplate,
        smsRequest: SMSLog,
    ) {

        if (appProperty.isStrictProduction) {

            val sharedBulkSMSResponse = communicationHelper.sendBulkSMS(smses, approvedTemplate).let {
                if (!it.isSuccess) {
                    log("comSendBulkSMS - ${it.message}")
                    storeSMSLogs(smsRequest, smses, JSONArray(smses), it.isSuccess)
                    return
                }

                it

            }

            val kaleyraResponseJson = JSONObject(sharedBulkSMSResponse.response).getJSONArray("data")
            storeSMSLogs(smsRequest, smses, kaleyraResponseJson, true)

        } else
            storeSMSLogs(smsRequest, smses, JSONArray(smses), false)

    }

    fun storeSMSLogs(
        smsRequest: SMSLog,
        smses: MutableList<SMSBody>,
        jsonResponse: JSONArray,
        success: Boolean,
    ) {
        val entriesToSave = ArrayList<SMSLog>()

        try {
            jsonResponse.forEach { it ->
                val smsStatusObject = JSONObject(it.toString())
                entriesToSave.add(
                    SMSLog().apply {
                        response = if (success) it.toString() else null
                        smsId = if (success) smsStatusObject.optString("id", NA) else null
                        status = if (success) smsStatusObject.optString("status", NA) else FAILURE
                        objectId = if (success) smsStatusObject.optString(
                            "customid1",
                            NA
                        ) else smsStatusObject.optString("custom1", NA)
                        mobileNumber =
                            if (success) smsStatusObject.optString("mobile", NA) else smsStatusObject.optString(
                                "to",
                                NA
                            )
                        message = if (success) smses.find {
                            it.custom1 == smsStatusObject.optString(
                                "customid1",
                                NA
                            )
                        }?.message else smsStatusObject.optString("message", NA)
                        userName = smsRequest.userName
                        userEmail = smsRequest.userEmail
                        userId = smsRequest.userId
                        source = smsRequest.source
                        approvedTemplateId = smsRequest.approvedTemplateId
                        objectName = smsRequest.objectName
                        orgId = smsRequest.orgId
                        smsCampaignId = smsRequest.smsCampaignId
                        updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()

                    }

                )

            }

            communicationRepository.smsLogRepository.saveAllAndFlush(entriesToSave)

        } catch (e: Exception) {
            log("storeSMSLogs - Error while storing bulk sms: ${e.message}")
            e.printStackTrace()
        }


    }

    @Async(THREAD_POOL_TASK_EXECUTOR)
    fun downloadAndUploadFileToS3(call: CallLog) {

        try {

            if (call.attachment == null) {

                val attachment = Attachment().apply {
                    fileName =
                        "${call.caller}_${call.receiver}_${System.currentTimeMillis()}${FileTypesExtentions.MP3.ext}"
                    fileIdentifier =
                        cryptoUtils.encodeBase64("${System.currentTimeMillis()}${Math.random()}")
                    objectId = call.id
                    objectType = MyObject.CALL_LOG.value
                    contentType = FileTypesExtentions.MP3.displayName
                    attachementType = AttachmentType.AUDIO.value
                }

                log("downloadAndUploadFileToS3 - uploading recording file in S3 for call Id: ${call.id}")

                val status = amazonClient.uploadFileToS3(call.recordingUrl!!, attachment.fileName!!)

                if (status) {

                    log(
                        "downloadAndUploadFileToS3 - recording file uploaded successfully " +
                                "in S3 with name: ${attachment.fileName}"
                    )

                    call.attachment = attachment
                    call.status = CallStatus.SUCCESS.value
                    call.updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
                    communicationRepository.callLogRepository.save(call)

                    if (!call.isNotified && isNotNullOrNA(call.callbackUrl))
                        callDisposition(call.id!!)

                } else {
                    log(
                        "downloadAndUploadFileToS3 - recording file fail to upload " +
                                "in S3 with name: ${attachment.fileName}"
                    )
                }

            } else {
                log("downloadAndUploadFileToS3 - Invalid file URL or recording already saved")
            }

        } catch (e: Exception) {

            log("clickToCallDisposition - Failed to Save and upload the Call Details")
            e.printStackTrace()
        }


    }

    @Throws(Exception::class)
    fun callDisposition(id: String) {

        if (id.isInvalid()) {
            log("callDisposition - Invalid id")
            return
        }

        val eCallLog = communicationRepository.callLogRepository.findById(id).get()

        if (eCallLog.callbackUrl!!.isInvalid()) {
            log("callDisposition - callback url is not valid.")
            return
        }

        val callDispositionUrl = StringBuilder()

        callDispositionUrl.append(eCallLog.callbackUrl)
        callDispositionUrl.append("?")
        callDispositionUrl.append("id=")
        callDispositionUrl.append(eCallLog.id)
        callDispositionUrl.append("&callId=")
        callDispositionUrl.append(eCallLog.callId)
        callDispositionUrl.append("&caller=")
        callDispositionUrl.append(eCallLog.caller)
        callDispositionUrl.append("&receiver=")
        callDispositionUrl.append(eCallLog.receiver)
        callDispositionUrl.append("&callerStatus=")
        callDispositionUrl.append(eCallLog.callerStatus)
        callDispositionUrl.append("&receiverStatus=")
        callDispositionUrl.append(eCallLog.receiverStatus)
        callDispositionUrl.append("&callStartTime=")
        callDispositionUrl.append(eCallLog.callStartTime)
        callDispositionUrl.append("&callEndTime=")
        callDispositionUrl.append(eCallLog.callEndTime)
        callDispositionUrl.append("&callerName=")
        callDispositionUrl.append(eCallLog.callerName)
        callDispositionUrl.append("&callerLocation=")
        callDispositionUrl.append(eCallLog.callerLocation)
        callDispositionUrl.append("&callerProvider=")
        callDispositionUrl.append(eCallLog.callerProvider)
        callDispositionUrl.append("&durationInSec=")
        callDispositionUrl.append(eCallLog.durationInSec)
        callDispositionUrl.append("&status=")
        callDispositionUrl.append(eCallLog.status)


        eCallLog.attachment?.let {
            callDispositionUrl.append("&recordingUrl=")
            callDispositionUrl.append("${appProperty.fileIdentifierURL}${it.fileIdentifier}")
        }

        val response = commonNetworkingClient
            .NewRequest()
            .getCall(callDispositionUrl.toString())
            .send()

        log(
            "callDisposition - Call disposition response for ID: ${eCallLog.id} " +
                    "| code: ${response.statusCode} | response: ${response.stringEntity}"
        )

        if (response.statusCode != 200) {
            log("callDisposition - Failed to post data to Call back URL")
            return
        }

        eCallLog.isNotified = true
        eCallLog.updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()

        communicationRepository.callLogRepository.save(eCallLog)

        log("callDisposition - Data posted to call back URL successfully!")
    }

    @Async(THREAD_POOL_TASK_EXECUTOR)
    fun callLogExport(
        startDatetime: String,
        endDatetime: String,
        objectName: String
    ) {

        val callLogs = communicationRepository.callLogRepository.findForExport(startDatetime,
            endDatetime, objectName)

        if (callLogs!!.isEmpty()) {
            val msg = "No call log data to export from $startDatetime to $endDatetime"
            log("callLogExport - $msg")
            return
        }

        val fileName = StringBuilder()
        fileName.append("CollectionCallLog")
        val sDate = DateTimeUtils.getStringFromDateTimeString(startDatetime, DateTimeFormat.yyyy_MM_dd_HH_mm_ss, DateTimeFormat.ddMMM)
        fileName.append("_$sDate")
        val eDate = DateTimeUtils.getStringFromDateTimeString(endDatetime, DateTimeFormat.yyyy_MM_dd_HH_mm_ss, DateTimeFormat.ddMMM)
        fileName.append("_$eDate")
        fileName.append("_${System.currentTimeMillis()}")
        fileName.append(".csv")

        val filePath = "${appProperty.filePath}$fileName"

        val writer = FileWriter(filePath)
        val sbc = StatefulBeanToCsvBuilder<CallLogExport>(writer)
            .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
            .build()

        sbc.write(callLogs)
        writer.flush()
        writer.close()

        val file = File(filePath)

        notifyCollectionCallLogToUser(startDatetime.substring(0, startDatetime.length - 9),
            endDatetime.substring(0, endDatetime.length - 9),
            MFile(fileName.toString(), filePath))
        file.delete()

        log("callLogExport - Call log exported successfully from $startDatetime to $endDatetime")

    }

    fun notifyCollectionCallLogToUser(
        startDate: String,
        endDate: String,
        mFile: MFile
    ) {

        val sb = StringBuilder()
        sb.append("Hi Team,")
        sb.append("\n\nPlease find the below attached file with collection call log data.")
        sb.append("\n\n\nThis is an auto generated email. Please do not reply.")
        sb.append("\n- Homefirst")

        if (!appProperty.isStrictProduction)
            return

        mailHelper.sendMimeMessage(
            if (appProperty.isProduction() || appProperty.isStaging())
                arrayOf("collections@homefirstindia.com")
            else
                arrayOf("sanjay.jaiswar@homefirstindia.com"),
            "Collection call log | $startDate - $endDate",
            sb.toString(),
            false,
            arrayListOf(mFile),
            arrayOf("sanjay.jaiswar@homefirstindia.com",
                "ranan.rodrigues@homefirstindia.com",
                "sanjay.sharma@homfirstindia.com")
        )

        println("notifyCollectionCallLogToUser - Mail sent successfully!")

    }
}