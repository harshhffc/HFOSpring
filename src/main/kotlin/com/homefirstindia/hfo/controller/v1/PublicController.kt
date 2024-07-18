package com.homefirstindia.hfo.controller.v1

import com.homefirstindia.hfo.model.v1.CallLog
import com.homefirstindia.hfo.model.v1.SMSLog
import com.homefirstindia.hfo.networking.v1.CommonNetworkingClient
import com.homefirstindia.hfo.service.v1.CommunicationService
import com.homefirstindia.hfo.service.v1.PublicService
import com.homefirstindia.hfo.utils.*
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/public/v1")
class PublicController(
    @Autowired val oneResponse: OneResponse,
    @Autowired val publicService: PublicService,
    @Autowired val communicationService: CommunicationService,
    @Autowired val commonNetworkingClient: CommonNetworkingClient
) {

    private fun log(value: String) = LoggerUtils.log("v1/${this.javaClass.simpleName}.$value")
    private fun logMethod(value: String) = LoggerUtils.logMethodCall("/public/v1/$value")

    @GetMapping(
        produces = [MediaType.TEXT_HTML_VALUE]
    )
    fun sayHello(): String? {

        log("log printed")
        println("log printed through println")

        return ("<html> " + "<title>" + "HFO Spring" + "</title>" + "<body><h1>"
                + "Successfully deployed and testing HFO Spring Application " + "</h1></body>" + "</html> ")
    }

    @PostMapping(
        "/PropertyInsight.reportCallback",
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun pcPropertyInsightReportCallback(
        request: HttpServletRequest,
        @RequestBody piResponseString: String,
    ): ResponseEntity<String> {

        logMethod("PropertyInsight.reportCallback")

        return try {

            val sessionPasscode = request.getHeader(SESSION_PASSCODE)

            publicService.pcPropertyInsightReportCallback(
                sessionPasscode, piResponseString)
        } catch (e: Exception) {
            log("pcProcessPropertyInsightDetail - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @GetMapping(
        "/SMS.disposition",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun comSMSDisposition(
        @RequestParam status: String,
        @RequestParam sid: String,
        @RequestParam custom: String,
        @RequestParam custom1: String,
        @RequestParam(required = false) senttime: String,
        @RequestParam(required = false) delivered: String,
        @RequestParam mobile: String,
    ): ResponseEntity<String>? {

        return try {

            var smsStatus = status

            if (!status.isInvalid()) {
                smsStatus = smsStatus.split(",")[0]
            }

            val smsLog = SMSLog().apply {
                this.status = smsStatus
                smsId = sid
                objectId = custom1
                sentDatetime = senttime
                deliveredDatetime = delivered
                mobileNumber = mobile
            }

            communicationService.comSMSDisposition(smsLog)

        } catch (e: Exception) {
            log("comSMSDisposition - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }

    }

    @GetMapping(
        "/callDisposition",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun pubClickToCallDisposition(

        @RequestParam(value = "id") callId: String,
        @RequestParam(value = "caller") caller: String,
        @RequestParam(value = "receiver") receiver: String,
        @RequestParam(value = "callerStatus") callerStatus: String,
        @RequestParam(value = "receiverStatus") receiverStatus: String,
        @RequestParam(value = "durationInSec") duration: Int,
        @RequestParam(value = "recordpath") recordingUrl: String,
        @RequestParam(value = "callStartTime") startTime: String,
        @RequestParam(value = "callEndTime") endTime: String,
        @RequestParam(value = "callerProvider") provider: String,
        @RequestParam(value = "callerLocation") location: String,

        ): ResponseEntity<String>? {

        logMethod("pubClickToCallDisposition")

        val callDetails = CallLog().apply {
            this.callId = callId
            this.caller = caller
            this.receiver = receiver
            this.callerStatus = callerStatus
            this.receiverStatus = receiverStatus
            durationInSec = duration
            this.recordingUrl = recordingUrl
            callStartTime = startTime
            callEndTime = endTime
            callerProvider = provider
            callerLocation = location
        }

        return try {
            publicService.clickToCallDisposition(callDetails)
        } catch (e: Exception) {
            log("pubClickToCallDisposition - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }

    }

    @GetMapping(
        "/fetchDocument",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun pubFetchDocument(
        request: HttpServletRequest,
        @RequestParam("fid") fid: String,
    ): ResponseEntity<String>? {

        logMethod("pubFetchDocument")

        return try {
            publicService.fetchDocument(fid)
        } catch (e: Exception) {
            log("pubFetchDocument - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }

    }

    @PostMapping(
        "/wa.captureMessageResponse",
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun pubCaptureMessage(
        @RequestBody requestBody: String
    ): ResponseEntity<String>? {

        logMethod("pubCaptureMessage")

        return try {
            publicService.captureWhatsappMessageResponse(requestBody)
        } catch (e: Exception) {
            log("pubCaptureMessage - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }

    }

    @PostMapping(
        "/mobile.report",
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun pcMobileReportCallback(
        @RequestBody request: String,
    ): ResponseEntity<String> {

        logMethod("pcMobileReportCallback")

        return try {
            publicService.pcMobileReportCallback(JSONObject(request))
        } catch (e: Exception) {
            log("pcMobileNumberValidationCallback - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

}