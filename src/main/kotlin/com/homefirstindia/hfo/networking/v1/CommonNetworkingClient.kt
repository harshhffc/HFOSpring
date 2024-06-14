package com.homefirstindia.hfo.networking.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.homefirstindia.hfo.security.AppProperty
import com.homefirstindia.hfo.utils.Errors
import com.homefirstindia.hfo.utils.LocalHTTPResponse
import com.homefirstindia.hfo.utils.LoggerUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class CommonNetworkingClient(
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val appProperty: AppProperty
) {

    companion object {
        private const val TIMEOUT = 60
        private lateinit var client: OkHttpClient
    }

    enum class HttpMethods {
        GET, POST
    }

    private fun log(value: String) = LoggerUtils.log("v1/${this.javaClass.simpleName}.$value")
    private fun printLog(value: String) = LoggerUtils.printLog("v1/${this.javaClass.simpleName}.$value")

    init {
        client = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT.toLong(), TimeUnit.SECONDS)
            .readTimeout(TIMEOUT.toLong(), TimeUnit.SECONDS)
            .build()
    }

    inner class NewRequest {

        private lateinit var request : Request
        private var requestBuilder = Request.Builder()

        fun getCall(url: String): NewRequest {

            requestBuilder.method(HttpMethods.GET.name, null)
            requestBuilder.url(url)

            return this

        }

        fun postCall(url: String, requestJson: JSONObject): NewRequest {

            val mediaType = "application/json".toMediaTypeOrNull()
            val body = requestJson.toString().toRequestBody(mediaType)
            requestBuilder.method(HttpMethods.POST.name, body)
            requestBuilder.url(url)

            return this

        }

        fun postFormDataCall(url: String, requestBody: RequestBody): NewRequest {
            requestBuilder.url(url)
            requestBuilder.addHeader("Content-Type", "multipart/form-data")
            requestBuilder.post(requestBody)

            return this

        }

        fun urlEncodedCall(
            url: String,
            bodyString: String
        ): NewRequest {

//            val mediaType = "application/x-www-form-urlencoded".toMediaTypeOrNull()
//            val body = bodyString.toRequestBody(mediaType)
            requestBuilder.method(HttpMethods.GET.name,null)
//            requestBuilder.method(HttpMethods.POST.name, body)
            println("url=======${url+bodyString}")
            requestBuilder.url(url+bodyString)
            requestBuilder.addHeader("Content-Type", "application/x-www-form-urlencoded")

            return this

        }

        fun addHeader(key: String, value: String) : NewRequest {
            requestBuilder.addHeader(key, value)
            return this
        }

        fun send(): LocalHTTPResponse {

            request = requestBuilder.build()

            printLog("send - Request url: ${request.url}")
            printLog("send - Request headers: ${objectMapper.writeValueAsString(request.headers)}")
            printLog("send - Request body: ${request.body}")

            val localHTTPResponse = LocalHTTPResponse()

            val response = client.newCall(request).execute()
            val responseString = response.body!!.string()
            val responseCode = response.code
            response.body!!.close()
            response.close()

            printLog("send - Response code: $responseCode")
            printLog("send - Response body: $responseString")

            localHTTPResponse.statusCode = responseCode
            localHTTPResponse.stringEntity = responseString

            when (responseCode) {
                200 -> {
                    localHTTPResponse.isSuccess = true
                }
                401 -> {
                    localHTTPResponse.isSuccess = false
                    localHTTPResponse.errorMessage = Errors.UNAUTHORIZED_ACCESS.value
                }
                else -> {
                    localHTTPResponse.isSuccess = false
                    localHTTPResponse.errorMessage = responseString
                }
            }

            return localHTTPResponse

        }

    }

}