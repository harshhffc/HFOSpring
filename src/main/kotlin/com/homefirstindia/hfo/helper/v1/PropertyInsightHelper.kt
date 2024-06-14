package com.homefirstindia.hfo.helper.v1

import com.homefirstindia.hfo.clients.AmazonClient
import com.homefirstindia.hfo.clients.EnS3BucketPath
import com.homefirstindia.hfo.manager.v1.CredsManager
import com.homefirstindia.hfo.manager.v1.EnCredType
import com.homefirstindia.hfo.manager.v1.EnPartnerName
import com.homefirstindia.hfo.manager.v1.SalesforceManager
import com.homefirstindia.hfo.model.v1.*
import com.homefirstindia.hfo.networking.v1.CommonNetworkingClient
import com.homefirstindia.hfo.repository.v1.DocumentRepositoryMaster
import com.homefirstindia.hfo.repository.v1.PropertyInsightRepository
import com.homefirstindia.hfo.security.AppProperty
import com.homefirstindia.hfo.utils.*
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File

@Component
class PropertyInsightHelper(
    @Autowired private val credsManager: CredsManager,
    @Autowired private val commonNetworkingClient: CommonNetworkingClient,
    @Autowired private val appProperty: AppProperty,
    @Autowired val propertyInsightRepository: PropertyInsightRepository,
    @Autowired val amazonClient: AmazonClient,
    @Autowired val cryptoUtils: CryptoUtils,
    @Autowired val documentRepositoryMaster: DocumentRepositoryMaster,
    @Autowired val salesforceManager: SalesforceManager,
    @Autowired val mailHelper: MailHelper,
    @Autowired val documentHelper: DocumentHelper
) {

    private fun log(value: String) = LoggerUtils.log("v1/${this.javaClass.simpleName}.$value")

    companion object {

        const val API_BASE_URL_UAT = "https://app-uat.tealindia.in/api"
        const val API_BASE_URL_PROD = "https://app.tealindia.in/api"
        const val FILE_API_BASE_URL_UAT = "https://filenet-uat.tealindia.in"
        const val FILE_API_BASE_URL_PROD = "https://filenet.tealindia.in"

        const val QUERY_ID = "query_id"
        const val REPORT_ID = "report_id"
        const val CODE = "code"
        const val REPORT_DATA = "report_data"
        const val REPORT_UPLOADED = "report_uploaded"

        const val DEVELOPER_MAIL_ID = "sanjay.jaiswar@homefirstindia.com"
        const val SUPERVISOR_MAIL_ID = "kamal.narwani@homefirstindia.com"

    }

    fun getApiBaseUrl(isFileApiUrl: Boolean): String {
        return if (appProperty.isProduction()) {
            if (!isFileApiUrl) API_BASE_URL_PROD else FILE_API_BASE_URL_PROD
        } else {
            if (!isFileApiUrl) API_BASE_URL_UAT else FILE_API_BASE_URL_UAT
        }
    }

    fun getSfBaseUrl(): String {
        return if (appProperty.isProduction()) {
            "https://hffc.my.salesforce.com/"
        } else {
            "https://hffc--preprod.sandbox.my.salesforce.com/"
        }
    }

    private var _tealCred: Creds? = null

    @Throws(Exception::class)
    fun tealCred(): Creds {
        if (_tealCred == null) {
            _tealCred = credsManager.fetchCredentials(
                EnPartnerName.TEAL_V2,
                if (appProperty.isProduction()) EnCredType.PRODUCTION else EnCredType.UAT
            )
            _tealCred ?: throw Exception("Failed to get teal credentials.")
        }
        return _tealCred!!
    }

    @Throws(Exception::class)
    fun sendReportRequest(
        propertyInsight: PropertyInsight
    ): LocalResponse {

        propertyInsight.mandatoryFieldsCheck().let {
            if (!it.isSuccess) {
                log("sendReportRequest - ${it.message}")
                return it
            }
        }

        propertyInsightRepository.findBySfPropertyInsightId(propertyInsight.sfPropertyInsightId)?.let {
            if (it.status != EnPropertyInsightStatus.INITIATED.value) {
                val msg = "Property insight request already exist: ${propertyInsight.sfPropertyInsightId}"
                log("requestPropertyInsightReport - $msg")
                return LocalResponse().setMessage(msg)
            } else {
                propertyInsight.id = it.id
            }
        }

        propertyInsight.status = EnPropertyInsightStatus.INITIATED.value
        propertyInsight.rawRequestJson = JSONObject(propertyInsight.propertyRequest).toString()

        propertyInsightRepository.save(propertyInsight)

        val apiUrl = "${getApiBaseUrl(false)}/reports"

        val response = commonNetworkingClient
            .NewRequest()
            .postCall(
                apiUrl,
                propertyInsight.getPropInsightRequestJson()!!
            )
            .addHeader(CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
            .addHeader(AUTHORIZATION, "Bearer ${tealCred().apiKey}")
            .send()

        if (!response.isSuccess) {

            propertyInsight.rawRequestJson = JSONObject(response.errorMessage).toString()

            log("sendReportRequest - Error: ${response.errorMessage}")
            return LocalResponse().setMessage("Failed to request property insight")

        }

        val propertyRequestJson = JSONObject(response.stringEntity)

        propertyInsight.queryId = propertyRequestJson.optString(QUERY_ID, NA)
        propertyInsight.tealRequestId = propertyRequestJson.optInt(REPORT_ID, -1)

        if (propertyInsight.queryId.isInvalid()
            || propertyInsight.tealRequestId.isInvalid()) {

            val msg = "Invalid query or report Id"
            log("requestPropertyInsightReport - $msg")
            return LocalResponse().setMessage(msg)

        }

        propertyInsight.status = EnPropertyInsightStatus.REQUESTED.value
        propertyInsight.updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()

        propertyInsightRepository.save(propertyInsight)

        val lResponse = LocalResponse()

        lResponse.apply {
            message = propertyInsight.id!!
            isSuccess = true
        }

        return lResponse
    }

    @Throws(Exception::class)
    fun addDocument(
        propertyInsightDocumentDTO: PropertyInsightDocumentDTO
    ): LocalResponse {

        propertyInsightDocumentDTO.mandatoryFieldsCheck().let {
            if (!it.isSuccess) {
                return it
            }
        }

        val ePropertyInsight = propertyInsightRepository.findBySfPropertyInsightIdAndStatusIn(
            propertyInsightDocumentDTO.sfPropertyInsightId,
            EnPropertyInsightStatus.getPIPresentStatus()) ?: run {
                return LocalResponse().setMessage("No property insight request " +
                    "found: ${propertyInsightDocumentDTO.sfPropertyInsightId}")
            }

        propertyInsightDocumentDTO.fileData?.let { fileData ->
            if (fileData.contains(",")) {
                val fileDataSplit = fileData.split(",")
                if (fileDataSplit.size >= 2) {
                    propertyInsightDocumentDTO.fileData = fileDataSplit[1]
                }
            }
        }

        val nFileName = "${System.currentTimeMillis()}_${propertyInsightDocumentDTO.fileName}"
        propertyInsightDocumentDTO.fileName = nFileName

        val docFile = convertBase64ToFile(propertyInsightDocumentDTO.fileData!!,
            File("${appProperty.filePath}${propertyInsightDocumentDTO.fileName}")
        )

        docFile ?: run {
            log("addDocument - fail to convert base 64 to file")
            return LocalResponse().setMessage("Failed to upload property insight document")
        }

        val status = amazonClient.uploadFile(
            propertyInsightDocumentDTO.fileName!!, docFile, EnS3BucketPath.PROPERTY_INSIGHT_DOCUMENT)
        docFile.delete()

        if (!status) {
            log("addDocument - fail to upload property insight document to S3")
            return LocalResponse().setMessage("Failed to upload property insight document")
        }

        val fileType = FileTypesExtentions[".${docFile.extension}"] ?: run {
            log("addDocument - No file extension found")
            return LocalResponse().setMessage("Failed to upload property insight document")
        }

        propertyInsightDocumentDTO.fileType = fileType.contentType

        val apiUrl = "${getApiBaseUrl(true)}/${ePropertyInsight.queryId}"

        val response = commonNetworkingClient
            .NewRequest()
            .postCall(
                apiUrl,
                propertyInsightDocumentDTO.getAddDocRequestJson())
            .addHeader(CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
            .addHeader(AUTHORIZATION, "Bearer ${tealCred().apiKey}")
            .send()

        if (!response.isSuccess) {
            log("addDocument - Error: ${response.errorMessage}")
            return LocalResponse().setMessage("Failed to add document")
        }

        val attachment = Attachment().apply {
            this.fileName = propertyInsightDocumentDTO.fileName
            fileIdentifier = cryptoUtils.encodeBase64("${System.currentTimeMillis()}${Math.random()}")
            this.objectId = ePropertyInsight.id
            objectType = MyObject.PROPERTY_INSIGHT.value
            contentType = fileType.displayName
            attachementType = AttachmentType.PROPERTY_INSIGHT_DOCUMENT.value
        }

        documentRepositoryMaster.attachmentRepository.save(attachment)

        val allAttachments = documentRepositoryMaster.attachmentRepository.getAttachmentByObjectId(ePropertyInsight.id)

        val fileNameBuilder = StringBuilder()

        allAttachments?.let { attachmentsList ->
            attachmentsList.sortedByDescending { it.createDatetime }.forEachIndexed { index, singleAttachment ->
                fileNameBuilder.append("${index + 1}. ${singleAttachment.fileName}")
                if (attachmentsList.size > (index + 1))
                    fileNameBuilder.append("\n")
            }
        }

        return LocalResponse().apply {
            message = fileNameBuilder.toString()
            isSuccess = true
        }

    }

    @Throws(Exception::class)
    fun getDocument(
        propertyInsight: PropertyInsight
    ): LocalResponse {

        if (propertyInsight.propertyInsightResponse?.reportName.isInvalid()) {
            log("getDocument - Invalid report name")
            return LocalResponse().setMessage("Failed to get report")
        }

        if (propertyInsight.queryId.isInvalid()) {
            log("getDocument - Invalid query Id")
            return LocalResponse().setMessage("Failed to get report")
        }

        val apiUrl = "${getApiBaseUrl(true)}/${propertyInsight.queryId}/" +
                "${propertyInsight.propertyInsightResponse?.reportName}"

        val response = commonNetworkingClient
            .NewRequest()
            .getCall(apiUrl)
            .addHeader(CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
            .addHeader(AUTHORIZATION, "Bearer ${tealCred().apiKey}")
            .send()

        if (!response.isSuccess) {
            log("getDocument - Error: ${response.errorMessage}")
            return LocalResponse().setMessage("Failed to get document")
        }

        return LocalResponse().apply {
            message = response.stringEntity
            isSuccess = true
        }

    }

    @Throws
    fun uploadPropertyInsightReportToS3 (
        fileBase64: String,
        contentType: String,
        propertyInsight: PropertyInsight,
        reportType: EnPIReportType
    ): Boolean {

        val fileType = FileTypesExtentions.getFileTypeFromContentType(contentType)

        fileType ?: run {
            log("uploadPropertyInsightReportToS3 - No file extension found from content type: $contentType")
            return false
        }

        val reportNameSuffix = if(reportType == EnPIReportType.DIGITAL_SEARCH_REPORT) "Digital" else "Legal"

        val nFileName = "${propertyInsight.sfPropertyInsightId}_Report_$reportNameSuffix" +
                "${System.currentTimeMillis()}${fileType.ext}"

        val status = amazonClient.uploadFile(
            nFileName, fileBase64, EnS3BucketPath.PROPERTY_INSIGHT_REPORTS)

        if (!status) {
            log("uploadPropertyInsightReportToS3 - fail to upload property insight report to S3")
            return false
        }

        val attachment =  Attachment().apply {
            this.fileName = nFileName
            fileIdentifier = cryptoUtils.encodeBase64("${System.currentTimeMillis()}${Math.random()}")
            this.objectId = propertyInsight.id
            objectType = MyObject.PROPERTY_INSIGHT.value
            this.contentType = fileType.displayName
            attachementType = AttachmentType.PROPERTY_INSIGHT.value
        }

        val document = Document().apply {

            val reportName = if(reportType == EnPIReportType.DIGITAL_SEARCH_REPORT) "DIGITAL" else "LEGAL"

            tag = "PROPERTY_INSIGHT,PROPERTY_INSIGHT_${reportName}"
            documentDetailObject = MyObject.PROPERTY_INSIGHT.value
            documentDetailID = propertyInsight.id
            documentType = if(reportType == EnPIReportType.DIGITAL_SEARCH_REPORT)
                EnDocumentType.PROPERTY_INSIGHT_DIGITAL.value else EnDocumentType.PROPERTY_INSIGHT_LEGAL.value
            this.attachment = attachment

        }

        val documentJunction = DocumentJunction().apply {
            this.document = document
            this.objectId = propertyInsight.id
            this.objectType = MyObject.PROPERTY_INSIGHT.value
        }

        documentRepositoryMaster.documentJunctionRepository.save(documentJunction)

        return true

    }

    @Throws(Exception::class)
    fun processPropertyInsightCallbackDetail(
        piResponseString: String
    ): LocalResponse {

        val propertyInsightResponse = JSONObject(piResponseString)

        val queryId = propertyInsightResponse.optString(QUERY_ID, NA)

        if (queryId.isInvalid()) {
            log("processPropertyInsightCallbackDetail - Invalid queryId.")
            return LocalResponse().setMessage("Invalid queryId")
        }

        val ePropertyInsight = propertyInsightRepository.findByQueryId(queryId)

        ePropertyInsight ?: run {
            LoggerUtils.log("processPropertyInsightCallbackDetail - No property insight " +
                    "request found for query id: $queryId")
            return LocalResponse().setMessage("No property insight request found for query id: $queryId")
        }

        ePropertyInsight.rawResponseJson = piResponseString
        ePropertyInsight.updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
        propertyInsightRepository.save(ePropertyInsight)

        val statusCode = propertyInsightResponse.optString(CODE, NA)
        val msg = propertyInsightResponse.optString("message", NA)

        when (statusCode) {
            EnPICallbackCode.S001.value -> {
                return processDigitalReportCallback(propertyInsightResponse, ePropertyInsight)
            }
            EnPICallbackCode.S002.value -> {
                return processLegalReportCallback(propertyInsightResponse, ePropertyInsight)
            }
            EnPICallbackCode.E001.value,
            EnPICallbackCode.E002.value,
            EnPICallbackCode.E003.value -> {
                salesforceManager.updatePIRemarkOrRequiredDocAndNotify(ePropertyInsight, msg,
                    null, this)

                log("processPropertyInsightCallbackDetail - Property insight cannot be " +
                        "processed due to: $msg | Property insight Id: ${ePropertyInsight.id}")

                return LocalResponse().apply {
                    message = "Property insight processing response captured successfully"
                    isSuccess = true
                }
            }
            EnPICallbackCode.P001.value -> {

                log("processPropertyInsightCallbackDetail - Property insight cannot be " +
                        "processed due to: $msg | Property insight Id: ${ePropertyInsight.id}")

                if (piRequestCompleted(ePropertyInsight))
                    return LocalResponse().apply {
                        message = "Property insight required document response captured successfully"
                        isSuccess = true
                    }

                return processRequiredDocumentCallback(propertyInsightResponse, ePropertyInsight)
            }
            EnPICallbackCode.P002.value -> {

                log("processPropertyInsightCallbackDetail - Property insight cannot be " +
                        "processed due to: $msg | Property insight Id: ${ePropertyInsight.id}")

                if (piRequestCompleted(ePropertyInsight))
                    return LocalResponse().apply {
                        message = "Property insight comment response captured successfully"
                        isSuccess = true
                    }

                val comment = propertyInsightResponse.optString("comment", NA)

                salesforceManager.updatePIRemarkOrRequiredDocAndNotify(ePropertyInsight, comment,
                    null, this)

                return LocalResponse().apply {
                    message = "Property insight comment response captured successfully"
                    isSuccess = true
                }
            }
            else ->{
                log("processPropertyInsightCallbackDetail - No valid status code found: $statusCode")
                return LocalResponse().setMessage("Failed to process property insight detail")
            }

        }

    }

    fun piRequestCompleted(pi: PropertyInsight): Boolean {

        var isSearchReportAvailable = false
        var isLegalReportAvailable = false

        documentHelper.getAttachmentFromDocJunction(
            EnDocumentType.PROPERTY_INSIGHT_DIGITAL, pi.id!!)?.let {
                isSearchReportAvailable = true
        }

        documentHelper.getAttachmentFromDocJunction(
            EnDocumentType.PROPERTY_INSIGHT_LEGAL, pi.id!!)?.let {
            isLegalReportAvailable = true
        }


        if (pi.generateLegalReport) return isSearchReportAvailable && isLegalReportAvailable

        return isSearchReportAvailable

    }

    fun processDigitalReportCallback(propertyInsightResponse: JSONObject,
                                     ePropertyInsight: PropertyInsight) : LocalResponse {

        val nProInsightResponse = PropertyInsightResponse()

        propertyInsightResponse.optJSONObject(REPORT_DATA)?.let {

            ePropertyInsight.status = EnPropertyInsightStatus.JSON_REPORT_PULLED.value
            propertyInsightRepository.save(ePropertyInsight)

            nProInsightResponse.parsePropertyInsightReportDataResponse(it)
            nProInsightResponse.isInsightAvailable = true

        }

        propertyInsightResponse.optJSONObject(REPORT_UPLOADED)?.let {
            nProInsightResponse.parsePropertyInsightReportFile(
                EnPIReportType.DIGITAL_SEARCH_REPORT.value, it
            )
            if (!nProInsightResponse.reportName.isInvalid())
                nProInsightResponse.isInsightAvailable = true
        }

        ePropertyInsight.propertyInsightResponse = nProInsightResponse

        if (!ePropertyInsight.propertyInsightResponse!!.isInsightAvailable) {
            val msg = "Property insight detail not available"
            log("processPropertyInsightCallbackDetail - $msg")
            return LocalResponse().setMessage(msg)
        }

        if (!ePropertyInsight.propertyInsightResponse?.reportName.isInvalid()
            && null == ePropertyInsight.attachment) {
            if (processPropertyInsightReportAndUpload(ePropertyInsight,
                    EnPIReportType.DIGITAL_SEARCH_REPORT)) {

                ePropertyInsight.attachment = documentHelper.getAttachmentFromDocJunction(
                    EnDocumentType.PROPERTY_INSIGHT_DIGITAL, ePropertyInsight.id!!)

                ePropertyInsight.status = EnPropertyInsightStatus.PDF_REPORT_PULLED.value
                ePropertyInsight.updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
                ePropertyInsight.reportGeneratedOn = DateTimeUtils.getCurrentDateTimeInIST()

                propertyInsightRepository.save(ePropertyInsight)

            }
        }

        val sfLocalResponse = salesforceManager.updatePropertyInsight(ePropertyInsight)

        if (!sfLocalResponse.isSuccess) {
            log("processPropertyInsightCallbackDetail - " +
                    "fail to update property insight detail on SF: ${sfLocalResponse.message}")

            sendSearchReportSFUpdateFailedMail(ePropertyInsight, sfLocalResponse.message)

            return LocalResponse().setMessage("Failed to process property insight detail")
        }

        ePropertyInsight.jsonDataUpdatedOnSF = true
        ePropertyInsight.pdfReportUpdatedOnSF = true

        ePropertyInsight.attachment?.let {
            ePropertyInsight.status = EnPropertyInsightStatus.COMPLETED.value
        }

        if (ePropertyInsight.status == EnPropertyInsightStatus.COMPLETED.value
            && !ePropertyInsight.requesteeNotified) {
            if (sendPropertyInsightMail(ePropertyInsight, EnPIReportType.DIGITAL_SEARCH_REPORT)) {
                ePropertyInsight.requesteeNotified = true
            }
        }

        ePropertyInsight.updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
        propertyInsightRepository.save(ePropertyInsight)

        return LocalResponse().apply {
            message = "Property insight digital report details captured successfully"
            isSuccess = true
        }

    }

     fun processLegalReportCallback(piResponse: JSONObject,
                                    ePropertyInsight: PropertyInsight) : LocalResponse {

        val nProInsightResponse = PropertyInsightResponse()

        piResponse.optJSONObject(REPORT_UPLOADED)?.let {
            nProInsightResponse.parsePropertyInsightReportFile(EnPIReportType.LEGAL_REPORT.value, it)
        }

        if (nProInsightResponse.reportName.isInvalid()) {
            log("processLegalReportCallback - No valid legal report name found: " +
                    "${ePropertyInsight.propertyInsightResponse?.reportName}")
            return LocalResponse().setMessage("Failed to capture property insight legal report details")
        }

        ePropertyInsight.propertyInsightResponse = nProInsightResponse

        processPropertyInsightReportAndUpload(ePropertyInsight, EnPIReportType.LEGAL_REPORT).let {status ->
            if (status) {
                ePropertyInsight.updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
                ePropertyInsight.legalReportGeneratedOn = DateTimeUtils.getCurrentDateTimeInIST()

                propertyInsightRepository.save(ePropertyInsight)

            } else {
                log("processLegalReportCallback - " +
                        "fail to upload property insight legal report for PI: ${ePropertyInsight.sfPropertyInsightId}")
                return LocalResponse().setMessage("Failed to process property insight legal report")
            }
        }

        val sfLocalResponse = salesforceManager.updatePropertyInsightLegalReport(ePropertyInsight)

        if (!sfLocalResponse.isSuccess) {
            log("processLegalReportCallback - " +
                    "Fail to update property insight detail on SF: ${sfLocalResponse.message}")

            sendLegalReportSFUpdateFailedMail(ePropertyInsight, sfLocalResponse.message)

            return LocalResponse().setMessage("Failed to process property insight legal report")
        }

        sendPropertyInsightMail(ePropertyInsight, EnPIReportType.LEGAL_REPORT)

        return LocalResponse().apply {
            message = "Property insight legal report details captured successfully"
            isSuccess = true
        }

    }

    fun processRequiredDocumentCallback(propertyInsightResponse: JSONObject,
                                        ePropertyInsight: PropertyInsight): LocalResponse {

        val remarkBuilder = StringBuilder()

        propertyInsightResponse.optJSONArray("documents")?.let {docArray ->

            docArray.forEachIndexed { index, singleDoc ->

                val doc = singleDoc as JSONObject
                val docName = doc.optString("name", NA)
                val docRemark = doc.optString("remarks", NA)

                if (!docName.isInvalid()) {
                    if (index == 0)
                        remarkBuilder.append("\n")
                    remarkBuilder.append("${index + 1}. $docName")
                    if (!docRemark.isInvalid())
                        remarkBuilder.append(" | Comment: $docRemark")
                    if (docArray.length() > (index + 1))
                        remarkBuilder.append("\n")
                }

            }

        }

        salesforceManager.updatePIRemarkOrRequiredDocAndNotify(ePropertyInsight, null,
            remarkBuilder.toString(), this)

        return LocalResponse().apply {
            message = "Property insight required document response captured successfully"
            isSuccess = true
        }

    }

    fun processPropertyInsightReportAndUpload(propertyInsight: PropertyInsight,
                                              reportType: EnPIReportType): Boolean {

        val docResponse = getDocument(propertyInsight)

        if (!docResponse.isSuccess) {
            log("processPropertyInsightReportAndUpload - failed to get document: ${docResponse.message}")
            return false
        }

        val docJson = JSONObject(docResponse.message)

        val fileBase64 = docJson.optString("base64", NA)
        val contentType = docJson.optString("content_type", NA)

        if (fileBase64.isInvalid() || contentType.isInvalid()) {
            log("processPropertyInsightReportAndUpload - Invalid fileBase64 or contentType")
            return false
        }

        if (!uploadPropertyInsightReportToS3(fileBase64, contentType,
                propertyInsight, reportType)) {
            log("processPropertyInsightReportAndUpload - Failed to upload property insight " +
                    "${reportType.value} to S3")
            return false
        }

        return true
    }

    fun sendPropertyInsightMail(pi: PropertyInsight, piReportType: EnPIReportType): Boolean {

        val sb = StringBuilder()

        sb.append("+++++++++++++++++++++++++++++++++++++++")

        if (piReportType == EnPIReportType.DIGITAL_SEARCH_REPORT)
            sb.append("\n Property insight search report is available.")
        else
            sb.append("\n Property insight legal report is available.")

        sb.append("\n+++++++++++++++++++++++++++++++++++++++")
        sb.append("\n\nProperty Insight ID: ${pi.id}")
        sb.append("\nProperty Insight URL: ${getSfBaseUrl()}${pi.sfPropertyInsightId}")

        if (!pi.sfPropertyId.isInvalid())
            sb.append("\nProperty URL: ${getSfBaseUrl()}${pi.sfPropertyId}")

        if (!pi.sfOpportunityId.isInvalid())
            sb.append("\nOpportunity URL: ${getSfBaseUrl()}${pi.sfOpportunityId}")

        if (piReportType == EnPIReportType.DIGITAL_SEARCH_REPORT) {

            documentHelper.getAttachmentFromDocJunction(
                EnDocumentType.PROPERTY_INSIGHT_DIGITAL, pi.id!!)?.let {
                val filePublicUrl = (if (appProperty.isProduction()) "https://one.homefirstindia.com/viewDocument?fid="
                else "https://test.homefirstindia.com/viewDocument?fid=") + it.fileIdentifier

                sb.append("\nSearch Report URL: $filePublicUrl")
                sb.append("\nSearch Report generated on: ${it.createDatetime}")

            }

        } else {

            documentHelper.getAttachmentFromDocJunction(
                EnDocumentType.PROPERTY_INSIGHT_LEGAL, pi.id!!)?.let {
                val filePublicUrl = (if (appProperty.isProduction()) "https://one.homefirstindia.com/viewDocument?fid="
                else "https://test.homefirstindia.com/viewDocument?fid=") + it.fileIdentifier

                sb.append("\nLegal Report URL: $filePublicUrl")
                sb.append("\nLegal Report generated on: ${it.createDatetime}")

            }

        }

        sb.append(
            "\n\n\nThis is an automatic email generated by Homefirst. Please Do Not Reply to this email."
        )

        if (!appProperty.isStrictProduction)
            return true

        val subject = if (piReportType == EnPIReportType.DIGITAL_SEARCH_REPORT)
            "Property Insight Report | Search Report" else "Property Insight Report | Legal Report"

        return mailHelper.sendMimeMessage(
            arrayOf(pi.requestee!!),
            subject,
            sb.toString(),
            cc = arrayOf(SUPERVISOR_MAIL_ID)
        )

    }

    fun sendPropertyInsightFailedMail(pi: PropertyInsight, failedMessage: String,
                                      subject: String = "Property Insight",
                                      requireDocument: Boolean = false): Boolean {

        val sb = StringBuilder()

        if (requireDocument) {
            sb.append("Document required for processing legal report")
            sb.append("\n\nRequired Documents: $failedMessage")
        } else {
            sb.append("Property insight remark available")
            sb.append("\nRemark: $failedMessage")
        }

        sb.append("\n\nProperty Insight ID: ${pi.id}")
        sb.append("\nProperty Insight URL: ${getSfBaseUrl()}${pi.sfPropertyInsightId}")

        if (!pi.sfPropertyId.isInvalid())
            sb.append("\nProperty URL: ${getSfBaseUrl()}${pi.sfPropertyId}")

        if (!pi.sfOpportunityId.isInvalid())
            sb.append("\nOpportunity URL: ${getSfBaseUrl()}${pi.sfOpportunityId}")

        sb.append(
            "\n\n\nThis is an automatic email generated by Homefirst. Please Do Not Reply to this email."
        )

        if (!appProperty.isStrictProduction)
            return true

        return mailHelper.sendMimeMessage(
            arrayOf(pi.requestee!!),
            subject,
            sb.toString(),
            cc = arrayOf(/*DEVELOPER_MAIL_ID, */SUPERVISOR_MAIL_ID)
        )

    }

    fun sendSearchReportSFUpdateFailedMail(pi: PropertyInsight, failedMessage: String): Boolean {

        val sb = StringBuilder()

        pi.attachment?.let {
            sb.append("Property insight search report available but failed to update it on salesforce")
        } ?: run { sb.append("Property insight search report details failed to update it on salesforce") }

        sb.append("\nReason: $failedMessage")
        sb.append("\n\nBelow is the available property insight search report details")
        sb.append("\n\nProperty Insight ID: ${pi.id}")
        sb.append("\nProperty Insight URL: ${getSfBaseUrl()}${pi.sfPropertyInsightId}")

        if (!pi.sfPropertyId.isInvalid())
            sb.append("\nProperty URL: ${getSfBaseUrl()}${pi.sfPropertyId}")

        if (!pi.sfOpportunityId.isInvalid())
            sb.append("\nOpportunity URL: ${getSfBaseUrl()}${pi.sfOpportunityId}")

        pi.propertyInsightResponse?.let {

            it.deedDate?.let { deDate->
                sb.append("\n\nDeed Date & Year: ${DateTimeUtils.getStringFromDateTimeString(
                    deDate, DateTimeFormat.yyyy_MM_dd_HH_mm_ss, DateTimeFormat.d_MMM_yyyy_hh_mm_a)}")
            }

            sb.append("\nDeed No: ${it.deedNo ?: NA}")
            sb.append("\nDeed Type: ${it.deedType ?: NA}")
            sb.append("\nProperty Address: ${it.address ?: NA}")
            sb.append("\nSRO (Source): ${it.sro ?: NA}")

            it.firstParty?.let { firstPartyList ->
                val nameBuilder = StringBuilder()
                firstPartyList.forEachIndexed { index, firstParty ->
                    nameBuilder.append("${index + 1}. $firstParty")
                    if (firstPartyList.size > (index + 1))
                        nameBuilder.append("\n")
                }
                if (nameBuilder.toString().isNotEmpty())
                    sb.append("\nFirstParty: $nameBuilder")
            }

            it.secondParty?.let { secondPartyList ->
                val nameBuilder = StringBuilder()
                secondPartyList.forEachIndexed { index, secondParty ->
                    nameBuilder.append("${index + 1}. $secondParty")
                    if (secondPartyList.size > (index + 1))
                        nameBuilder.append("\n")
                }
                if (nameBuilder.toString().isNotEmpty())
                    sb.append("\nSecondParty: $nameBuilder")
            }

            sb.append("\nLitigation Records: ${if (it.litigationRecords) "Yes" else "No"}")
            sb.append("\nMunicipal Property: ${if (it.municipalProperty) "Yes" else "No"}")
            sb.append("\nLand Records: ${if (it.landRecords) "Yes" else "No"}")
            sb.append("\nProperty Tax: ${if (it.propertyTax) "Yes" else "No"}")
            sb.append("\nAllotment Letters: ${if (it.allotmentLetters) "Yes" else "No"}")
            sb.append("\nOwner Match: ${if (it.ownerNameMatch) "Yes" else "No"}")

        }

        pi.attachment?.let {
            val filePublicUrl = (if (appProperty.isProduction()) "https://one.homefirstindia.com/viewDocument?fid="
            else "https://test.homefirstindia.com/viewDocument?fid=") + it.fileIdentifier
            sb.append("\nSearch Report URL : $filePublicUrl")
        }

        pi.reportGeneratedOn?.let { reDate->
            sb.append("\nSearch Report Generated On: ${DateTimeUtils.getStringFromDateTimeString(
                reDate, DateTimeFormat.yyyy_MM_dd_HH_mm_ss, DateTimeFormat.d_MMM_yyyy_hh_mm_a)}")
        }

        sb.append(
            "\n\n\n This is an automatic email generated by Homefirst. Please Do Not Reply to this email."
        )

        if (!appProperty.isStrictProduction)
            return true

        return mailHelper.sendMimeMessage(
            arrayOf(pi.requestee!!),
            "Property Insight Search Report | Salesforce Update Failed",
            sb.toString(),
            cc = arrayOf(DEVELOPER_MAIL_ID, SUPERVISOR_MAIL_ID)
        )

    }

    fun sendLegalReportSFUpdateFailedMail(pi: PropertyInsight, failedMessage: String): Boolean {

        val sb = StringBuilder()

        val attach = documentHelper.getAttachmentFromDocJunction(
            EnDocumentType.PROPERTY_INSIGHT_LEGAL, pi.id!!)

        attach?.let {
            sb.append("Property insight legal report available but failed to update it on salesforce")
        } ?: run { sb.append("Property insight legal report details failed to update it on salesforce") }

        sb.append("\nReason: $failedMessage")
        sb.append("\n\nBelow is the available property insight legal report details")
        sb.append("\n\nProperty Insight ID: ${pi.id}")
        sb.append("\nProperty Insight URL: ${getSfBaseUrl()}${pi.sfPropertyInsightId}")

        if (!pi.sfPropertyId.isInvalid())
            sb.append("\nProperty URL: ${getSfBaseUrl()}${pi.sfPropertyId}")

        if (!pi.sfOpportunityId.isInvalid())
            sb.append("\nOpportunity URL: ${getSfBaseUrl()}${pi.sfOpportunityId}")

        attach?.let {
            val filePublicUrl = (if (appProperty.isProduction()) "https://one.homefirstindia.com/viewDocument?fid="
            else "https://test.homefirstindia.com/viewDocument?fid=") + it.fileIdentifier
            sb.append("\nLegal Report URL: $filePublicUrl")
            it.createDatetime.let { leDate->
                sb.append("\nLegal Report Generated On: ${DateTimeUtils.getStringFromDateTimeString(
                    leDate, DateTimeFormat.yyyy_MM_dd_HH_mm_ss, DateTimeFormat.d_MMM_yyyy_hh_mm_a)}")
            }
        }

        sb.append(
            "\n\n\n This is an automatic email generated by Homefirst. Please Do Not Reply to this email."
        )

        if (!appProperty.isStrictProduction)
            return true

        return mailHelper.sendMimeMessage(
            arrayOf(pi.requestee!!),
            "Property Insight Legal Report | Salesforce Update Failed",
            sb.toString(),
            cc = arrayOf(DEVELOPER_MAIL_ID, SUPERVISOR_MAIL_ID)
        )

    }

}