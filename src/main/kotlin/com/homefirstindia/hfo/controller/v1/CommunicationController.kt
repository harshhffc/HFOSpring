package com.homefirstindia.hfo.controller.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.homefirstindia.hfo.dto.v1.AdvanceFilter
import com.homefirstindia.hfo.dto.v1.externalpartner.EPAuthRequest
import com.homefirstindia.hfo.model.v1.CallLog
import com.homefirstindia.hfo.model.v1.SMSLog
import com.homefirstindia.hfo.service.v1.CommunicationService
import com.homefirstindia.hfo.utils.ID
import com.homefirstindia.hfo.utils.LoggerUtils
import com.homefirstindia.hfo.utils.OneResponse
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/com/v1")
class CommunicationController(
    @Autowired val oneResponse: OneResponse,
    @Autowired val communicationService: CommunicationService,
    @Autowired val objectMapper: ObjectMapper,
) {

    private fun logMethod(value: String) = LoggerUtils.logMethodCall("/communication/v1/$value")

    private fun log(value: String) = LoggerUtils.log("v1/${this.javaClass.simpleName}.$value")

    @PostMapping(
        "/SMS.sendBulk",
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun comSendBulkSMS(
        request: HttpServletRequest,
        @RequestBody bulkSMSRequest: SMSLog,
    ): ResponseEntity<String>? {

        logMethod("comSendBulkSMS")

        return try {
            communicationService.comSendBulkSMS(EPAuthRequest(request), bulkSMSRequest)

        } catch (e: Exception) {
            log("comSendBulkSMS - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }

    }

    @PostMapping(
        "/SMS.retrySendBulk",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun comRetrySendBulkSMS(
        @RequestBody retryBulkSMSRequest: String,
    ): ResponseEntity<String>? {

        logMethod("comRetrySendBulkSMS")

        return try {
            communicationService.comRetrySendBulkSMS(JSONObject(retryBulkSMSRequest))
        } catch (e: Exception) {
            log("comRetrySendBulkSMS - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }

    }

    @PostMapping(
        "/SMS.logs",
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun comGetSMSLogs(
        request: HttpServletRequest,
        @RequestBody jsonRequest: String,
    ): ResponseEntity<String>? {

        logMethod("comGetSMSLogs request body $jsonRequest")

        return try {
            communicationService.comGetSMSLogs(EPAuthRequest(request), JSONObject(jsonRequest))
        } catch (e: Exception) {
            log("comGetSMSLogs - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }


    @GetMapping(
        "/SMS.logDetail",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun comGetSMSLogDetail(
        request: HttpServletRequest,
        @RequestParam(ID) id: String,
    ): ResponseEntity<String>? {

        logMethod("comGetSMSLogDetail")
        return try {
            communicationService.comGetSMSLogDetail(EPAuthRequest(request), id)
        } catch (e: Exception) {
            log("comGetSMSLogDetail - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @PostMapping(
        "/SMS.logDetailStatus",
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun comGetSMSLogDetailStatus(
        request: HttpServletRequest,
        @RequestBody requestBody: String,
    ): ResponseEntity<String>? {

        logMethod("comGetSMSLogDetailStatus")


        return try {
            communicationService.getSMSLogDetailStatus(EPAuthRequest(request), JSONObject(requestBody))
        } catch (e: Exception) {
            log("comGetSMSLogDetailStatus - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @PostMapping(
        "/Voice.requestCall",
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun comRequestCall(
        request: HttpServletRequest,
        @RequestBody callLog: CallLog,
    ): ResponseEntity<String>? {

        logMethod("comRequestCall")

        return try {
            communicationService.comVoiceRequestCall(EPAuthRequest(request), callLog)
        } catch (e: Exception) {
            log("comRequestCall - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @GetMapping(
        "/CallLog.detail",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun comGetCallLogDetail(
        request: HttpServletRequest,
        @RequestParam(ID) id: String,
    ): ResponseEntity<String>? {

        logMethod("comGetCallLogDetail")

        return try {
            communicationService.getCallLogDetail(EPAuthRequest(request), id)
        } catch (e: Exception) {
            log("comGetCallLogDetail - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @PostMapping(
        "/CallLogs.advanceFilter",
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun comGetCallLogs(
        request: HttpServletRequest,
        @RequestBody advanceFilter: AdvanceFilter,
        pageable: Pageable?
    ): ResponseEntity<String>? {

        logMethod("comGetCallLogs")

        return try {
            communicationService.getCallLogs(EPAuthRequest(request), advanceFilter, pageable)
        } catch (e: Exception) {
            log("comGetCallLogs - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }

    }

    @PostMapping(
        "/CallLog.addRemark",
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun comAddCallLogRemark(
        request: HttpServletRequest,
        @RequestBody callLog: CallLog
    ): ResponseEntity<String>? {

        logMethod("comAddRemark")

        return try {
            communicationService.addCallLogRemark(EPAuthRequest(request), callLog)
        } catch (e: Exception) {
            log("comAddCallLogRemark - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

}