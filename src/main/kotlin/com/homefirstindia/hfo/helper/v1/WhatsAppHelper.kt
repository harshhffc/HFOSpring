package com.homefirstindia.hfo.helper.v1

import com.homefirstindia.hfo.dto.v1.MessageDTO
import com.homefirstindia.hfo.manager.v1.CredsManager
import com.homefirstindia.hfo.manager.v1.EnCredType
import com.homefirstindia.hfo.manager.v1.EnPartnerName
import com.homefirstindia.hfo.model.v1.Creds
import com.homefirstindia.hfo.networking.v1.CommonNetworkingClient
import com.homefirstindia.hfo.repository.v1.WhatsAppAvailabilityRepository
import com.homefirstindia.hfo.security.AppProperty
import com.homefirstindia.hfo.utils.*
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Component
class WhatsAppHelper(
    @Autowired private val credsManager: CredsManager,
    @Autowired private val commonNetworkingClient: CommonNetworkingClient,
    @Autowired private val appProperty: AppProperty,
    @Autowired val whatsAppAvailabilityRepository: WhatsAppAvailabilityRepository,
) {

    private fun log(value: String) = LoggerUtils.log("v1/${this.javaClass.simpleName}.$value")
    private fun printLog(value: String) = LoggerUtils.printLog("v1/${this.javaClass.simpleName}.$value")

    companion object {
        const val BASE_URL_MESSAGE_API = "http://media.smsgupshup.com/GatewayAPI/rest?"
        const val API_VERSION = "1.1"
    }

    private var _gupshupCred: Creds? = null

    @Throws(Exception::class)
    private fun gupshupCred(): Creds {
        if (_gupshupCred == null) {
            _gupshupCred = credsManager.fetchCredentials(
                EnPartnerName.GUPSHUP,
                if (appProperty.isProduction()) EnCredType.PRODUCTION else EnCredType.UAT
            )
            _gupshupCred ?: throw Exception("Failed to get credentials.")
        }
        return _gupshupCred!!
    }

    @Throws(Exception::class)
    fun optIn(
        mobileNumber: String
    ): LocalResponse {

        val sb = StringBuilder()
        sb.append("userid=${gupshupCred().username}")
        sb.append("&password=${gupshupCred().password}")
        sb.append("&phone_number=${mobileNumber}")
        sb.append("&v=$API_VERSION")
        sb.append("&format=json")
        sb.append("&method=OPT_IN")
        sb.append("&channel=WHATSAPP")
        sb.append("&auth_scheme=plain")

        val fullUrl = "${gupshupCred().apiUrl}$sb"

        val localHTTPResponse = commonNetworkingClient
            .NewRequest()
            .getCall(fullUrl)
            .send()

        log("optIn - response: ${localHTTPResponse.stringEntity}")

        val responseJson = JSONObject(localHTTPResponse.stringEntity)

        return LocalResponse().apply {
            message = localHTTPResponse.stringEntity
            isSuccess = responseJson.optJSONObject("response")?.optString(STATUS, NA) == SUCCESS
        }

    }

    fun isApplicableToSendMessage(mobileNumber: String): Boolean{

        return whatsAppAvailabilityRepository.findByMobileNumber(mobileNumber)?.let {
            if (it.isAvailable) true
            else {
                DateTimeUtils.getDateDifferenceInDays(it.lastTriedDatetime,
                    DateTimeUtils.getCurrentDateTimeInIST()) > 90
            }
        } ?: run {
            true
        }

    }

    @Throws(Exception::class)
    fun sendMessage(
        singleTextTemplate: MessageDTO
    ): LocalResponse {

        val sb = StringBuilder()
        sb.append("userid=${gupshupCred().username}")
        sb.append("&password=${gupshupCred().password}")
        sb.append("&send_to=${singleTextTemplate.mobileNumber}")
        sb.append("&v=$API_VERSION")
        sb.append("&format=json")
        sb.append("&msg_type=TEXT")
        sb.append("&method=SENDMESSAGE")
        sb.append("&msg=${singleTextTemplate.message}")
//        sb.append("&isTemplate=true") //TODO: This needs to be confirmed

        val fullUrl = "${gupshupCred().apiUrl}$sb"

        val localHTTPResponse = commonNetworkingClient
            .NewRequest()
            .getCall(fullUrl)
            .send()

        log("sendMessage - response: ${localHTTPResponse.stringEntity}")

        val responseJson = JSONObject(localHTTPResponse.stringEntity)

        return LocalResponse().apply {
            message = localHTTPResponse.stringEntity
            isSuccess = responseJson.optJSONObject("response")?.optString(STATUS, NA) == SUCCESS
        }

    }

}


@Service
@EnableAsync
class WhatsAppBackgroundProcessHelper(
    @Autowired val whatsAppHelper: WhatsAppHelper,
    @Autowired val mailHelper: MailHelper,
) {

    private fun printLog(value: String) = LoggerUtils.printLog("v1/${this.javaClass.simpleName}.$value")

    @Transactional
    @Async(THREAD_POOL_TASK_EXECUTOR)
    fun processBulkTextTemplateSend(
        csvContacts: ArrayList<MessageDTO>,
        header: String?,
        footer: String?,
        orgId: String,
        requestee: String?,
    ) {

        val total = csvContacts.size
        var valid = 0
        var invalid = 0
        var sent = 0
        var error = 0

        csvContacts.forEach { singleTextTemplateDTO ->

            singleTextTemplateDTO.mandatoryFieldCheck().let {

                if (it.isSuccess) {

                    valid++

//                    if (singleTextTemplateDTO.header.isInvalid() && header.isNotNullOrNA())
//                        singleTextTemplateDTO.header = header
//
//                    if (singleTextTemplateDTO.footer.isInvalid() && footer.isNotNullOrNA())
//                        singleTextTemplateDTO.footer = footer

                    val whatsAppResponse = whatsAppHelper.sendMessage(singleTextTemplateDTO)

                    if (whatsAppResponse.isSuccess) sent++
                    else error++

                    Thread.sleep(200)

                } else {
                    invalid++
                    printLog("processBulkTextTemplateSend - Error: ${it.message}")
                }

            }

        }

        //TODO: discuss with ranan

//        if (requestee.isNotNullOrNA()) {
//
//            val sb = StringBuilder()
//            sb.append("Please find the stats below:")
//            sb.append("\n\nTotal : $total")
//            sb.append("\nvalid : $valid")
//            sb.append("\ninvalid : $invalid")
//            sb.append("\nsent : $sent")
//            sb.append("\nerror : $error")
//            sb.append("\n\n\nThis is an auto generated email. Please do not reply.")
//            sb.append("\n- Homefirst")
//
//            mailHelper.sendMimeMessage(
//                arrayOf(requestee!!),
//                "WhatsApp Messages Sent",
//                sb.toString(),
//                cc = arrayOf("harshit.chauhan@homefirstindia.com")
//            )
//
//        }

    }

}