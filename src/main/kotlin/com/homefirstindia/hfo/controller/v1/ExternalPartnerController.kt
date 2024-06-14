package com.homefirstindia.hfo.controller.v1

import com.homefirstindia.hfo.dto.v1.MessageDTO
import com.homefirstindia.hfo.dto.v1.PaymentLinkDTO
import com.homefirstindia.hfo.dto.v1.externalpartner.EPAuthRequest
import com.homefirstindia.hfo.model.v1.*
import com.homefirstindia.hfo.model.v1.salesforce.ServiceRequest
import com.homefirstindia.hfo.service.v1.CustomerService
import com.homefirstindia.hfo.service.v1.ExternalPartnerService
import com.homefirstindia.hfo.utils.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/ep/v1")
class ExternalPartnerController(
    @Autowired val oneResponse: OneResponse,
    @Autowired val externalPartnerService: ExternalPartnerService,
    @Autowired val customerService: CustomerService
) {

    private fun logMethod(value: String) = LoggerUtils.logMethodCall("/ep/v1/$value")

    private fun log(value: String) = LoggerUtils.log("v1/${this.javaClass.simpleName}.$value")

    @GetMapping(
        "/authenticate",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun epAuthenticate(
        request: HttpServletRequest
    ): ResponseEntity<String>? {

        logMethod("authenticate")

        return try {
            externalPartnerService.authenticate(EPAuthRequest(request))
        } catch (e: Exception) {
            log("authenticate - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @GetMapping(
        "/wa.optIn/{mobileNumber}",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun epWhatsappOptIn(
        request: HttpServletRequest,
        @PathVariable(MOBILE_NUMBER) mobileNumber: String
    ): ResponseEntity<String>? {

        logMethod("wa.optIn/$mobileNumber")

        return try {
            externalPartnerService.whatsappOptIn(EPAuthRequest(request), mobileNumber)
        } catch (e: Exception){
            log("epWhatsappOptIn - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }

    }

    @PostMapping(
        "/wa.sendMessage",
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun epSendWhatsappMessage(
        request: HttpServletRequest,
        @RequestBody singleTextTemplate: MessageDTO
    ): ResponseEntity<String>? {

        logMethod("wa.sendMessage")

        return try {
            externalPartnerService.sendWhatsappMessage(
                EPAuthRequest(request), singleTextTemplate)
        } catch (e: Exception) {
            log("epSendWhatsappMessage - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @PostMapping(
        "/wa.send.bulkTextTemplate",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun epWhatsAppTemplateTextBulk(
        request: HttpServletRequest,
        @RequestParam("file") file: MultipartFile,
        @RequestParam("header") header: String?,
        @RequestParam("footer") footer: String?,
        @RequestParam("requestee") requestee: String?
    ): ResponseEntity<String>? {

        logMethod("wa.send.bulkTextTemplate")

        return try {
            externalPartnerService.sendWhatsAppTextTemplateBulk(
                EPAuthRequest(request), file, header, footer, requestee)
        } catch (e: Exception) {
            log("epWhatsAppTemplateTextBulk - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @PostMapping(
        "/Location.distanceMatrix",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun epGetLocationDistanceMatrix(
        request: HttpServletRequest,
        @RequestBody locationDistanceMatrix: LocationDistanceMatrix
    ): ResponseEntity<String>? {

        logMethod("Location.distanceMatrix")

        return try {
            externalPartnerService.getLocationDistanceMatrix(
                EPAuthRequest(request), locationDistanceMatrix)
        } catch (e: Exception) {
            log("epGetLocationDistanceMatrix - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @GetMapping(
        "/Loan.getDetails/{loanAccountNumber}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun epGetLoanDetail(
        request: HttpServletRequest,
        @PathVariable(LOAN_ACCOUNT_NUMBER) loanAccountNumber: String
    ): ResponseEntity<String>? {

        logMethod("Loan.getDetails/$loanAccountNumber")

        return try {
            externalPartnerService.getLoanDetail(
                EPAuthRequest(request), loanAccountNumber)
        } catch (e: Exception) {
            log("epGetLoanDetail - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }

    }

    @PostMapping(
        "/PropertyInsight.requestReport",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun epRequestPropertyInsightReport(
        request: HttpServletRequest,
        @RequestBody propertyInsightRequest: PropertyInsight
    ): ResponseEntity<String>? {

        logMethod("PropertyInsight.requestReport")

        return try {
            externalPartnerService.requestPropertyInsightReport(
                EPAuthRequest(request), propertyInsightRequest)
        } catch (e: Exception) {
            log("epRequestPropertyInsightReport - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @PostMapping(
        "/PropertyInsight.addDocument",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun epPropertyInsightAddDocument(
        request: HttpServletRequest,
        @RequestBody propertyInsightDocumentDTO: PropertyInsightDocumentDTO
    ): ResponseEntity<String>? {

        logMethod("PropertyInsight.addDocument")

        return try {

            externalPartnerService.addDocumentOnPropertyInsight(
                EPAuthRequest(request), propertyInsightDocumentDTO)

        } catch (e: Exception) {
            log("epPropertyInsightAddDocument - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }

    }

    @PostMapping(
        "/Location.directions",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun epGetLocationDirections(
        request: HttpServletRequest,
        @RequestBody locationDirections: LocationDirections
    ): ResponseEntity<String>? {

        logMethod("Location.directions")

        return try {

            externalPartnerService.getLocationDirections(
                EPAuthRequest(request), locationDirections)
        } catch (e: Exception) {
            log("epGetLocationDirections - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @GetMapping(
        "/Customer.LoanLookUp/{mobileNumber}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun epLoanLoopUp(
        request: HttpServletRequest,
        @PathVariable(MOBILE_NUMBER) mobileNumber: String
    ): ResponseEntity<String>? {

        logMethod("Customer.LoanLookUp/$mobileNumber")

        return try {
            customerService.loanLookUp(
                EPAuthRequest(request), mobileNumber)
        } catch (e: Exception) {
            log("epLoanLoopUp - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }

    }

    @GetMapping(
        "/Customer.LoanDetails/{loanAccountNumber}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun epLoanDetails(
        request: HttpServletRequest,
        @PathVariable(LOAN_ACCOUNT_NUMBER) loanAccountNumber: String
    ): ResponseEntity<String>? {

        logMethod("Customer.LoanDetails/$loanAccountNumber")

        return try {
            customerService.getLoanDetail(
                EPAuthRequest(request), loanAccountNumber)
        } catch (e: Exception) {
            log("epLoanDetails - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }

    }

    @PostMapping(
        "/Customer.createCase",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun epCreateCase(
        request: HttpServletRequest,
        @RequestBody serviceRequest: ServiceRequest
    ):ResponseEntity<String>? {

        logMethod("Customer.createCase")

        return try {

            customerService.createCase(
                EPAuthRequest(request), serviceRequest)

        } catch (e: Exception){
            log("epCreateCase - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @GetMapping(
        "/Customer.emiDueAmount/{loanAccountNumber}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun epEmiDueAmount(
        request: HttpServletRequest,
        @PathVariable(LOAN_ACCOUNT_NUMBER) loanAccountNumber: String
    ): ResponseEntity<String>? {

        logMethod("Customer.emiDueAmount/$loanAccountNumber")

        return try {
            customerService.getEmiDueAmount(
                EPAuthRequest(request), loanAccountNumber)
        } catch (e: Exception) {
            log("epEmiDueAmount - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }

    }

    @PostMapping(
        "/Customer.requestPaymentLink",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun epRequestPaymentLink(
        request: HttpServletRequest,
        @RequestBody paymentLinkDTO: PaymentLinkDTO
    ): ResponseEntity<String>? {

        logMethod("epRequestPaymentLink")

        return try {
            customerService.epRequestPaymentLink(
                EPAuthRequest(request), paymentLinkDTO)
        } catch (e: Exception) {
            log("epRequestPaymentLink - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }

    }

    @GetMapping(
        "/Lead.telephony.dialNumber/{mobileNumber}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun epLeadTelephonyDialNumber(
        request: HttpServletRequest,
        @PathVariable(MOBILE_NUMBER) mobileNumber: String,
    ): ResponseEntity<String>? {

        logMethod("Lead.telephony.dialNumber/$mobileNumber")

        return try {

            externalPartnerService.leadTelephonyDialNumber(
                EPAuthRequest(request), mobileNumber)

        } catch (e: Exception) {
            log("epLeadTelephonyDialNumber - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }

    }

}