package com.homefirstindia.hfo.service.v1

import com.homefirstindia.hfo.clients.AmazonClient
import com.homefirstindia.hfo.clients.DigitapClient
import com.homefirstindia.hfo.clients.EnS3BucketPath
import com.homefirstindia.hfo.helper.v1.*
import com.homefirstindia.hfo.model.v1.CallLog
import com.homefirstindia.hfo.model.v1.WhatsAppAvailability
import com.homefirstindia.hfo.model.v1.WhatsAppMessageDisposition
import com.homefirstindia.hfo.repository.v1.CallLogRepository
import com.homefirstindia.hfo.repository.v1.DocumentRepositoryMaster
import com.homefirstindia.hfo.repository.v1.WhatsappRepositoryMaster
import com.homefirstindia.hfo.repository.v1.MobileNumberValidationRepository
import com.homefirstindia.hfo.utils.*
import com.homefirstindia.hfo.utils.DateTimeUtils.getCurrentDateTimeInIST
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service


@Service
class PublicService(
    @Autowired val propertyInsightHelper: PropertyInsightHelper,
    @Autowired val oneResponse: OneResponse,
    @Autowired val callLogRepository: CallLogRepository,
    @Autowired val whatsappRepositoryMaster: WhatsappRepositoryMaster,
    @Autowired val communicationBackgroundProcessHelper: CommunicationBackgroundProcessHelper,
    @Autowired val documentRepositoryMaster: DocumentRepositoryMaster,
    @Autowired val amazonClient: AmazonClient,
    @Autowired val mobileNumberValidationRepository: MobileNumberValidationRepository,
    @Autowired val digitapClient: DigitapClient
) {

    private fun log(value: String) = LoggerUtils.log("v1/${this.javaClass.simpleName}.$value")

    private fun methodName(value: String): String = "v1/${this.javaClass.simpleName}.$value"

    @Throws(Exception::class)
    fun pcPropertyInsightReportCallback(
        sessionPasscode: String,
        piResponseString: String,
    ): ResponseEntity<String> {

        if (sessionPasscode != propertyInsightHelper.tealCred().memberPasscode) {
            log("pcPropertyInsightReportCallback - session passcode does not matched")
            return oneResponse.getFailureResponse(
                LocalResponse()
                    .setMessage("Invalid session passcode")
                    .toJson()
            )
        }

        val sendReportResponse = propertyInsightHelper.processPropertyInsightCallbackDetail(
            piResponseString
        )

        return if (sendReportResponse.isSuccess) {

            oneResponse.getSuccessResponse(
                JSONObject()
                    .put(MESSAGE, sendReportResponse.message)
            )

        } else {
            oneResponse.getFailureResponse(sendReportResponse.toJson())
        }

    }

    @Throws(Exception::class)
    fun clickToCallDisposition(callLog: CallLog): ResponseEntity<String> {

        if (callLog.callId.isInvalid()) {
            log("clickToCallDisposition - Invalid call Id: ${callLog.callId}")
            return oneResponse.getSuccessResponse(JSONObject().put(MESSAGE, "Call disposition captured!"))
        }

        val eCallLog = callLogRepository.findCallLogByCallId(callLog.callId) ?: run {
            log("clickToCallDisposition - no call log exist with call Id: ${callLog.callId}")
            return oneResponse.getSuccessResponse(JSONObject().put(MESSAGE, "Call disposition captured!"))
        }

        eCallLog.apply {

            callStartTime = DateTimeUtils.getDateTimeFromEpoch(
                callLog.callStartTime?.toLong()!!,
                DateTimeFormat.yyyy_MM_dd_HH_mm_ss,
                DateTimeZone.IST
            )

            callEndTime = DateTimeUtils.getDateTimeFromEpoch(
                callLog.callEndTime?.toLong()!!,
                DateTimeFormat.yyyy_MM_dd_HH_mm_ss,
                DateTimeZone.IST
            )

            callerStatus = callLog.callerStatus
            receiverStatus = callLog.receiverStatus
            durationInSec = callLog.durationInSec
            callerProvider = callLog.callerProvider
            callerLocation = callLog.callerLocation
            updateDatetime = getCurrentDateTimeInIST()

            recordingUrl = if (!callLog.recordingUrl.isInvalid()
                && !callLog.recordingUrl!!.startsWith("https:")
            ) {
                "https: ${callLog.recordingUrl}"
            } else callLog.recordingUrl
        }

        callLogRepository.save(eCallLog)

        communicationBackgroundProcessHelper.downloadAndUploadFileToS3(eCallLog)

        return OneResponse().getSuccessResponse(
            JSONObject().put(MESSAGE, "Call disposition captured successfully")
        )
    }

    @Throws(Exception::class)
    fun fetchDocument(fid: String): ResponseEntity<String>? {

        if (fid.isInvalid()) {
            log("fetchDocument - Invalid fid: $fid")
            return oneResponse.invalidData("Invalid fid")
        }

        val attachment = documentRepositoryMaster.attachmentRepository.findAttachmentByFid(fid) ?: run {
            log("fetchDocument - No attachment found for fid testing with anuj: $fid")
            return OneResponse().resourceNotFound("No attachment found")
        }

        val bucketPath: EnS3BucketPath = when (val objectType = MyObject[attachment.objectType!!]) {

            MyObject.E_SIGN_DOCUMENT -> EnS3BucketPath.SIGNED_DOCS
            MyObject.BANK_ACCOUNT -> EnS3BucketPath.BANK_ACCOUNT
            MyObject.SITE_PHOTO -> EnS3BucketPath.PROPERTY_SITE_PHOTOGRAPH
            MyObject.E_STAMP_REQUEST -> EnS3BucketPath.ESTAMP
            MyObject.PROPERTY_INSIGHT -> EnS3BucketPath.PROPERTY_INSIGHT_REPORTS
            MyObject.CALL_LOG -> EnS3BucketPath.AUDIO_RECORDING
            MyObject.SF_LOAN_AMORT_REQUEST -> EnS3BucketPath.AMORT_CALCULATION
            MyObject.USER_REQUEST -> EnS3BucketPath.LMS_EXPORT
            MyObject.PROPERTY -> EnS3BucketPath.PROPERTY_IMAGES
            MyObject.KYC_DOCUMENT -> {

                when (attachment.attachementType) {
                    AttachmentType.AADHAAR.value -> EnS3BucketPath.AADHAAR_IMAGES
                    AttachmentType.PASSPORT.value -> EnS3BucketPath.PASSPORT_IMAGES
                    AttachmentType.VOTER_ID.value -> EnS3BucketPath.VOTER_IMAGES
                    AttachmentType.DRIVING_LICENCE.value -> EnS3BucketPath.DRIVING_LICENCE_IMAGES
                    else -> {
                        log("fetchDocument - No attachment type found while fetching kyc document.")
                        return OneResponse().invalidData("No attachment type found")
                    }
                }

            }

            else -> {
                log("fetchDocument - Invalid object type: $objectType")
                return OneResponse().invalidData("Invalid object type")
            }
        }

        val docPublicURL = amazonClient.getPublicURL(attachment.fileName!!, bucketPath, 10)

        if (docPublicURL.isInvalid()) {
            log("Failed to generate public url.")
            return OneResponse().operationFailedResponse("Failed to get the Document")
        }

        val responseObject = JSONObject()
        responseObject.put("documentUrl", docPublicURL)

        return OneResponse().getSuccessResponse(responseObject)

    }

    @Throws(Exception::class)
    fun captureWhatsappMessageResponse(
        requestBody: String
    ): ResponseEntity<String> {

        val requestJSONArray = JSONArray(requestBody)

        if (requestJSONArray.isEmpty) {
            log("captureWhatsappMessageResponse - Data is empty")
            return oneResponse.getSuccessResponse(JSONObject().put(MESSAGE, "Message callback response captured successfully"))
        }

        val waDisposition = WhatsAppMessageDisposition()
            .parseDisposition(requestJSONArray.getJSONObject(0))

        waDisposition.mobileNumber = waDisposition.mobileNumber?.takeLast(10)

        whatsappRepositoryMaster.whatsAppMessageDispositionRepository.save(waDisposition)

        whatsappRepositoryMaster.whatsAppAvailabilityRepository
            .findByMobileNumber(waDisposition.mobileNumber!!)?.let {
                it.availabilityReason = waDisposition.deliveryReason
                it.isAvailable = waDisposition.deliveryReason != EnWhatsAppNumberStatus.UNKNOWN_SUBSCRIBER.key
                it.lastTriedDatetime = getCurrentDateTimeInIST()
                whatsappRepositoryMaster.whatsAppAvailabilityRepository.save(it)
            } ?: run {
                whatsappRepositoryMaster.whatsAppAvailabilityRepository
                    .save(WhatsAppAvailability().parseDisposition(waDisposition))
            }

        whatsappRepositoryMaster.whatsAppMessageRepository
            .findByMessageId(waDisposition.messageId!!)?.let {
                it.deliveryStatus = waDisposition.deliveryStatus
                whatsappRepositoryMaster.whatsAppMessageRepository.save(it)
            }


        return OneResponse().getSuccessResponse(
            JSONObject().put(MESSAGE, "Message callback response captured successfully")
        )

    }

    fun pcMobileReportCallback(
        requestJson: JSONObject
    ): ResponseEntity<String> {

        val clientRefNum = requestJson.optString("client_ref_num", NA)

        if (clientRefNum.isInvalid()) {
            log("pcMobileReportCallback - Invalid reference number: $clientRefNum")
            return oneResponse.getSuccessResponse(JSONObject().put(MESSAGE, "Report captured successfully!"))
        }

        val eMobNumValidation = mobileNumberValidationRepository.findByReferenceNumber(clientRefNum)

        eMobNumValidation ?: run {
            LoggerUtils.log("pcMobileReportCallback - No request found for reference number: $clientRefNum")
            return oneResponse.getSuccessResponse(JSONObject().put(MESSAGE, "Report captured successfully!"))
        }

        eMobNumValidation.status = requestJson.optString("txn_status_code", NA)
        eMobNumValidation.updateDatetime = getCurrentDateTimeInIST()

        mobileNumberValidationRepository.save(eMobNumValidation)

        if (eMobNumValidation.status.isInvalid() && eMobNumValidation.status != "ReportGenerated") {
            log("pcMobileReportCallback - Report not generated for reference number: $clientRefNum")
            return oneResponse.getSuccessResponse(JSONObject().put(MESSAGE, "Report captured successfully!"))
        }

        if (eMobNumValidation.transactionId.isInvalid()) {
            log("pcMobileReportCallback - Invalid transaction id: ${eMobNumValidation.transactionId}")
            return oneResponse.getSuccessResponse(JSONObject().put(MESSAGE, "Report captured successfully!"))
        }

        val lResponse = digitapClient.getOtpStatus(eMobNumValidation.transactionId!!)

        if (!lResponse.isSuccess) {
            log("pcMobileReportCallback - Fail to get report for reference number: $clientRefNum")
            return oneResponse.getSuccessResponse(JSONObject().put(MESSAGE, "Report captured successfully!"))
        }

        eMobNumValidation.verifyOtpResponse = JSONObject(lResponse.message).toString()
        eMobNumValidation.updateDatetime = getCurrentDateTimeInIST()
        mobileNumberValidationRepository.save(eMobNumValidation)

        return oneResponse.getSuccessResponse(
            JSONObject().put(MESSAGE, "Report captured successfully!")
        )

    }

}