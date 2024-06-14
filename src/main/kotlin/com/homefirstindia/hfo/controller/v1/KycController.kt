package com.homefirstindia.hfo.controller.v1

import com.homefirstindia.hfo.dto.v1.OTPVerificationDTO
import com.homefirstindia.hfo.dto.v1.externalpartner.EPAuthRequest
import com.homefirstindia.hfo.service.v1.KycService
import com.homefirstindia.hfo.utils.LoggerUtils
import com.homefirstindia.hfo.utils.OneResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/kyc/v1")
class KycController(
    @Autowired val kycService: KycService,
    @Autowired val oneResponse: OneResponse
) {
    private fun logMethod(value: String) = LoggerUtils.logMethodCall("/ds/v1/$value")

    private fun log(value: String) = LoggerUtils.log("v1/${this.javaClass.simpleName}.$value")

    @GetMapping(
        "/validate.pan/{panNumber}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun kcValidatePan(
        request: HttpServletRequest,
        @PathVariable("panNumber") panNumber: String
    ): ResponseEntity<String>? {

        logMethod("kcValidatePan")

        return try {
            kycService.validatePan(EPAuthRequest(request), panNumber)
        } catch (e: Exception) {
            log("kcValidatePan - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @GetMapping(
        "/validate.aadhaar/{aadhaarNumber}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun kcValidateAadhaar(
        request: HttpServletRequest,
        @PathVariable("aadhaarNumber") panNumber: String
    ): ResponseEntity<String>? {

        logMethod("kcValidateAadhaar")

        return try {
            kycService.validateAadhaar(EPAuthRequest(request), panNumber)
        } catch (e: Exception) {
            log("kcValidateAadhaar - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @GetMapping(
        "/mobile.generateOtp",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun kcGenerateOtp(
        request: HttpServletRequest,
        @RequestParam("source") source: String,
        @RequestParam("mobileNumber") mobileNumber: String
    ): ResponseEntity<String>? {

        logMethod("kcGenerateOtp")

        return try {
            kycService.generateOTP(EPAuthRequest(request), mobileNumber, source)
        } catch (e: Exception) {
            log("kcGenerateOtp - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @PostMapping(
        "/mobile.verifyOtp",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun kcVerifyOtp(
        request: HttpServletRequest,
        @RequestBody otpVerificationDTO: OTPVerificationDTO
    ): ResponseEntity<String>? {

        logMethod("kcVerifyOtp")
        return try {
            kycService.verifyOTP(EPAuthRequest(request), otpVerificationDTO)
        } catch (e: Exception) {
            log("kcVerifyOtp - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }
}