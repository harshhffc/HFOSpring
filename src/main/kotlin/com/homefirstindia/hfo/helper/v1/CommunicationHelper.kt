package com.homefirstindia.hfo.helper.v1

import com.homefirstindia.hfo.manager.v1.CredsManager
import com.homefirstindia.hfo.manager.v1.EnCredType
import com.homefirstindia.hfo.manager.v1.EnPartnerName
import com.homefirstindia.hfo.model.v1.*
import com.homefirstindia.hfo.networking.v1.CommonNetworkingClient
import com.homefirstindia.hfo.security.AppProperty
import com.homefirstindia.hfo.utils.*
import okhttp3.MultipartBody
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*
import java.util.function.Consumer
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root


@Component
class CommunicationHelper(
    @Autowired private val credsManager: CredsManager,
    @Autowired private val commonNetworkingClient: CommonNetworkingClient,
    @Autowired private val appProperty: AppProperty,
) {

    private fun log(value: String) = LoggerUtils.log("v1/${this.javaClass.simpleName}.$value")

    private fun printLog(value: String) = LoggerUtils.printLog("v1/${this.javaClass.simpleName}.$value")

    companion object {
        const val SMS_API_METHOD = "sms.json"
        const val KALEYRA_SMS_CALLBACK_PARAMS =
            "status={status}&sid={sid}&custom={custom}&custom1={custom1}&senttime={senttime}" +
                    "&delivered={delivered}&mobile={mobile}"
        const val VOICE_CALL_API_METHOD = "dial.click2call"
        const val KALEYRA_VOICE_CALLBACK_PARAMS = "id={id}&caller={caller}" +
                "&receiver={receiver}&durationInSec={duration}&callerStatus={status1}&receiverStatus={status2}" +
                "&recordpath={recordpath}&callStartTime={starttime}&callEndTime={endtime}& status={status}" +
                "&callerId={callerid}&callerProvider={provider}&callerLocation={location}"
    }

    private var _kaleyraCred: Creds? = null
    private var _callingCred: Creds? = null

    @Throws(Exception::class)
    private fun kaleyraSmsCred(): Creds {
        if (_kaleyraCred == null) {
            _kaleyraCred = credsManager.fetchCredentials(
                EnPartnerName.KALEYRA_SMS,
                if (appProperty.isProduction()) EnCredType.PRODUCTION else EnCredType.UAT
            )
            _kaleyraCred ?: throw Exception("Failed to get kaleyra credentials ")
        }
        return _kaleyraCred!!
    }

    @Throws(Exception::class)
    private fun kaleyraCallingCred(): Creds {
        if (_callingCred == null) {
            _callingCred = credsManager.fetchCredentials(
                EnPartnerName.KALEYRA,
                if (appProperty.isProduction()) EnCredType.PRODUCTION else EnCredType.UAT
            )
            _callingCred ?: throw Exception("Failed to get kaleyra credentials ")
        }
        return _callingCred!!
    }


    @Throws(Exception::class)
    fun sendBulkSMS(
        smses: MutableList<SMSBody>,
        approvedTemplate: SMSTemplate,
    ): LocalResponse {


        val smsJsonRequest = JSONObject()
        smsJsonRequest.put("sender", approvedTemplate.senderId)
        smsJsonRequest.put("entity_id", approvedTemplate.entityId)
        smsJsonRequest.put("template_id", approvedTemplate.templateId)
        smsJsonRequest.put("unicode", approvedTemplate.isUnicode) //TODO: check for multilingual
        smsJsonRequest.put("flash", 0)
        smsJsonRequest.put("sms", smses)
        smsJsonRequest.put(
            "dlrurl", appProperty.smsDispositionURL +
                    KALEYRA_SMS_CALLBACK_PARAMS
        )


        val requestBody = MultipartBody.Builder()
        requestBody.setType(MultipartBody.FORM)

        requestBody.addFormDataPart("method", SMS_API_METHOD)
        requestBody.addFormDataPart("json", smsJsonRequest.toString())

        kaleyraSmsCred().let {
            requestBody.addFormDataPart("api_key", it.apiKey!!)
        }

        val response = commonNetworkingClient
            .NewRequest()
            .postFormDataCall(
                _kaleyraCred?.apiUrl!!,
                requestBody.build()
            ).send()

        val lResponse = LocalResponse()

        if (response.isSuccess) {

            val responseJson = JSONObject(response.stringEntity)

            if (responseJson.getString("status").equals(OK, true)) {

                lResponse.apply {
                    isSuccess = true
                    this.response = responseJson.toString()
                }

            } else {

                val gError = responseJson.getString("message")
                log("sendBulkSMS - Failure: $gError")

                lResponse.apply {
                    message = gError
                    isSuccess = false
                    this.response = responseJson.toString()

                }

            }

        } else {

            log("sendBulkSMS - Error: ${response.errorMessage}")

            lResponse.apply {
                message = response.errorMessage
                isSuccess = false
            }

        }

        return lResponse

    }


    @Throws(Exception::class)
    fun clickToCall(
        callRequest: CallLog,
        callingCred: CallingInfo,
    ): LocalResponse {

        val requestBody = MultipartBody.Builder()
        requestBody.setType(MultipartBody.FORM)

        requestBody
            .addFormDataPart("method", VOICE_CALL_API_METHOD)
            .addFormDataPart("caller", callRequest.caller!!)
            .addFormDataPart("receiver", callRequest.receiver!!)
            .addFormDataPart("caller_id", callingCred.callerId!!)
            .addFormDataPart("callback", "${appProperty.callDispositionURL}$KALEYRA_VOICE_CALLBACK_PARAMS")
            .addFormDataPart("format", "json")
            .addFormDataPart("return", "1")

        val response = commonNetworkingClient
            .NewRequest()
            .addHeader(X_API_KEY, callingCred.apiKey!!)
            .postFormDataCall(
                kaleyraCallingCred().apiUrl!!,
                requestBody.build()
            ).send()

        if (!response.isSuccess) {

            log("clickToCall - Failure: ${response.errorMessage}")

            return LocalResponse().apply {
                message = response.errorMessage
            }

        }

        val responseJson = JSONObject(response.stringEntity)

        if (responseJson.getString("status").equals(STATUS_CODE_200, true)) {
            return LocalResponse().apply {
                isSuccess = true
                this.response = responseJson.toString()
            }
        }

        val eMessage = responseJson.getString(MESSAGE)
        log("clickToCall - Failure: $eMessage")

        return LocalResponse().apply {
            message = eMessage
        }

    }
}

class CommunicationSearchQueryCriteria(
    var predicate: Predicate?,
    val builder: CriteriaBuilder,
    private val root: Root<*>,
    private val groupCondition: String,
    ) : Consumer<RowCondition> {

    private val nullFieldValues = arrayListOf(" ", "", "NA", "null", "0")

    override fun accept(param: RowCondition) {

        predicate = when (AdvancedFilterOperator[param.op!!]) {
            AdvancedFilterOperator.EQUALS -> {

                if (groupCondition == "And") {
                    builder.and(predicate, root.get<CallLog>(param.lso).`in`(param.rso))
                } else {
                    builder.or(predicate, root.get<CallLog>(param.lso).`in`(param.rso))
                }
            }

            AdvancedFilterOperator.NOT_EQ -> {

                if (groupCondition == "And") {
                    builder.and(predicate, builder.not(root.get<CallLog>(param.lso)!!.`in`(param.rso)))
                } else {
                    builder.or(predicate, builder.not(root.get<CallLog>(param.lso)!!.`in`(param.rso)))
                }
            }

            AdvancedFilterOperator.CONTAINS -> {
                if (groupCondition == "And") {
                    builder.and(predicate, builder.not(root.get<CallLog>(param.lso)!!.`in`(param.rso)))
                } else {
                    builder.or(predicate, builder.not(root.get<CallLog>(param.lso)!!.`in`(param.rso)))
                }
            }

            AdvancedFilterOperator.DOES_NOT_CONTAIN -> {
                if (groupCondition == "And") {
                    builder.and(predicate, builder.notLike(root.get(param.lso), "%" + param.rso?.get(0) + "%"))
                } else {
                    builder.or(predicate, builder.notLike(root.get(param.lso), "%" + param.rso?.get(0) + "%"))
                }
            }

            AdvancedFilterOperator.ENDS_WITH -> {
                if (groupCondition == "And") {
                    builder.and(predicate, builder.like(root.get(param.lso), "%" + (param.rso?.get(0))))
                } else {
                    builder.or(predicate, builder.like(root.get(param.lso), "%" + (param.rso?.get(0))))
                }
            }

            AdvancedFilterOperator.STARTS_WITH -> {
                if (groupCondition == "And") {
                    builder.and(predicate, builder.like(root.get(param.lso), (param.rso?.get(0)) + "%"))
                } else {
                    builder.or(predicate, builder.like(root.get(param.lso), (param.rso?.get(0)) + "%"))
                }
            }

            AdvancedFilterOperator.GREATER_THAN -> {
                if (groupCondition == "And") {
                    builder.and(predicate, builder.greaterThan(root.get(param.lso), (param.rso?.get(0))!!))
                } else {
                    builder.or(predicate, builder.greaterThan(root.get(param.lso), (param.rso?.get(0))!!))
                }
            }

            AdvancedFilterOperator.LESS_THAN -> {
                if (groupCondition == "And") {
                    builder.and(predicate, builder.lessThan(root.get(param.lso), (param.rso?.get(0))!!))
                } else {
                    builder.or(predicate, builder.lessThan(root.get(param.lso), (param.rso?.get(0))!!))
                }
            }

            AdvancedFilterOperator.BETWEEN -> {
                if (groupCondition == "And") {
                    builder.and(
                        predicate,
                        builder.between(root.get(param.lso), (param.rso?.get(0))!!, (param.rso?.get(1)!!))
                    )
                } else {
                    builder.or(
                        predicate,
                        builder.between(root.get(param.lso), (param.rso?.get(0))!!, (param.rso?.get(1)!!))
                    )
                }
            }

            AdvancedFilterOperator.NULL -> {
                val leadNullCaseOne = root.get<CallLog>(param.lso).isNull
                val leadNullCaseTwo = root.get<CallLog?>(param.lso).`in`(nullFieldValues)
                if (groupCondition == "And") {
                    builder.and(predicate, builder.or(leadNullCaseOne, leadNullCaseTwo))
                } else {
                    builder.or(predicate, builder.or(leadNullCaseOne, leadNullCaseTwo))
                }
            }

            AdvancedFilterOperator.NOT_NULL -> {
                val sfCollectionNullCaseOne = root.get<CallLog>(param.lso).isNotNull
                val sfCollectionCaseTwo = root.get<CallLog>(param.lso).`in`(nullFieldValues).not()
                if (groupCondition == "And") {
                    builder.and(predicate, builder.and(sfCollectionNullCaseOne, sfCollectionCaseTwo))
                } else {
                    builder.or(predicate, builder.and(sfCollectionNullCaseOne, sfCollectionCaseTwo))
                }
            }

            else -> null
        }

    }


}