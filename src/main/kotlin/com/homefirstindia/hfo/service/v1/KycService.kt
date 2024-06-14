package com.homefirstindia.hfo.service.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.homefirstindia.hfo.clients.DigitapClient
import com.homefirstindia.hfo.dto.v1.OTPVerificationDTO
import com.homefirstindia.hfo.dto.v1.externalpartner.EPAuthRequest
import com.homefirstindia.hfo.helper.v1.PartnerLogHelper
import com.homefirstindia.hfo.helper.v1.UserActionStatus
import com.homefirstindia.hfo.manager.v1.ExternalServiceManager
import com.homefirstindia.hfo.model.v1.KYCDocument
import com.homefirstindia.hfo.model.v1.MobileNumberValidation
import com.homefirstindia.hfo.repository.v1.DocumentRepositoryMaster
import com.homefirstindia.hfo.repository.v1.ExternalServiceLogRepository
import com.homefirstindia.hfo.repository.v1.MobileNumberValidationRepository
import com.homefirstindia.hfo.security.AppProperty
import com.homefirstindia.hfo.utils.*
import org.apache.commons.lang3.RandomStringUtils
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.util.*


@Service
class KycService(
    @Autowired val mobileNumberValidationRepository: MobileNumberValidationRepository,
    @Autowired val oneResponse: OneResponse,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val partnerLogHelper: PartnerLogHelper,
    @Autowired val documentRepositoryMaster: DocumentRepositoryMaster,
    @Autowired val cryptoUtils: CryptoUtils,
    @Autowired val appProperty: AppProperty,
    @Autowired val externalServiceLogRepository: ExternalServiceLogRepository,
    @Autowired val digitapClient: DigitapClient,
    @Autowired val externalServiceManager: ExternalServiceManager
) {
    private fun log(value: String) = LoggerUtils.log("v1/${this.javaClass.simpleName}.$value")

    @Throws(Exception::class)
    fun validatePan(
        epAuthRequest: EPAuthRequest,
        panNumber: String
    ): ResponseEntity<String>? {

        val epLogger = partnerLogHelper.Builder(epAuthRequest)

        if (panNumber.isInvalid()) {
            val msg = "Invalid Pan Number"
            epLogger.setRequestStatus(UserActionStatus.FAILURE)
                .setResponseStatus(201)
                .setServiceName(object {}.javaClass.enclosingMethod.name)
                .setRequestDesc(msg)
                .log()
            return oneResponse.invalidData(msg)
        }

        val eServiceLog = externalServiceManager.logDigitapService(
            DigitapClient.EnEndPointUrl.PAN_BASIC_VALIDATION.value
        )

        eServiceLog ?: run {
            log("validatePan - Failed to add external service log in db")
            val msg = "Failed to validate pan"
            epLogger.setServiceName(object {}.javaClass.enclosingMethod.name)
                .setRequestStatus(UserActionStatus.FAILURE).setResponseStatus(201)
                .setRequestDesc(msg)
                .log()
            return oneResponse.operationFailedResponse(msg)
        }

        val lResponse = digitapClient.panValidation(panNumber)

        val responseJson = JSONObject(lResponse.message)

        eServiceLog.let {
            it.responseCode = responseJson.optInt("http_response_code", -1).toString()
            it.orgId = epAuthRequest.orgId
            it.serviceType = EnExternalServiceType.VALIDATE.value
            it.status = if (lResponse.isSuccess) EnUserRequestStatus.SUCCESS.value
            else EnUserRequestStatus.FAILED.value
            it.updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
        }

        externalServiceLogRepository.save(eServiceLog)

        if (!lResponse.isSuccess) {
            val msg = "Failed to validate pan"
            epLogger.setRequestStatus(UserActionStatus.FAILURE)
                .setResponseStatus(201)
                .setServiceName(object {}.javaClass.enclosingMethod.name)
                .setRequestDesc(msg)
                .log()
            return oneResponse.operationFailedResponse(msg)
        }

        val nameOnPan = responseJson.optJSONObject("result")?.optString("name", NA)

        if (nameOnPan.isInvalid()) {
            val msg = "Failed to validate pan"
            epLogger.setRequestStatus(UserActionStatus.FAILURE).setResponseStatus(201)
                .setServiceName(object {}.javaClass.enclosingMethod.name).setRequestDesc(msg).log()
            return oneResponse.operationFailedResponse(msg)
        }

        var kycDocument = KYCDocument()

        documentRepositoryMaster.kycDocumentRepository.findByDocumentIdAndDocumentType(
           encryptAnyKey(panNumber), EnDocumentType.PAN.value
        )?.let {
            kycDocument = it
        } ?: run {
            kycDocument.apply {
                orgId = epAuthRequest.orgId
                documentId = encryptAnyKey(panNumber)
                documentType = EnDocumentType.PAN.value
            }
        }

        kycDocument.userName = nameOnPan
        kycDocument.isValidated = true
        kycDocument.validationData = responseJson.toString()
        kycDocument.isVerified = true
        kycDocument.updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()

        documentRepositoryMaster.kycDocumentRepository.save(kycDocument)

        eServiceLog.apply {
            objectId = kycDocument.id
            objectName = MyObject.KYC_DOCUMENT.value
            updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
        }

        externalServiceLogRepository.save(eServiceLog)

        epLogger.setServiceName(object {}.javaClass.enclosingMethod.name)
            .setResponseStatus(200)
            .setRequestStatus(UserActionStatus.SUCCESS)
            .log()

        kycDocument.documentId = decryptAnyKey(kycDocument.documentId!!)

        return oneResponse.getSuccessResponse(
            JSONObject(objectMapper.writeValueAsString(kycDocument))
        )
    }

    @Throws(Exception::class)
    fun validateAadhaar(
        epAuthRequest: EPAuthRequest,
        aadhaarNumber: String
    ): ResponseEntity<String>? {

        val epLogger = partnerLogHelper.Builder(epAuthRequest)

        if (aadhaarNumber.isInvalid()) {
            val msg = "Invalid Aadhaar Number"
            epLogger.setRequestStatus(UserActionStatus.FAILURE)
                .setResponseStatus(201)
                .setServiceName(object {}.javaClass.enclosingMethod.name)
                .setRequestDesc(msg)
                .log()
            return oneResponse.invalidData(msg)
        }

        val eServiceLog = externalServiceManager.logDigitapService(
            DigitapClient.EnEndPointUrl.PAN_BASIC_VALIDATION.value
        )

        eServiceLog ?: run {
            log("validateAadhaar - Failed to add external service log in db")
            val msg = "Failed to validate aadhaar"
            epLogger.setServiceName(object {}.javaClass.enclosingMethod.name)
                .setRequestStatus(UserActionStatus.FAILURE).setResponseStatus(201)
                .setRequestDesc(msg)
                .log()
            return oneResponse.operationFailedResponse(msg)
        }

        val lResponse = digitapClient.aadhaarValidation(aadhaarNumber)

        val responseJson = JSONObject(lResponse.message)

        eServiceLog.let {
            it.responseCode = responseJson.optInt("http_response_code", -1).toString()
            it.orgId = epAuthRequest.orgId
            it.serviceType = EnExternalServiceType.VALIDATE.value
            it.status = if (lResponse.isSuccess) EnUserRequestStatus.SUCCESS.value
            else EnUserRequestStatus.FAILED.value
            it.updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
        }

        externalServiceLogRepository.save(eServiceLog)

        if (!lResponse.isSuccess) {
            val msg = "Failed to validate aadhaar"
            epLogger.setRequestStatus(UserActionStatus.FAILURE)
                .setResponseStatus(201)
                .setServiceName(object {}.javaClass.enclosingMethod.name)
                .setRequestDesc(msg)
                .log()
            return oneResponse.operationFailedResponse(msg)
        }

        val gender = responseJson.optJSONObject("result")?.optString("aadhaar_gender", NA)

        if (gender.isInvalid()) {
            val msg = "Failed to validate aadhaar "
            epLogger.setRequestStatus(UserActionStatus.FAILURE).setResponseStatus(201)
                .setServiceName(object {}.javaClass.enclosingMethod.name).setRequestDesc(msg).log()
            return oneResponse.operationFailedResponse(msg)
        }

        var kycDocument = KYCDocument()

        documentRepositoryMaster.kycDocumentRepository.findByDocumentIdAndDocumentType(
            encryptAnyKey(aadhaarNumber), EnDocumentType.AADHAAR.value
        )?.let {
            kycDocument = it
        } ?: run {
            kycDocument.apply {
                orgId = epAuthRequest.orgId
                documentId = encryptAnyKey(aadhaarNumber)
                documentType = EnDocumentType.PAN.value
            }
        }

        kycDocument.userGender = gender
        kycDocument.isValidated = true
        kycDocument.validationData = responseJson.toString()
        kycDocument.isVerified = true
        kycDocument.updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()

        documentRepositoryMaster.kycDocumentRepository.save(kycDocument)

        eServiceLog.apply {
            objectId = kycDocument.id
            objectName = MyObject.KYC_DOCUMENT.value
            updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
        }

        externalServiceLogRepository.save(eServiceLog)

        epLogger.setServiceName(object {}.javaClass.enclosingMethod.name)
            .setResponseStatus(200)
            .setRequestStatus(UserActionStatus.SUCCESS)
            .log()

        kycDocument.documentId = decryptAnyKey(kycDocument.documentId!!)

        return oneResponse.getSuccessResponse(
            JSONObject(objectMapper.writeValueAsString(kycDocument))
        )

    }

    @Throws(Exception::class)
    fun generateOTP(
        epAuthRequest: EPAuthRequest,
        mobileNumber: String,
        source: String
    ): ResponseEntity<String>? {

        val epLogger = partnerLogHelper.Builder(epAuthRequest)

        if (mobileNumber.isInvalid() && source.isInvalid()) {
            val msg = "Invalid mobile number or source"
            epLogger.setRequestStatus(UserActionStatus.FAILURE)
                .setResponseStatus(201)
                .setServiceName(object {}.javaClass.enclosingMethod.name)
                .setRequestDesc(msg)
                .log()
            return oneResponse.invalidData(msg)
        }

        val eServiceLog = externalServiceManager.logDigitapService(
            DigitapClient.EnEndPointUrl.MOBILE_NUMBER_VALIDATION.value
        )

        eServiceLog ?: run {
            log("generateOTP - Failed to add external service log in db")
            val msg = "Failed to generate OTP"
            epLogger.setServiceName(object {}.javaClass.enclosingMethod.name)
                .setRequestStatus(UserActionStatus.FAILURE).setResponseStatus(201)
                .setRequestDesc(msg)
                .log()
            return oneResponse.operationFailedResponse(msg)
        }

        val nMobNumValidation = MobileNumberValidation().apply {
            this.mobileNumber = mobileNumber
            this.source = source
            this.referenceNumber = "so${RandomStringUtils.randomNumeric(8).uppercase()}"
        }

        val lResponse = digitapClient.sendOTP(nMobNumValidation)

        val responseJson = JSONObject(lResponse.message)

        eServiceLog.let {
            it.orgId = epAuthRequest.orgId
            it.serviceType = EnExternalServiceType.VALIDATE.value
            it.status = if (lResponse.isSuccess) EnUserRequestStatus.SUCCESS.value
            else EnUserRequestStatus.FAILED.value
            it.updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
        }

        externalServiceLogRepository.save(eServiceLog)

        if (!lResponse.isSuccess) {
            val msg = "Failed to generate OTP"
            epLogger.setRequestStatus(UserActionStatus.FAILURE)
                .setResponseStatus(201)
                .setServiceName(object {}.javaClass.enclosingMethod.name)
                .setRequestDesc(msg)
                .log()
            return oneResponse.operationFailedResponse(msg)
        }

        val rToken = responseJson.optString("token", NA)
        val rTxnId = responseJson.optString("txn_id", NA)

        if (rToken.isInvalid() && rTxnId.isInvalid()) {
            val msg = "Failed to generate OTP"
            log("generateOTP - Invalid token and txnId for mobile number: $mobileNumber")
            epLogger.setRequestStatus(UserActionStatus.FAILURE).setResponseStatus(201)
                .setServiceName(object {}.javaClass.enclosingMethod.name).setRequestDesc(msg).log()
            return oneResponse.operationFailedResponse(msg)
        }

        nMobNumValidation.apply {
            generateOtpResponse = responseJson.toString()
            token = rToken
            transactionId = rTxnId
        }

        mobileNumberValidationRepository.save(nMobNumValidation)

        eServiceLog.apply {
            objectId = nMobNumValidation.id
            objectName = MyObject.MOBILE_NUMBER_LOOKUP.value
            updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
        }

        externalServiceLogRepository.save(eServiceLog)

        log("generateOTP - OTP sent successfully!")

        epLogger.setServiceName(object {}.javaClass.enclosingMethod.name)
            .setResponseStatus(200)
            .setRequestStatus(UserActionStatus.SUCCESS)
            .log()

        return oneResponse.getSuccessResponse(JSONObject()
            .put(STATUS, SUCCESS)
            .put(MESSAGE, "OTP sent successfully")
        )

    }

    @Throws(Exception::class)
    fun verifyOTP(
        epAuthRequest: EPAuthRequest,
        otpVerificationDTO: OTPVerificationDTO
    ): ResponseEntity<String>? {

        val epLogger = partnerLogHelper.Builder(epAuthRequest)

        otpVerificationDTO.mandatoryFieldsCheck().let {
            if (!it.isSuccess) {
                log("verifyOTP - Error : ${it.message}")
                epLogger.setResponseStatus(201).setRequestStatus(UserActionStatus.FAILURE)
                    .setRequestDesc(it.message).log()
                return oneResponse.getFailureResponse(it.toJson())
            }
        }

        val eMobNumValidation = mobileNumberValidationRepository.findFirstByMobileNumberOrderByCreateDatetimeDesc(
            otpVerificationDTO.mobileNumber)

        eMobNumValidation ?: run {

            log("verifyOTP - OTP details not found")
            val msg = "Failed to verify OTP"
            epLogger.setRequestStatus(UserActionStatus.FAILURE).setResponseStatus(201)
                .setServiceName(object {}.javaClass.enclosingMethod.name).setRequestDesc(msg).log()
            return oneResponse.operationFailedResponse(msg)

        }

        val eServiceLog = externalServiceManager.logDigitapService(
            DigitapClient.EnEndPointUrl.MOBILE_NUMBER_VALIDATION.value
        )

        eServiceLog ?: run {
            log("verifyOTP - Failed to add external service log in db")
            val msg = "Failed to verify OTP"
            epLogger.setServiceName(object {}.javaClass.enclosingMethod.name)
                .setRequestStatus(UserActionStatus.FAILURE).setResponseStatus(201)
                .setRequestDesc(msg)
                .log()
            return oneResponse.operationFailedResponse(msg)
        }

        val lResponse = digitapClient.validateOTP(otpVerificationDTO.otp!!,
            eMobNumValidation.transactionId!!, eMobNumValidation.token!!)

        val responseJson = JSONObject(lResponse.message)

        eMobNumValidation.verifyOtpResponse = responseJson.toString()
        mobileNumberValidationRepository.save(eMobNumValidation)

        eServiceLog.let {
            it.orgId = epAuthRequest.orgId
            it.serviceType = EnExternalServiceType.VALIDATE.value
            it.status = if (lResponse.isSuccess) EnUserRequestStatus.SUCCESS.value
            else EnUserRequestStatus.FAILED.value
            it.updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
        }

        externalServiceLogRepository.save(eServiceLog)

        if (!lResponse.isSuccess) {
            val msg = "Failed to verify OTP"
            epLogger.setRequestStatus(UserActionStatus.FAILURE)
                .setResponseStatus(201)
                .setServiceName(object {}.javaClass.enclosingMethod.name)
                .setRequestDesc(msg)
                .log()
            return oneResponse.operationFailedResponse(msg)
        }


        eMobNumValidation.isValidated = true
        mobileNumberValidationRepository.save(eMobNumValidation)

        eServiceLog.apply {
            objectId = eMobNumValidation.id
            objectName = MyObject.MOBILE_NUMBER_LOOKUP.value
            updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
        }

        externalServiceLogRepository.save(eServiceLog)

        log("verifyOTP - OTP validation successfully!")

        epLogger.setServiceName(object {}.javaClass.enclosingMethod.name).setResponseStatus(200)
            .setRequestStatus(UserActionStatus.SUCCESS).log()

        return oneResponse.getSuccessResponse(JSONObject()
            .put(STATUS, SUCCESS)
            .put(MESSAGE, "OTP verified successfully")
        )

    }

}