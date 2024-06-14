package com.homefirstindia.hfo.service.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.homefirstindia.hfo.dto.v1.AdvanceFilter
import com.homefirstindia.hfo.dto.v1.externalpartner.EPAuthRequest
import com.homefirstindia.hfo.helper.v1.CommunicationBackgroundProcessHelper
import com.homefirstindia.hfo.helper.v1.CommunicationHelper
import com.homefirstindia.hfo.helper.v1.PartnerLogHelper
import com.homefirstindia.hfo.helper.v1.UserActionStatus
import com.homefirstindia.hfo.model.v1.CallLog
import com.homefirstindia.hfo.model.v1.SMSBody
import com.homefirstindia.hfo.model.v1.SMSLog
import com.homefirstindia.hfo.repository.v1.CallLogRepository
import com.homefirstindia.hfo.repository.v1.CallLogRepositoryMasterRepository
import com.homefirstindia.hfo.repository.v1.CommunicationRepository
import com.homefirstindia.hfo.utils.*
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service


@Service
class CommunicationService(
    @Autowired val oneResponse: OneResponse,
    @Autowired val communicationRepository: CommunicationRepository,
    @Autowired val communicationBackgroundProcessHelper: CommunicationBackgroundProcessHelper,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val partnerLogHelper: PartnerLogHelper,
    @Autowired val communicationHelper: CommunicationHelper,
    @Autowired val callLogRepositoryMasterRepository: CallLogRepositoryMasterRepository,
    private val callLogRepository: CallLogRepository,
) {

    private fun log(value: String) = LoggerUtils.log("v1/${this.javaClass.simpleName}.$value")
    private fun methodName(value: String): String = "v1/${this.javaClass.simpleName}.$value"
    private fun printLog(value: String) = println("v1/${this.javaClass.simpleName}.$value")

    companion object {
        const val TOP_LEVEL_BATCH_SIZE = 3000
        const val BOTTOM_LEVEL_BATCH_SIZE = 1000
        const val DEFAULT_PAGE_SIZE = 25
    }

    @Throws(Exception::class)
    fun comSendBulkSMS(epAuthRequest: EPAuthRequest, bulkSMSRequest: SMSLog): ResponseEntity<String> {

        printLog("comSendBulkSMS - point 1 | smses : ${bulkSMSRequest.jsonMessage?.size}")

        val epLogger = partnerLogHelper.Builder(epAuthRequest)
            .setServiceName(object {}.javaClass.enclosingMethod.name)

        bulkSMSRequest.mandatoryFieldsCheck().let {

            if (!it.isSuccess) {
                val msg = "Invalid data"
                log("comSendBulkSMS - Error : ${it.message}")

                epLogger
                    .setResponseStatus(201)
                    .setRequestStatus(UserActionStatus.FAILURE)
                    .setRequestDesc(msg)
                    .log()
                return oneResponse.getFailureResponse(it.toJson())

            }

        }

        val approvedTemplate =
            communicationRepository.smsTemplateRepository.findById(bulkSMSRequest.approvedTemplateId!!).let {
                if (it.isEmpty) {
                    val msg = "No sms template found."
                    log("comSendBulkSMS - $msg")
                    epLogger
                        .setResponseStatus(201)
                        .setRequestStatus(UserActionStatus.FAILURE)
                        .setRequestDesc(msg)
                        .log()
                    return oneResponse.invalidData(msg)

                }

                it.get()
            }

        bulkSMSRequest.orgId = epAuthRequest.orgId

        communicationBackgroundProcessHelper.sendBulkSMS(bulkSMSRequest, approvedTemplate)

        epLogger
            .setResponseStatus(200)
            .setRequestStatus(UserActionStatus.SUCCESS)
            .log()

        return oneResponse.getSuccessResponse(
            JSONObject().put(MESSAGE, "Send bulk sms process started")
        )

    }

    @Throws(Exception::class)
    fun comSMSDisposition(sms: SMSLog): ResponseEntity<String> {

        if (sms.smsId.isInvalid()) {
            val msg = "Invalid SMS id Found !!"
            return oneResponse.invalidData(msg)
        }

        val eSMS = communicationRepository.smsLogRepository.findBySmsId(sms.smsId!!).let {
            if (null == it) {
                val msg = "No SMS Found !!"
                return oneResponse.getFailureResponse(JSONObject(msg))
            }
            it
        }

        eSMS.apply {
            status = sms.status
            sentDatetime = sms.sentDatetime
            deliveredDatetime = sms.deliveredDatetime
            updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
        }

        communicationRepository.smsLogRepository.save(eSMS)

        return oneResponse.getSuccessResponse(
            JSONObject().put(MESSAGE, "SMS status updated successfully")
        )

    }

    @Throws(Exception::class)
    fun comRetrySendBulkSMS(retryBulkSMSRequest: JSONObject): ResponseEntity<String> {

        val oCampaignId = retryBulkSMSRequest.optString("campaignId")
        val nCampaignId = retryBulkSMSRequest.optString("newCampaignId")

        if (oCampaignId.isInvalid() || nCampaignId.isInvalid()) {
            val msg = "Invalid campaign id and new campaign id."
            log("comRetrySendBulkSMS - $msg")
            return oneResponse.invalidData(msg)
        }

        val failedCampaignList =
            communicationRepository.smsLogRepository.findBySmsCampaignId(oCampaignId, Pageable.unpaged())

        if (failedCampaignList!!.isEmpty) {
            val msg = "No failed sms campaign found."
            log("comRetrySendBulkSMS - $msg")
            return oneResponse.resourceNotFound(msg)
        }

        val bulkSMSRequest = SMSLog()
        val campaign = failedCampaignList.content[0]

        val approvedTemplate =
            communicationRepository.smsTemplateRepository.findById(campaign.approvedTemplateId!!).let {
                if (it.isEmpty) {
                    val msg = "No sms template found."
                    log("comRetrySendBulkSMS - $msg")
                    return oneResponse.invalidData(msg)
                }
                it.get()
            }

        bulkSMSRequest.apply {
            approvedTemplateId = campaign.approvedTemplateId
            source = campaign.source
            userId = campaign.userId
            userEmail = campaign.userEmail
            userName = campaign.userName
            objectName = campaign.objectName
            smsCampaignId = nCampaignId
            jsonMessage = ArrayList()
        }

        for (smsLog in failedCampaignList) {
            val smsBody = SMSBody()
            smsBody.to = smsLog.mobileNumber
            smsBody.custom1 = smsLog.objectId
            smsBody.message = smsLog.message
            bulkSMSRequest.jsonMessage!!.add(smsBody)
        }

        communicationBackgroundProcessHelper.sendBulkSMS(bulkSMSRequest, approvedTemplate)

        return oneResponse.getSuccessResponse(
            JSONObject().put(MESSAGE, "Retry send bulk sms process started")
        )

    }

    @Throws(Exception::class)
    fun comGetSMSLogs(epAuthRequest: EPAuthRequest, requestJson: JSONObject): ResponseEntity<String> {

        val epLogger = partnerLogHelper.Builder(epAuthRequest)

        val id = requestJson.optString("id", NA)
        val type = requestJson.optString("type", NA)
        val page = requestJson.optJSONObject("page", null)


        if (id.isInvalid() || type.isInvalid()) {
            val msg = "Invalid data"
            log("comGetSMSLogs - $msg")
            epLogger
                .setServiceName(object {}.javaClass.enclosingMethod.name)
                .setResponseStatus(201)
                .setRequestStatus(UserActionStatus.FAILURE)
                .setRequestDesc(msg)
                .log()
            return oneResponse.invalidData(msg)
        }

        var pageRequest: Pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE)

        if (null != page) {
            pageRequest = PageRequest.of(
                page.optInt("pageNumber", 0),
                page.optInt("pageSize", 0),
                Sort.unsorted()
            )
        }

        val smsLogs = if (type == "objectId") {
            communicationRepository.smsLogRepository.findAllByObjectId(id, pageRequest)
        } else communicationRepository.smsLogRepository.findAllBySmsCampaignId(id, pageRequest)

        epLogger
            .setServiceName(object {}.javaClass.enclosingMethod.name)
            .setResponseStatus(200)
            .setRequestStatus(UserActionStatus.SUCCESS)
            .log()

        return oneResponse.getSuccessResponse(JSONObject(objectMapper.writeValueAsString(smsLogs)))
    }

    @Throws(Exception::class)
    fun getSMSLogDetailStatus(epAuthRequest: EPAuthRequest, requestBody: JSONObject): ResponseEntity<String> {

        val epLogger = partnerLogHelper.Builder(epAuthRequest)

        val ids = requestBody.optJSONArray("ids")

        if (ids.isEmpty) {
            val msg = "Invalid data"
            log("comGetSMSLogDetail - $msg")
            epLogger
                .setServiceName(object {}.javaClass.enclosingMethod.name)
                .setResponseStatus(201)
                .setRequestStatus(UserActionStatus.FAILURE)
                .setRequestDesc(msg)
                .log()
            return oneResponse.invalidData(msg)
        }

        val campaignIds = ArrayList<String>()
        ids.forEach { campaignIds.add(it.toString()) }


        val smsLogStats = communicationRepository.findSMSCampaignStats(campaignIds)

        if (smsLogStats == null) {
            val msg = "smsCampaignid list is empty"
            log("getSMSLogDetailStatus: $msg")

            epLogger.setRequestStatus(UserActionStatus.FAILURE).log()
            return OneResponse().getFailureResponse(
                LocalResponse()
                    .setMessage(msg)
                    .setError(Errors.RESOURCE_NOT_FOUND.value)
                    .setAction(Actions.CANCEL.value).toJson()
            )
        }

        epLogger
            .setServiceName(object {}.javaClass.enclosingMethod.name)
            .setResponseStatus(200)
            .setRequestStatus(UserActionStatus.SUCCESS)
            .log()

        return oneResponse.getSuccessResponse(
            JSONObject().put("logStats", smsLogStats)
        )

    }

    @Throws(Exception::class)
    fun comGetSMSLogDetail(epAuthRequest: EPAuthRequest, id: String): ResponseEntity<String> {

        val epLogger = partnerLogHelper.Builder(epAuthRequest)

        if (id.isInvalid()) {
            val msg = "Invalid data"
            log("comGetSMSLogDetail - $msg")
            epLogger
                .setServiceName(object {}.javaClass.enclosingMethod.name)
                .setResponseStatus(201)
                .setRequestStatus(UserActionStatus.FAILURE)
                .setRequestDesc(msg)
                .log()
            return oneResponse.invalidData(msg)
        }

        val log = communicationRepository.smsLogRepository.findById(id).let {
            if (it.isEmpty) {
                val msg = "No record found for sms log id : $id"
                log("getLeadHistory - $msg")
                epLogger
                    .setServiceName(object {}.javaClass.enclosingMethod.name)
                    .setResponseStatus(201)
                    .setRequestStatus(UserActionStatus.FAILURE)
                    .setRequestDesc(msg)
                    .log()
                return oneResponse.resourceNotFound(msg)
            }
            it.get()
        }

        epLogger
            .setServiceName(object {}.javaClass.enclosingMethod.name)
            .setResponseStatus(200)
            .setRequestStatus(UserActionStatus.SUCCESS)
            .log()

        return oneResponse.getSuccessResponse(JSONObject(objectMapper.writeValueAsString(log)))

    }

    @Throws(Exception::class)
    fun comVoiceRequestCall(epAuthRequest: EPAuthRequest, callLog: CallLog): ResponseEntity<String> {

        val epLogger = partnerLogHelper.Builder(epAuthRequest)
            .setServiceName(object {}.javaClass.enclosingMethod.name)

        callLog.mandatoryFieldsCheck().let {
            if (!it.isSuccess) {
                val msg = it.message
                log("comVoiceRequestCall - $msg")
                epLogger
                    .setResponseStatus(201)
                    .setRequestStatus(UserActionStatus.FAILURE)
                    .setRequestDesc(msg)
                    .log()

                return oneResponse.invalidData(msg)
            }
        }

        callLog.orgId = epAuthRequest.orgId
        callLog.type = EnCallType.OUTBOUND.value

        val callingCreds = if (callLog.callerId.isInvalid())
            communicationRepository.callingInfoRepository.findDefaultCallingInfo()
        else
            communicationRepository.callingInfoRepository.findByCallerIdAndIsActiveTrue(callLog.callerId)

        callingCreds ?: run {
            val msg = "Failed to get calling creds from DB."
            log("comVoiceRequestCall - $msg")

            epLogger
                .setResponseStatus(201)
                .setRequestStatus(UserActionStatus.FAILURE)
                .setRequestDesc(msg)
                .log()
            return oneResponse.getFailureResponse(
                JSONObject()
                    .put(MESSAGE, "Failed to place a call")
            )
        }

        val callLocalResponse = communicationHelper.clickToCall(callLog, callingCreds)

        if (!callLocalResponse.isSuccess) {

            log("comVoiceRequestCall - ${callLocalResponse.message}")

            epLogger
                .setResponseStatus(201)
                .setRequestStatus(UserActionStatus.FAILURE)
                .setRequestDesc(callLocalResponse.message)
                .log()

            return oneResponse.getFailureResponse(
                JSONObject()
                    .put(MESSAGE, "Failed to place a call")
            )
        }

        val callResponse = JSONObject(callLocalResponse.response)
        callLog.response = callLocalResponse.response
        callLog.updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()

        callResponse.optJSONObject("data").optString("id")?.let {

            callLog.callId = it
            callLog.status = CallStatus.INITIATED.value
            communicationRepository.callLogRepository.save(callLog)

        } ?: run {

            communicationRepository.callLogRepository.save(callLog)

            log("comVoiceRequestCall - ${callLocalResponse.message}")

            epLogger
                .setResponseStatus(201)
                .setRequestStatus(UserActionStatus.FAILURE)
                .setRequestDesc(callLocalResponse.message)
                .log()

            return oneResponse.getFailureResponse(
                JSONObject()
                    .put(MESSAGE, "Failed to place a call")
            )

        }

        val responseJson = JSONObject()
        responseJson.put("hfoId", callLog.id)
        responseJson.put("callId", callLog.callId)
        responseJson.put(MESSAGE, "Call placed successfully!")

        epLogger
            .setResponseStatus(200)
            .setRequestStatus(UserActionStatus.SUCCESS)
            .log()

        return oneResponse.getSuccessResponse(responseJson)

    }

    @Throws(Exception::class)
    fun getCallLogDetail(epAuthRequest: EPAuthRequest, id: String): ResponseEntity<String> {

        val epLogger = partnerLogHelper.Builder(epAuthRequest)

        if (id.isInvalid()) {
            val msg = "Invalid call log Id"
            log("comGetCallLogDetail - $msg")
            epLogger
                .setServiceName(object {}.javaClass.enclosingMethod.name)
                .setResponseStatus(201)
                .setRequestStatus(UserActionStatus.FAILURE)
                .setRequestDesc(msg)
                .log()
            return oneResponse.invalidData(msg)
        }

        val callLog = communicationRepository.callLogRepository.findById(id).let {
            if (it.isEmpty) {
                val msg = "No record found for call log id : $id"
                log("comGetCallLogDetail - $msg")
                epLogger
                    .setServiceName(object {}.javaClass.enclosingMethod.name)
                    .setResponseStatus(201)
                    .setRequestStatus(UserActionStatus.FAILURE)
                    .setRequestDesc(msg)
                    .log()
                return oneResponse.resourceNotFound(msg)
            }
            it.get()
        }

        epLogger
            .setServiceName(object {}.javaClass.enclosingMethod.name)
            .setResponseStatus(200)
            .setRequestStatus(UserActionStatus.SUCCESS)
            .log()

        return oneResponse.getSuccessResponse(JSONObject(objectMapper.writeValueAsString(callLog)))

    }

    @Throws(Exception::class)
    fun getCallLogs(
        epAuthRequest: EPAuthRequest,
        advanceFilter: AdvanceFilter,
        pageable: Pageable?
    ): ResponseEntity<String> {

        val epLogger = partnerLogHelper.Builder(epAuthRequest)

        advanceFilter.mandatoryFieldsCheck().let {
            if (!it.isSuccess) {
                val msg = it.message
                log("getAllCallLogs - $msg")
                epLogger
                    .setServiceName(object {}.javaClass.enclosingMethod.name)
                    .setResponseStatus(201)
                    .setRequestStatus(UserActionStatus.FAILURE)
                    .setRequestDesc(msg)
                    .log()
                return oneResponse.invalidData(msg)
            }
        }

        val callLogs = callLogRepositoryMasterRepository.advancedList(
            advanceFilter,
            pageable ?: PageRequest.of(0, DEFAULT_PAGE_SIZE)
        )

        epLogger
            .setServiceName(object {}.javaClass.enclosingMethod.name)
            .setResponseStatus(200)
            .setRequestStatus(UserActionStatus.SUCCESS)
            .log()

        return oneResponse.getSuccessResponse(JSONObject(objectMapper.writeValueAsString(callLogs)))

    }

    @Throws(Exception::class)
    fun addCallLogRemark(epAuthRequest: EPAuthRequest, callLog: CallLog): ResponseEntity<String> {

        val epLogger = partnerLogHelper.Builder(epAuthRequest)
            .setServiceName(object {}.javaClass.enclosingMethod.name)

        callLog.mandatoryFieldsCheckForRemark().let {
            if (!it.isSuccess) {
                val msg = it.message
                log("addCallLogRemark - $msg")
                epLogger
                    .setResponseStatus(201)
                    .setRequestStatus(UserActionStatus.FAILURE)
                    .setRequestDesc(msg)
                    .log()
                return oneResponse.invalidData(msg)
            }
        }

        callLogRepository.findById(callLog.id!!).let { opt ->
            if (opt.isPresent) {
                opt.get().let {
                    it.remark = callLog.remark
                    it.updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
                    callLogRepository.save(it)
                }
            } else {
                return oneResponse.resourceNotFound("No call log found")
            }
        }

        epLogger
            .setResponseStatus(201)
            .setRequestStatus(UserActionStatus.FAILURE)
            .setRequestDesc("Remark added successfully!")
            .log()

        return oneResponse.getSuccessResponse(JSONObject().apply {
            put(MESSAGE, "Remark added successfully!")
        })

    }
}

