package com.homefirstindia.hfo.networking.v1

import com.homefirstindia.hfo.model.v1.Creds
import com.homefirstindia.hfo.repository.v1.CredsRepository
import com.homefirstindia.hfo.security.AppProperty
import com.homefirstindia.hfo.utils.*
import okhttp3.RequestBody
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class LMSNetworkingClient  (
    @Autowired val appProperty: AppProperty,
    @Autowired val cryptoUtils: CryptoUtils,
    @Autowired val credsRepo: CredsRepository,
    @Autowired var commonNetworkingClient: CommonNetworkingClient
) {

    private fun log(value: String) = LoggerUtils.log("v1/${this.javaClass.simpleName}.$value")
    private var _creds: Creds? = null

    companion object {
        private var SESSION_PASSCODE: String? = null
    }

    private var retryCount = 0

    enum class Endpoints(val value: String) {
        AUTHENTICATE("/v1/ep/authenticate"),
        LEAD_CAPTURE_NOTIFY("/v1/ep/Lead.captureAndNotifyOwner/")
    }

    private fun newApiRequest(): CommonNetworkingClient.NewRequest {
        return commonNetworkingClient
            .NewRequest()
            .addHeader(
                "Authorization",
                "Basic " + cryptoUtils.encodeBase64("${lmsCreds().username}:${lmsCreds().password}")
            )
            .addHeader("orgId", lmsCreds().memberPasscode!!)
            .addHeader("sessionPasscode", SESSION_PASSCODE ?: NA)
    }

    @Throws(Exception::class)
    fun get(url: String): LocalHTTPResponse {

        authenticateCASClient()

        val fullUrl = lmsCreds().apiUrl + url

        val localHTTPResponse = newApiRequest()
            .getCall(fullUrl)
            .send()

        if (localHTTPResponse.statusCode == 401) {

            if (retryCount < 3) {

                retryCount++
                reAuthenticate()
                return get(url)

            } else retryCount = 0
        }

        return localHTTPResponse

    }


    @Throws(Exception::class)
    fun post(url: String, requestJson: JSONObject): LocalHTTPResponse {

        authenticateCASClient()

        val fullUrl = "${lmsCreds().apiUrl}${url}"

        val localHTTPResponse = newApiRequest()
            .postCall(fullUrl, requestJson)
            .send()

        if (localHTTPResponse.statusCode == 401) {

            if (retryCount < 3) {

                retryCount++
                reAuthenticate()
                return post(fullUrl, requestJson)

            } else retryCount = 0
        }

        return localHTTPResponse

    }



    @Throws(Exception::class)
    private fun authenticateCASClient() {

        try {

            lmsCreds()

            if (isNotNullOrNA(SESSION_PASSCODE)) return

            val fullUrl = "${lmsCreds().apiUrl}${Endpoints.AUTHENTICATE.value}"

            val hfoResponse = newApiRequest().getCall(fullUrl).send()

            log("HFO response code: ${hfoResponse.statusCode} body: ${hfoResponse.stringEntity}")

            val lsJsonResponse = JSONObject(hfoResponse.stringEntity)

            when (hfoResponse.statusCode) {
                200 -> {
                    log("HFO Client authorized successfully.")
                    SESSION_PASSCODE = lsJsonResponse.getString("sessionPasscode")
                }
                401 -> {
                    log("Unauthorized access while authenticateCPClient.")
                    throw Exception("Unauthorized access while authenticateCPClient.")
                }
                else -> {
                    val errorMessage = lsJsonResponse.optString(MESSAGE, "Error while authenticateCPClient.")
                    log("Error while authenticateCPClient: $errorMessage")
                    throw Exception(errorMessage)
                }
            }
        } catch (e: Exception) {
            log("Error while authenticateHFOClient: " + e.message)
            e.printStackTrace()
            throw e
        }
    }

    @Throws(Exception::class)
    private fun lmsCreds(): Creds {
        if (null == _creds || null == SESSION_PASSCODE) {

            _creds = credsRepo.findByPartnerNameAndCredType(
                PARTNER_HOMEFIRST_LMS,
                if (appProperty.isProduction()) CredType.PRODUCTION.value else CredType.UAT.value
            )
            if (null == _creds) {
                log("lmsCreds - failed to get HomefirstLMS Creds from DB.")
                throw Exception("failed to get HomefirstLMS Creds from DB.")
            }
        }
        return _creds!!
    }

    @Throws(Exception::class)
    private fun reAuthenticate() {
        SESSION_PASSCODE = null
        authenticateCASClient()
    }


    @Throws(Exception::class)
    fun postFormData(url: String, requestBody: RequestBody): LocalHTTPResponse {

        authenticateCASClient()

        val fullUrl = "${lmsCreds().apiUrl}${url}"

        val localHTTPResponse = newApiRequest()
            .postFormDataCall(fullUrl, requestBody)
            .send()

        if (localHTTPResponse.statusCode == 401) {

            if (retryCount < 3) {

                retryCount++
                reAuthenticate()
                return postFormData(fullUrl, requestBody)

            } else retryCount = 0
        }

        return localHTTPResponse

    }



}
