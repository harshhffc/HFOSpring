package com.homefirstindia.hfo.clients

import com.fasterxml.jackson.databind.ObjectMapper
import com.homefirstindia.hfo.manager.v1.CredsManager
import com.homefirstindia.hfo.manager.v1.EnCredType
import com.homefirstindia.hfo.manager.v1.EnPartnerName
import com.homefirstindia.hfo.model.v1.Creds
import com.homefirstindia.hfo.model.v1.MobileNumberValidation
import com.homefirstindia.hfo.networking.v1.CommonNetworkingClient
import com.homefirstindia.hfo.security.AppProperty
import com.homefirstindia.hfo.utils.*
import org.apache.commons.lang3.RandomStringUtils
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration


@Configuration
class DigitapClient(
    @Autowired val appProperty: AppProperty,
    @Autowired val cryptoUtils: CryptoUtils,
    @Autowired val credentialManager: CredsManager,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val commonNetworkingClient: CommonNetworkingClient,
    @Autowired val oneResponse: OneResponse
) {
    private var _digitapCred: Creds? = null

    private fun log(value: String) = LoggerUtils.log("DigitapClient.$value")

    @Throws(Exception::class)
    private fun digitapCred(): Creds? {
        if (null == _digitapCred) {
            _digitapCred = credentialManager.fetchCredentials(
                EnPartnerName.DIGITAP,
                if (cryptoUtils.appProperty.isProduction()) EnCredType.PRODUCTION else EnCredType.UAT
            )
        }
        return _digitapCred
    }

    enum class EnEndPointUrl(val value: String) {
        AADHAAR_VALIDATION("/validation/kyc/v1/aadhaar"),
        PAN_BASIC_VALIDATION("/validation/kyc/v1/pan_basic"),
        MOBILE_NUMBER_VALIDATION("/telecom/mobile_number_validation"),
        KYC_OCR_PAN("/ocr/v1/pan"),
        KYC_OCR_AADHAAR("/ocr/v1/aadhaar"),
        KYC_OCR_VOTER_ID("/ocr/v1/voter"),
        KYC_OCR_PASSPORT("/ocr/v1/passport");

        fun getFullApiUrl(baseUrl: String): String {
            return baseUrl + value
        }

    }

    @Throws(Exception::class)
    fun panValidation(panNumber: String): LocalResponse {

        val requestBody = JSONObject().apply {
            put("client_ref_num", "pv${RandomStringUtils.randomNumeric(8).uppercase()}")
            put("pan", panNumber)
        }

        val panValUrl = EnEndPointUrl.PAN_BASIC_VALIDATION.getFullApiUrl("${digitapCred()?.apiUrl}")

        val localHTTPResponse = commonNetworkingClient
            .NewRequest()
            .postCall(panValUrl, requestBody)
            .addHeader(CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
            .addHeader(AUTHORIZATION, cryptoUtils.generateBasicAuth(digitapCred()?.username!!,
                digitapCred()?.password!!))
            .send()

        log("panValidation - digitap response: ${localHTTPResponse.stringEntity}")

        val responseJson = JSONObject(localHTTPResponse.stringEntity)

        val localResponse =  LocalResponse().apply {
            message = localHTTPResponse.stringEntity
            isSuccess = responseJson.optInt("http_response_code", -1) == 200
        }

        return  localResponse

    }

    @Throws(Exception::class)
    fun aadhaarValidation(aadhaarNumber: String): LocalResponse {

        val requestBody = JSONObject().apply {
            put("client_ref_num", "av${RandomStringUtils.randomNumeric(8).uppercase()}")
            put("aadhaar", aadhaarNumber)
        }

        val aadhaarValUrl = EnEndPointUrl.AADHAAR_VALIDATION.getFullApiUrl("${digitapCred()?.apiUrl}")

        val localHTTPResponse = commonNetworkingClient
            .NewRequest()
            .postCall(aadhaarValUrl, requestBody)
            .addHeader(CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
            .addHeader(AUTHORIZATION, cryptoUtils.generateBasicAuth(digitapCred()?.username!!,
                digitapCred()?.password!!))
            .send()

        log("aadhaarValidation - digitap response: ${localHTTPResponse.stringEntity}")

        val responseJson = JSONObject(localHTTPResponse.stringEntity)

        val localResponse =  LocalResponse().apply {
            message = localHTTPResponse.stringEntity
            isSuccess = responseJson.optInt("http_response_code", -1) == 200
        }

        return localResponse


    }

    @Throws(Exception::class)
    fun sendOTP(eMobVal: MobileNumberValidation): LocalResponse {

        val requestBody = JSONObject().apply {
            put("purpose", "initiate_request")
            put("client_ref_num", eMobVal.referenceNumber)
            put("mobile_num", eMobVal.mobileNumber)
            put("txn_complete_cburl", if (appProperty.isProduction()) "https://one.homefirstindia.com:8443/hfo/public/v1/mobile.report"
            else "https://developer.homefirstindia.com:8443/hfo/public/v1/mobile.report")
        }

        val sendOtpUrl = EnEndPointUrl.MOBILE_NUMBER_VALIDATION.getFullApiUrl("${digitapCred()?.apiUrl}")

        val localHTTPResponse = commonNetworkingClient
            .NewRequest()
            .postCall(sendOtpUrl, requestBody)
            .addHeader(CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
            .addHeader(AUTHORIZATION, cryptoUtils.generateBasicAuth(digitapCred()?.username!!,
                digitapCred()?.password!!))
            .send()

        log("sendOTP - digitap response: ${localHTTPResponse.stringEntity}")

        val responseJson = JSONObject(localHTTPResponse.stringEntity)

        return LocalResponse().apply {
            message = localHTTPResponse.stringEntity
            isSuccess = responseJson.optString("status", NA) == SUCCESS
        }

    }

    @Throws(Exception::class)
    fun validateOTP(otp: String, txtId: String, token: String): LocalResponse {

        val requestBody = JSONObject().apply {
            put("purpose", "submit_otp")
            put("otp_value", otp)
            put("txn_id", txtId)
            put("token", token)
        }

        val mobValUrl = EnEndPointUrl.MOBILE_NUMBER_VALIDATION.getFullApiUrl("${digitapCred()?.apiUrl}")

        val localHTTPResponse = commonNetworkingClient
            .NewRequest()
            .postCall(mobValUrl, requestBody)
            .addHeader(CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
            .addHeader(AUTHORIZATION, cryptoUtils.generateBasicAuth(digitapCred()?.username!!
                ,digitapCred()?.password!!))
            .send()

        log("validateOTP - digitap response: ${localHTTPResponse.stringEntity}")

        val responseJson = JSONObject(localHTTPResponse.stringEntity)

        if (responseJson.optString("status", NA) != SUCCESS) {
            return LocalResponse().apply {
                message = localHTTPResponse.stringEntity
                isSuccess = false
            }
        }

        return getOtpStatus(txtId)

    }

    fun getOtpStatus(txtId: String): LocalResponse {

        val requestBody = JSONObject().apply {
            put("purpose", "get_status")
            put("txn_id", txtId)
        }

        val mobStatusUrl = EnEndPointUrl.MOBILE_NUMBER_VALIDATION.getFullApiUrl("${digitapCred()?.apiUrl}")

        val localHTTPResponse = commonNetworkingClient
            .NewRequest()
            .postCall(mobStatusUrl, requestBody)
            .addHeader(CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
            .addHeader(AUTHORIZATION, cryptoUtils.generateBasicAuth(digitapCred()?.username!!,digitapCred()?.password!!))
            .send()

        log("getOtpStatus - digitap response: ${localHTTPResponse.stringEntity}")

        val responseJson = JSONObject(localHTTPResponse.stringEntity)

        return LocalResponse().apply {
            message = localHTTPResponse.stringEntity
            isSuccess = responseJson.optString("status", NA) == SUCCESS
        }

    }

}