package com.homefirstindia.hfo.service.v1

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.homefirstindia.hfo.dto.v1.PaymentLinkDTO
import com.homefirstindia.hfo.dto.v1.externalpartner.EPAuthRequest
import com.homefirstindia.hfo.helper.v1.*
import com.homefirstindia.hfo.manager.v1.SalesforceManager
import com.homefirstindia.hfo.model.v1.*
import com.homefirstindia.hfo.model.v1.salesforce.ServiceRequest
import com.homefirstindia.hfo.networking.v1.EnSfObjectName
import com.homefirstindia.hfo.networking.v1.HFONetworkClient
import com.homefirstindia.hfo.repository.v1.*
import com.homefirstindia.hfo.utils.*
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import kotlin.math.abs

@Service
class CustomerService(
    @Autowired val oneResponse: OneResponse,
    @Autowired val partnerLogHelper: PartnerLogHelper,
    @Autowired val botLogHelper: BotLogHelper,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val salesforceManager: SalesforceManager,
    @Autowired val botUserLogRepository: BotUserLogRepository,
    @Autowired val serviceRequestRepository: ServiceRequestRepository,
    @Autowired val hfoNetworkClient: HFONetworkClient,

    ) {

    private fun log(value: String) = LoggerUtils.log("v1/${this.javaClass.simpleName}.$value")
    private fun printLog(value: String) = LoggerUtils.printLog("v1/${this.javaClass.simpleName}.$value")

    private fun logFailure(message: String, botLog: BotLogHelper.Builder,
                   epLogger: PartnerLogHelper.Builder) {

        botLog.setResponseStatus(false).setRequestStatus(UserActionStatus.FAILURE)
            .setRequestDesc(message).log()

        epLogger.setRequestDesc(message).setRequestStatus(UserActionStatus.FAILURE).log()

    }

    @Throws(Exception::class)
    fun loanLookUp(
        epAuthRequest: EPAuthRequest,
        mobileNumber: String,
    ): ResponseEntity<String>? {

        val epLogger = partnerLogHelper.Builder(epAuthRequest)

        epLogger
            .setRequestStatus(UserActionStatus.SUCCESS)
            .setServiceName(object {}.javaClass.enclosingMethod.name)
            .log()

        val botLog = botLogHelper.Builder()
            .setServiceName(object {}.javaClass.enclosingMethod.name)
            .setRequestStatus(UserActionStatus.SUCCESS)
            .setRawRequest(mobileNumber)
            .log()

        val cMobileNumber = if (mobileNumber.length > 10)
            mobileNumber.substring(mobileNumber.length - 10)
        else mobileNumber


        val loanList = salesforceManager.getLAIListByMobileNumber(cMobileNumber)

        return if (loanList!!.size > 0) {

            botLog.responseStatus = true
            botUserLogRepository.save(botLog)

            oneResponse.getSuccessResponse(
                JSONObject()
                    .put("loans", objectMapper.writeValueAsString(loanList))
                    .put(MESSAGE, "Please select a loan account to continue")
            )

        } else {

            botLog.responseStatus = false
            botLog.requestDesc = "No Active Loan found with this mobile number"
            botUserLogRepository.save(botLog)

            oneResponse.getFailureResponse(
                LocalResponse()
                    .setMessage("Sorry, this mobile number is not registered with us. \n" +
                            "Kindly reach out to us from your registered mobile number for assistance")
                    .setError(Errors.RESOURCE_NOT_FOUND.value)
                    .setAction(Actions.CANCEL.value).toJson()
            )

        }

    }


    fun getLoanDetail(
        epAuthRequest: EPAuthRequest,
        loanAccountNumber: String,
    ): ResponseEntity<String> {

        val epLogger = partnerLogHelper.Builder(epAuthRequest)

        val botLog = botLogHelper.Builder()
            .setServiceName(object {}.javaClass.enclosingMethod.name)
            .setRawRequest(JSONObject().put("loanAccountNumber", loanAccountNumber).toString())

        if (loanAccountNumber.isInvalid()) {
            val message = "Invalid loan account number"
            log("getLoanDetail - $message || $loanAccountNumber")
            logFailure(message, botLog, epLogger)
            return oneResponse.invalidData(message)

        }

        val loan = salesforceManager.fetchDisbursmentDetails(loanAccountNumber)

        if (loan.first == NA && loan.second == NA && loan.third == NA) {
            return oneResponse.getFailureResponse(
                LocalResponse()
                    .setMessage("No loan found with $loanAccountNumber LAI")
                    .setError(Errors.RESOURCE_NOT_FOUND.value)
                    .setAction(Actions.CANCEL.value).toJson()
            )
        }


        botLog.setResponseStatus(true).setRequestStatus(UserActionStatus.SUCCESS).log()
        epLogger.setRequestStatus(UserActionStatus.SUCCESS).log()

        return oneResponse.getSuccessResponse(
            JSONObject()
                .put("loanDisbursalStatus", loan.first)
                .put("percentageDisbursed", loan.second)
                .put("loanDisbursedAmount", loan.third)
        )

    }

    fun createCase(
        epAuthRequest: EPAuthRequest,
        serviceRequest: ServiceRequest,
    ): ResponseEntity<String> {

        val epLogger = partnerLogHelper.Builder(epAuthRequest)

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        val botLog = botLogHelper.Builder()
            .setServiceName(object {}.javaClass.enclosingMethod.name)
            .setRawRequest(objectMapper.writeValueAsString(serviceRequest))

        if (serviceRequest.loanAccountNumber.isInvalid()) {
            val message = "Invalid loan account number"
            log("createCase - $message || ${objectMapper.writeValueAsString(serviceRequest)}")
            logFailure(message, botLog, epLogger)
            return oneResponse.invalidData(message)
        }

        val requestType =
            getServiceRequestTypeData().filter { serviceRequestType ->
                serviceRequestType.requestType == serviceRequest.masterCaseReason
            }

        if (requestType.isEmpty()) {
            val message = "Invalid Request type"
            log("createCase - $message || ${objectMapper.writeValueAsString(serviceRequest)}")

            logFailure(message, botLog, epLogger)
            return oneResponse.invalidData(message)

        }

        val subReasonType = requestType.first().categories.filter { s: String -> s == serviceRequest.caseReason }
        if (subReasonType.isEmpty()) {
            val message = "Invalid Request Sub Reason"
            log("createCase - $message || ${objectMapper.writeValueAsString(serviceRequest)}")
            logFailure(message, botLog, epLogger)
            return oneResponse.invalidData(message)

        }

        if (serviceRequest.subject.isInvalid())
            serviceRequest.subject = "${requestType[0].requestType} - ${subReasonType[0]}"

        serviceRequestRepository.save(serviceRequest)


        // Create on SF

        val sfResponse = salesforceManager.createCase(serviceRequest)


        if (sfResponse.isSuccess)
            serviceRequest.caseId = sfResponse.message
        else {
           val message = "Failed to save case on sf"
            log("createCase - failed to create case on SF || ${objectMapper.writeValueAsString(serviceRequest)}")

            logFailure(message, botLog, epLogger)
            return oneResponse.operationFailedResponse(sfResponse.message)
        }

        serviceRequest.updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
        serviceRequestRepository.save(serviceRequest)

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)

        botLog.setResponseStatus(true).setRequestStatus(UserActionStatus.SUCCESS).log()
        epLogger.setRequestStatus(UserActionStatus.SUCCESS).log()

        return oneResponse.getSuccessResponse(
//            if (!IS_PRODUCTION || IS) {
//                JSONObject().put(
//                    "serviceRequest",
//                    JSONObject(objectMapper.writeValueAsString(serviceRequest))
//                )
//            } else {
            JSONObject().put(MESSAGE, "Case created successfully!")

//            }
        )

    }

    fun getEmiDueAmount(
        epAuthRequest: EPAuthRequest,
        loanAccountNumber: String,
    ): ResponseEntity<String> {

        val epLogger = partnerLogHelper.Builder(epAuthRequest)

        val botLog = botLogHelper.Builder()
            .setServiceName(object {}.javaClass.enclosingMethod.name)
            .setRawRequest(JSONObject().put("loanAccountNumber", loanAccountNumber).toString())

        if (loanAccountNumber.isInvalid()) {
            val message = "Invalid loan account number"
            log("getEmiDueAmount - $message: $loanAccountNumber")
            logFailure(message, botLog, epLogger)
            return oneResponse.invalidData(message)
        }

        val emiDueAmount = salesforceManager.fetchEmiDueAmountDetails(loanAccountNumber)

        botLog.setResponseStatus(true).setRequestStatus(UserActionStatus.SUCCESS).log()
        epLogger.setRequestStatus(UserActionStatus.SUCCESS).log()

        return oneResponse.getSuccessResponse(
            JSONObject().put("emiDueAmount", abs(emiDueAmount))
                .put(MESSAGE, "EMI due fetched successfully")
        )

    }

    @Throws(Exception::class)
    fun epRequestPaymentLink(
        epAuthRequest: EPAuthRequest,
        paymentLinkDTO: PaymentLinkDTO,
    ): ResponseEntity<String> {

        val epLogger = partnerLogHelper.Builder(epAuthRequest)

        val botLog = botLogHelper.Builder()
            .setServiceName(object {}.javaClass.enclosingMethod.name)
            .setRawRequest(objectMapper.writeValueAsString(paymentLinkDTO))

        paymentLinkDTO.mandatoryFieldsCheck().let {
            if (!it.isSuccess) {
                log("epRequestPaymentLink - ${it.message}")
                logFailure(it.message, botLog, epLogger)
                return oneResponse.invalidData(it.message)
            }
        }

        val sfResponseJson = salesforceManager.getSFObjectDetails(listOf("Opportunity__r.Id"),
            EnSfObjectName.LOAN_ACCOUNT.value, "Name", paymentLinkDTO.loanAccountNumber!!)

        var sfOpportunityId = NA

        sfResponseJson?.let {

            if (it.getInt("totalSize") > 0) {

                val loanJson = sfResponseJson.getJSONArray("records").getJSONObject(0)

                loanJson.optJSONObject("Opportunity__r")?.optString("Id")?.let { oppId->
                    sfOpportunityId = oppId
                } ?: run {
                    val message = "Invalid opportunity Id"
                    logFailure(message, botLog, epLogger)
                    return oneResponse.invalidData(message)
                }

            } else {
                val message = "Invalid opportunity Id"
                logFailure(message, botLog, epLogger)
                return oneResponse.invalidData(message)
            }

        }

        val requestObject = paymentLinkDTO.paymentLinkReqJson(sfOpportunityId)

        val localResponse = hfoNetworkClient.post(
            HFONetworkClient.Endpoints.REQUEST_PAYMENT_LINK, requestObject)

        if (!localResponse.isSuccess) {
            log("epRequestPaymentLink - HFO Response: ${localResponse.message}")
            logFailure(localResponse.message, botLog, epLogger)
            return oneResponse.getFailureResponse(
                localResponse.setMessage("Failed to request payment link").toJson()
            )
        }

        val paymentLink = JSONObject(localResponse.response).optString("paymentLink")
        val paymentId = JSONObject(localResponse.response).optString("id")

        botLog.setRawResponse(JSONObject(localResponse.response).toString())
            .setResponseStatus(true).setRequestStatus(UserActionStatus.SUCCESS).log()

        epLogger.setRequestStatus(UserActionStatus.SUCCESS).log()

        return oneResponse.getSuccessResponse(
            JSONObject().put("paymentLink", paymentLink)
                .put("paymentId", paymentId)
                .put(MESSAGE, "Payment link requested successfully!")
        )

    }

}
