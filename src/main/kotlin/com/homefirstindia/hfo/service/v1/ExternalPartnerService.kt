package com.homefirstindia.hfo.service.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.homefirstindia.hfo.dto.v1.MessageDTO
import com.homefirstindia.hfo.dto.v1.externalpartner.EPAuthRequest
import com.homefirstindia.hfo.helper.v1.*
import com.homefirstindia.hfo.manager.v1.SalesforceManager
import com.homefirstindia.hfo.model.v1.*
import com.homefirstindia.hfo.networking.v1.LMSNetworkingClient
import com.homefirstindia.hfo.repository.v1.*
import com.homefirstindia.hfo.security.ContextProvider
import com.homefirstindia.hfo.utils.*
import com.opencsv.bean.CsvToBeanBuilder
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import kotlin.math.ln
import kotlin.math.roundToInt


@Service
class ExternalPartnerService(
    @Autowired val oneResponse: OneResponse,
    @Autowired val contextProvider: ContextProvider,
    @Autowired val partnerLogHelper: PartnerLogHelper,
    @Autowired val partnerRepository: PartnerRepository,
    @Autowired val cryptoUtils: CryptoUtils,
    @Autowired val whatsAppHelper: WhatsAppHelper,
    @Autowired val whatsAppBackgroundProcessHelper: WhatsAppBackgroundProcessHelper,
    @Autowired val locationDistanceMatrixRepository: LocationDistanceMatrixRepository,
    @Autowired val locationDirectionsRepository: LocationDirectionsRepository,
    @Autowired val locationHelper: LocationHelper,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val salesforceManager: SalesforceManager,
    @Autowired val propertyInsightHelper: PropertyInsightHelper,
    @Autowired val whatsAppMessageRepository: WhatsAppMessageRepository,
    @Autowired val whatsappRepositoryMaster: WhatsappRepositoryMaster,
    @Autowired val lmsNetworkingClient: LMSNetworkingClient
) {

    private fun log(value: String) = LoggerUtils.log("v1/${this.javaClass.simpleName}.$value")
    private fun printLog(value: String) = LoggerUtils.printLog("v1/${this.javaClass.simpleName}.$value")

    @Throws(Exception::class)
    fun authenticate(epAuthRequest: EPAuthRequest): ResponseEntity<String>? {

        printLog("authenticate - hfPartner.sessionPasscode : ${epAuthRequest.orgId}")

        val hfPartner = contextProvider.getPartner(epAuthRequest.orgId)

        val epLogger = partnerLogHelper.Builder(epAuthRequest)

        if (!hfPartner.isSessionValid()) {

            val passcodeString =
                "${getRandomUUID().replace("-", "")}${epAuthRequest.orgId}${System.currentTimeMillis()}"

            hfPartner.apply {
                sessionPasscode = cryptoUtils.encryptAes(passcodeString)
                updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
                sessionUpdateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
                sessionValidDatetime = DateTimeUtils.getDateTimeByAddingHours(1)
            }

            partnerRepository.save(hfPartner)

        }

        epLogger
            .setRequestStatus(UserActionStatus.SUCCESS)
            .log()

        printLog("authenticate - hfPartner.sessionPasscode : ${hfPartner.sessionPasscode}")

        return oneResponse.getSuccessResponse(
            JSONObject()
                .put(SESSION_PASSCODE, hfPartner.sessionPasscode)
                .put(VALID_UPTO, hfPartner.sessionValidDatetime)
        )

    }

    @Throws(Exception::class)
    fun whatsappOptIn(
        epAuthRequest: EPAuthRequest,
        mobileNumber: String
    ): ResponseEntity<String> {

        val epLogger = partnerLogHelper.Builder(epAuthRequest)

        if (mobileNumber.isInvalid() && mobileNumber.isValidMobileNumber()) {
            val message = "Invalid mobile number"
            log("whatsappOptIn - $message: $mobileNumber")
            epLogger.setRequestDesc(message).setRequestStatus(UserActionStatus.FAILURE).log()
            return oneResponse.invalidData(message)
        }

        var eOptInData = whatsappRepositoryMaster.whatsAppOptInRepository.findByMobileNumber(mobileNumber)

        eOptInData?.let {
            if (it.status == EnOptInStatus.OPT_IN.value) {
                val message = "Already opt in"
                log("whatsappOptIn - $message: $mobileNumber")
                epLogger.setRequestDesc(message).setRequestStatus(UserActionStatus.FAILURE).log()
                return oneResponse.operationFailedResponse(message)
            }
        } ?: run {
            eOptInData = WhatsAppOptIn().apply {
                this.mobileNumber = mobileNumber
            }
        }

        val lResponse = whatsAppHelper.optIn(eOptInData?.mobileNumber!!)

        if (!lResponse.isSuccess) {
            val message = "Failed to opt in"
            log("whatsappOptIn - $message: $mobileNumber")
            epLogger.setRequestDesc(lResponse.message).setRequestStatus(UserActionStatus.FAILURE).log()
            return oneResponse.operationFailedResponse(message)
        }

        JSONObject(lResponse.message).let { mainJson ->
            mainJson.optJSONObject("response")?.let {
                eOptInData?.optInId = it.optString("id", NA)
                eOptInData?.status = it.optString("details", NA)
            }
        }

        eOptInData?.updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
        eOptInData?.let { whatsappRepositoryMaster.whatsAppOptInRepository.save(it) }

        epLogger.setRequestStatus(UserActionStatus.SUCCESS)
            .setServiceName(object {}.javaClass.enclosingMethod.name)
            .setResponseStatus(200).log()

        return oneResponse.getSuccessResponse(
            JSONObject()
                .put(MESSAGE, "Mobile number opted in successfully!")
        )

    }

    @Throws(Exception::class)
    fun sendWhatsappMessage(
        epAuthRequest: EPAuthRequest,
        messageDTO: MessageDTO,
    ): ResponseEntity<String>? {

        val epLogger = partnerLogHelper.Builder(epAuthRequest)

        messageDTO.mandatoryFieldCheck().let {
            if (!it.isSuccess) {
                log("sendWhatsappMessage - ${it.message}")
                epLogger.setResponseStatus(201).setRequestStatus(UserActionStatus.FAILURE)
                    .setRequestDesc(it.message).log()
                return oneResponse.invalidData(it.message)
            }
        }

        val eOptInData = whatsappRepositoryMaster.whatsAppOptInRepository
            .findByMobileNumber(messageDTO.mobileNumber!!) ?: run { WhatsAppOptIn() }

        if (eOptInData.status.isInvalid() || eOptInData.status != EnOptInStatus.OPT_IN.value) {

            printLog("sendWhatsappMessage - mobile number not opted in: ${messageDTO.mobileNumber}")

            eOptInData.mobileNumber = messageDTO.mobileNumber

            val lResponse = whatsAppHelper.optIn(eOptInData.mobileNumber!!)

            log("sendWhatsappMessage - ${lResponse.message}")

            if (!lResponse.isSuccess) {
                val msg = "Failed to opt in"
                log("sendWhatsappMessage - $messageDTO: ${eOptInData.mobileNumber}")
                epLogger.setRequestDesc(lResponse.message).setRequestStatus(UserActionStatus.FAILURE).log()
                return oneResponse.operationFailedResponse(msg)
            }

            JSONObject(lResponse.message).let { mainJson ->
                mainJson.optJSONObject("response")?.let {
                    eOptInData.optInId = it.optString("id", NA)
                    eOptInData.status = it.optString("details", NA)
                    eOptInData.response = lResponse.message
                }
            }

            eOptInData.updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
            eOptInData.let { whatsappRepositoryMaster.whatsAppOptInRepository.save(it) }

        }

        if (!whatsAppHelper.isApplicableToSendMessage(messageDTO.mobileNumber!!)) {
            val msg = "Failed to send message"
            log("sendWhatsappMessage - whatsapp not available or blocked for 90 days: ${messageDTO.mobileNumber}")
            epLogger.setRequestDesc(msg).setRequestStatus(UserActionStatus.FAILURE).log()
            return oneResponse.operationFailedResponse(msg)
        }

        printLog("sendWhatsappMessage - sending message to mobile number: ${messageDTO.mobileNumber}")

        val lResponse = whatsAppHelper.sendMessage(messageDTO)

        val responseJson = JSONObject(lResponse.message).getJSONObject("response")

        val whatsAppMessage = WhatsAppMessage().fromDTO(messageDTO)
        whatsAppMessage.orgId = epAuthRequest.orgId
        whatsAppMessage.messageId = responseJson.getString("id")
        whatsAppMessage.response = responseJson.toString()
        whatsAppMessage.setUpdateParams()

        if (!lResponse.isSuccess) {

            whatsAppMessage.apply {
                error = lResponse.message
                status = EnTransactionStatus.FAILURE.name
            }

            whatsAppMessageRepository.save(whatsAppMessage)

            val message = "Failed to send message"
            log("sendWhatsappMessage - $message: ${messageDTO.mobileNumber}")
            epLogger.setRequestDesc(lResponse.message).setRequestStatus(UserActionStatus.FAILURE).log()
            return oneResponse.operationFailedResponse(message)

        }

        epLogger.setRequestStatus(UserActionStatus.SUCCESS).log()

        whatsAppMessage.status = EnTransactionStatus.SUCCESS.name
        whatsAppMessageRepository.save(whatsAppMessage)

        return oneResponse.getSuccessResponse(
            JSONObject()
                .put(ID, whatsAppMessage.id)
                .put(MESSAGE, "Message sent successfully!")
        )

    }

    @Throws(Exception::class)
    fun sendWhatsAppTextTemplateBulk(
        epAuthRequest: EPAuthRequest,
        file: MultipartFile,
        header: String?,
        footer: String?,
        requestee: String?,
    ): ResponseEntity<String>? {

        val epLogger = partnerLogHelper.Builder(epAuthRequest)

        if (file.isEmpty) {

            val msg = "Invalid / Empty file."
            log("sendWhatsAppTextTemplateBulk - $msg")
            epLogger
                .setRequestStatus(UserActionStatus.FAILURE)
                .setRequestDesc(msg)
                .log()

            return oneResponse.invalidData("Please select a CSV file to upload.")

        }

        val csvContacts: ArrayList<MessageDTO>
        var bufferedReader: Reader? = null
        try {

            bufferedReader = BufferedReader(InputStreamReader(file.inputStream))

            val csvToBean = CsvToBeanBuilder<MessageDTO>(bufferedReader)
                .withType(MessageDTO::class.java)
                .withIgnoreLeadingWhiteSpace(true)
                .build()

            csvContacts = csvToBean.parse() as ArrayList<MessageDTO>

        } catch (e: Exception) {

            bufferedReader?.close()

            val msg = "Error while processing the csv file : ${e.message}"
            log("sendWhatsAppTextTemplateBulk - $msg")
            epLogger
                .setRequestStatus(UserActionStatus.FAILURE)
                .setRequestDesc(msg)
                .log()

            return oneResponse.invalidData(msg)

        } finally {
            bufferedReader?.close()
        }

        log("sendWhatsAppTextTemplateBulk - Total contacts in CSV : ${csvContacts.size}")

        if (csvContacts.isEmpty()) {

            val msg = "No contacts found in the csv file."
            log("sendWhatsAppTextTemplateBulk - $msg")
            epLogger
                .setRequestStatus(UserActionStatus.FAILURE)
                .setRequestDesc(msg)
                .log()

            return oneResponse.invalidData(msg)

        }

        /*
        * Start the scheduling in background
        * */
        whatsAppBackgroundProcessHelper.processBulkTextTemplateSend(
            csvContacts,
            header,
            footer,
            epAuthRequest.orgId,
            requestee
        )

        return oneResponse.getSuccessResponse(
            JSONObject().put(
                MESSAGE,
                "Your request have been scheduled. " +
                        "You'll receive update via email once the process is completed."
            )
        )

    }


    @Throws(Exception::class)
    fun getLocationDistanceMatrix(
        epAuthRequest: EPAuthRequest,
        distanceMatrix: LocationDistanceMatrix,
    ): ResponseEntity<String> {

        val epLogger = partnerLogHelper.Builder(epAuthRequest)

        distanceMatrix.mandatoryFieldsCheck().let {

            if (!it.isSuccess) {

                log("getLocationDistanceMatrix - ${it.message}")
                epLogger
                    .setRequestStatus(UserActionStatus.FAILURE)
                    .setRequestDesc(it.message)
                    .log()

                return oneResponse.getFailureResponse(it.toJson())

            }

        }

        distanceMatrix.orgId = epAuthRequest.orgId
        distanceMatrix.setModifiers()
        locationDistanceMatrixRepository.save(distanceMatrix)

        val gResponse = locationHelper.getDistanceMatrix(distanceMatrix)

        if (!gResponse.isSuccess) {

            log("getLocationDistanceMatrix - ${gResponse.message}")

            epLogger
                .setRequestStatus(UserActionStatus.FAILURE)
                .setRequestDesc(gResponse.message)
                .log()

            distanceMatrix.apply {
                success = false
                error = gResponse.message
                setModifiers()
            }

            locationDistanceMatrixRepository.save(distanceMatrix)

            return oneResponse.getFailureResponse(gResponse.toJson())

        }

        JSONObject(gResponse.message).let { gData ->
            distanceMatrix.apply {
                success = true
                updateFromJson(gData)
                setModifiers()
            }
        }

        locationDistanceMatrixRepository.save(distanceMatrix)

        epLogger
            .setRequestStatus(UserActionStatus.SUCCESS)
            .log()

        return oneResponse.getSuccessResponse(
            JSONObject().put(
                "distanceMatrix",
                JSONObject(objectMapper.writeValueAsString(distanceMatrix))
            )
        )

    }

    @Throws(Exception::class)
    fun getLocationDirections(
        epAuthRequest: EPAuthRequest,
        locationDirections: LocationDirections,
    ): ResponseEntity<String> {

        val epLogger = partnerLogHelper.Builder(epAuthRequest)
        locationDirections.mandatoryFieldsCheck().let {

            if (!it.isSuccess) {

                log("getLocationDirections - ${it.message}")
                epLogger
                    .setRequestStatus(UserActionStatus.FAILURE)
                    .setRequestDesc(it.message)
                    .log()

                return oneResponse.getFailureResponse(it.toJson())

            }

        }

        locationDirections.orgId = epAuthRequest.orgId
        locationDirections.setModifiers()
        locationDirectionsRepository.save(locationDirections)

        val gResponse = locationHelper.getDirection(locationDirections)

        if (!gResponse.isSuccess) {

            log("getLocationDirections - ${gResponse.message}")

            epLogger
                .setRequestStatus(UserActionStatus.FAILURE)
                .setRequestDesc(gResponse.message)
                .log()

            locationDirections.apply {
                success = false
                error = gResponse.message
                setModifiers()
            }

            locationDirectionsRepository.save(locationDirections)

            return oneResponse.getFailureResponse(gResponse.toJson())

        }

        JSONObject(gResponse.message).let { gData ->
            locationDirections.apply {
                success = true
                updateFromJson(gData)
                setModifiers()
            }
        }

        locationDirectionsRepository.save(locationDirections)

        epLogger
            .setRequestStatus(UserActionStatus.SUCCESS)
            .log()

        return oneResponse.getSuccessResponse(
            JSONObject().put(
                "locationDirections",
                JSONObject(objectMapper.writeValueAsString(locationDirections))
            )
        )

    }

    fun getLoanDetail(
        epAuthRequest: EPAuthRequest,
        loanAccountNumber: String,
    ): ResponseEntity<String> {

        val epLogger = partnerLogHelper.Builder(epAuthRequest)

        if (loanAccountNumber.isInvalid()) {

            val message = "Invalid loan account number"
            log("getLoanDetail - $message")
            epLogger.setRequestDesc(message).setRequestStatus(UserActionStatus.FAILURE).log()
            return oneResponse.invalidData(message)

        }

        val loan = salesforceManager.fetchLoanDetails(loanAccountNumber)

        loan ?: run {
            val message = "No loan details found with loan account number: $loanAccountNumber"
            log("getLoanDetail - $message")
            epLogger.setRequestDesc(message).setRequestStatus(UserActionStatus.FAILURE).log()
            return oneResponse.resourceNotFound(message)
        }

        val emiAmount = salesforceManager.getEmiAmount(loanAccountNumber)

        emiAmount?.let {

            loan.let {

                it.emiAmount = emiAmount

                if (!it.disbursalStatus.equals(FULLY_DISBURSED)
                    || it.emiAmount == 0.0
                ) {

                    it.remainingTenure = it.totalTenure

                } else {

                    val monthlyROI = it.interestRate!! / 1200

                    val remainingTenure = ((ln(it.emiAmount!!)
                            - ln(it.emiAmount!! - (it.principalAmount!! * monthlyROI))) / ln(1 + monthlyROI))

                    it.remainingTenure = remainingTenure.roundToInt()

                }

            }

        }

        epLogger
            .setRequestStatus(UserActionStatus.SUCCESS)
            .log()

        return oneResponse.getSuccessResponse(
            JSONObject().put(
                "loanDetails",
                JSONObject(objectMapper.writeValueAsString(loan))
            )
        )

    }

    @Throws(Exception::class)
    fun requestPropertyInsightReport(
        epAuthRequest: EPAuthRequest,
        propertyInsightRequest: PropertyInsight,
    ): ResponseEntity<String> {

        val epLogger = partnerLogHelper.Builder(epAuthRequest)

        val sendReportResponse = propertyInsightHelper.sendReportRequest(
            propertyInsightRequest
        )

        return if (sendReportResponse.isSuccess) {

            epLogger.setRequestStatus(UserActionStatus.SUCCESS)
                .setServiceName(object {}.javaClass.enclosingMethod.name)
                .setResponseStatus(200).log()

            oneResponse.getSuccessResponse(
                JSONObject()
                    .put(MESSAGE, "Property insight requested successfully!")
                    .put(ID, sendReportResponse.message)
            )

        } else {

            epLogger.setServiceName(object {}.javaClass.enclosingMethod.name)
                .setRequestStatus(UserActionStatus.FAILURE)
                .setRequestDesc(sendReportResponse.message).setResponseStatus(201).log()

            oneResponse.getFailureResponse(sendReportResponse.toJson())

        }

    }

    @Throws(Exception::class)
    fun addDocumentOnPropertyInsight(
        epAuthRequest: EPAuthRequest,
        propertyInsightDocumentDTO: PropertyInsightDocumentDTO,
    ): ResponseEntity<String> {

        val epLogger = partnerLogHelper.Builder(epAuthRequest)

        val addDocumentResponse = propertyInsightHelper.addDocument(
            propertyInsightDocumentDTO
        )

        return if (addDocumentResponse.isSuccess) {

            epLogger.setRequestStatus(UserActionStatus.SUCCESS)
                .setServiceName(object {}.javaClass.enclosingMethod.name)
                .setResponseStatus(200).log()

            oneResponse.getSuccessResponse(
                JSONObject()
                    .put(MESSAGE, "Document added successfully!")
                    .put(FILE_NAMES, addDocumentResponse.message)
            )

        } else {

            epLogger.setServiceName(object {}.javaClass.enclosingMethod.name)
                .setRequestStatus(UserActionStatus.FAILURE)
                .setRequestDesc(addDocumentResponse.message).setResponseStatus(201).log()

            oneResponse.getFailureResponse(addDocumentResponse.toJson())

        }

    }

    @Throws(Exception::class)
    fun leadTelephonyDialNumber(
        epAuthRequest: EPAuthRequest,
        mobileNumber: String,
    ):ResponseEntity<String>{

        val epLogger = partnerLogHelper.Builder(epAuthRequest)

        if (!mobileNumber.isValidMobileNumber()) {

            val msg = "Invalid mobile number: $mobileNumber"
            log("leadTelephonyDialNumber - $msg")

            epLogger
                .setRequestDesc("Invalid mobile number: $mobileNumber")
                .setRequestStatus(UserActionStatus.FAILURE)
                .log()

            return oneResponse.invalidData(msg)

        }

        val lhResponse = lmsNetworkingClient.get(
            "${LMSNetworkingClient.Endpoints.LEAD_CAPTURE_NOTIFY.value}$mobileNumber")

        if (!lhResponse.isSuccess) {

            val msg = "Failed to get telephony dial number: ${lhResponse.stringEntity}"
            log("leadTelephonyDialNumber - $msg")
            epLogger
                .setRequestDesc("Failed to get telephony dial number")
                .setRequestStatus(UserActionStatus.FAILURE)
                .log()

            return oneResponse.operationFailedResponse("Failed to get telephony dial number")

        }

        val mainJson = JSONObject(lhResponse.stringEntity)

        mainJson.optJSONObject("lead")?.let {leadJson ->

            leadJson.optJSONObject("owner")?.let {ownerJson ->

                ownerJson.optString("mobileNumber")?.let {

                    if (it.isInvalid())
                        return oneResponse.operationFailedResponse("No dial number found")

                    val responseJson = JSONObject().apply {
                        put("dialNumber", it)
                    }

                    return oneResponse.getSuccessResponse(responseJson)

                }

            }

        }

        return oneResponse.operationFailedResponse("No dial number found")

    }

}