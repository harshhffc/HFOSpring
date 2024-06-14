package com.homefirstindia.hfo.model.v1

import com.homefirstindia.hfo.helper.v1.DocumentHelper
import com.homefirstindia.hfo.security.AppProperty
import com.homefirstindia.hfo.utils.*
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.GenericGenerator
import org.json.JSONArray
import org.json.JSONObject
import javax.persistence.*


@Entity
@Table(name = "`PropertyInsight`")
class PropertyInsight {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    var id: String? = null

    var sfPropertyId: String? = null

    var sfOpportunityId: String? = null

    @Column(nullable = true)
    var sfPropertyInsightId: String? = null

    @Column(nullable = false)
    var requestee: String? = null

    @ColumnDefault("0")
    var requesteeNotified = false

    @ColumnDefault("-1")
    var tealRequestId = -1

    var queryId: String? = null

    var status: String? = null

    @Column(columnDefinition = "JSON")
    var rawRequestJson: String? = null

    @Column(columnDefinition = "JSON")
    var rawResponseJson: String? = null

    @ColumnDefault("0")
    var pdfReportUpdatedOnSF = false

    @Column(columnDefinition = "DATETIME")
    var reportGeneratedOn: String? = null

    @Column(columnDefinition = "DATETIME")
    var legalReportGeneratedOn: String? = null

    @ColumnDefault("0")
    var generateLegalReport = false

    @ColumnDefault("0")
    var jsonDataUpdatedOnSF = false

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "attachmentId", referencedColumnName = "id")
    var attachment: Attachment? = null

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var createDatetime = DateTimeUtils.getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME")
    var updateDatetime: String? = null

    @Transient
    var propertyRequest: PropertyInsightRequest? = null

    @Transient
    var propertyInsightResponse: PropertyInsightResponse? = null


    fun mandatoryFieldsCheck() : LocalResponse {

        val localResponse = LocalResponse()
            .setError(Errors.INVALID_DATA.value)
            .setAction(Actions.FIX_RETRY.value)

        propertyRequest ?: run { localResponse.message = "Invalid propertyRequest." }

        val prLocalResponse = propertyRequest?.mandatoryFieldsCheck()

        prLocalResponse?.let {
            if (!it.isSuccess)
                return it
        }

        when {
            sfPropertyInsightId.isInvalid() -> localResponse.message = "Invalid sfPropertyInsightId."
            requestee.isInvalid() -> localResponse.message = "Invalid requestee."
            else -> {
                localResponse.apply {
                    message = NA
                    error = NA
                    action = NA
                    isSuccess = true
                }
            }
        }

        return localResponse
    }

    fun getPropInsightRequestJson(): JSONObject? {

        propertyRequest?.let { propertyInsightRequest ->
            val applicantJsonArray = JSONArray().apply {
                propertyInsightRequest.propertyOwners?.forEach { propertyOwner ->
                    JSONObject().let { jsonObject ->
                        jsonObject.put("name", propertyOwner.nameOfOwner)
                        if (!propertyOwner.panCardNumber.isInvalid())
                            jsonObject.put("pan", propertyOwner.panCardNumber)
                        this.put(jsonObject)
                    }
                }
            }

            val propertyJson = JSONObject().let { propJson ->
                propJson.put("address", propertyInsightRequest.propertyAddress)
                propJson.put("type", propertyInsightRequest.typeOfProperty)
                propJson.put("state", NA)
                propJson.put("district", NA)
            }

            val reportTypes = ArrayList<String>()
            reportTypes.add("Digital Property Search Report")

            if (generateLegalReport)
                reportTypes.add("Legal Due Diligence Report")

            val referenceJson = JSONObject().put("case_number", propertyInsightRequest.referenceNumber)

            val propertyRequestJson = JSONObject()
            propertyRequestJson.put("applicant_details", applicantJsonArray)
            propertyRequestJson.put("property_details", propertyJson)
            propertyRequestJson.put("reports_requested", reportTypes)
            propertyRequestJson.put("reference_data", referenceJson)

            return propertyRequestJson
        }

        return null
    }

    fun getPropertyInsightSfJson(isProduction: Boolean) : JSONObject? {

        propertyInsightResponse?.let {propInsightResponse ->

            val sfJson = JSONObject()

            propInsightResponse.deedDate?.let {
                sfJson.put("Deed_Date_year__c", DateTimeUtils.getDateForSalesforce(it))
            }
            propInsightResponse.deedNo?.let {
                sfJson.put("Deed_No__c", it)
            }
            propInsightResponse.deedType?.let {
                sfJson.put("Deed_Type__c", it)
            }
            propInsightResponse.address?.let {
                sfJson.put("Property_Address__c", it)
            }
            propInsightResponse.sro?.let {
                sfJson.put("SRO_Source__c", it)
            }

            propInsightResponse.firstParty?.let {
                val nameBuilder = StringBuilder()
                it.forEachIndexed { index, firstParty ->
                    nameBuilder.append("${index + 1}. $firstParty")
                    if (it.size > (index + 1))
                        nameBuilder.append("\n")
                }
                if (nameBuilder.toString().isNotEmpty())
                    sfJson.put("FirstParty__c", nameBuilder.toString())
            }


            propInsightResponse.secondParty?.let {
                val nameBuilder = StringBuilder()
               it.forEachIndexed { index, firstParty ->
                    nameBuilder.append("${index + 1}. $firstParty")
                    if (it.size > (index + 1))
                        nameBuilder.append("\n")
                }
                if (nameBuilder.toString().isNotEmpty())
                    sfJson.put("SecondParty__c", nameBuilder.toString())
            }

            sfJson.put("Litigation_Records__c", if (propInsightResponse.litigationRecords) "Yes" else "No")
            sfJson.put("Municipal_Property__c", if (propInsightResponse.municipalProperty) "Yes" else "No")
            sfJson.put("Land_Records__c", if (propInsightResponse.landRecords) "Yes" else "No")
            sfJson.put("Property_Tax__c", if (propInsightResponse.propertyTax) "Yes" else "No")
            sfJson.put("Allotment_Letters__c", if (propInsightResponse.allotmentLetters) "Yes" else "No")

           val baseUrl = if (isProduction) "https://one.homefirstindia.com/viewDocument?fid="
           else "https://test.homefirstindia.com/viewDocument?fid="

            attachment?.let {
                val filePublicUrl = "$baseUrl${it.fileIdentifier}"
                sfJson.put("Report_URL__c", filePublicUrl)
            }

            if (!sfPropertyId.isInvalid())
                sfJson.put("Property__c", sfPropertyId)

            if (!sfOpportunityId.isInvalid())
                sfJson.put("Opportunity__c", sfOpportunityId)

            sfJson.put("Owner_Match__c", propInsightResponse.ownerNameMatch)

            reportGeneratedOn?.let {
                sfJson.put("Report_Date_Time__c", DateTimeUtils.getDateForSalesforce(it))
            }

            return sfJson

        }

        return null

    }

    fun getLegalReportSfJson(documentHelper: DocumentHelper, appProperty: AppProperty) : JSONObject{

        val sfJson = JSONObject()

        val baseUrl = if (appProperty.isProduction()) "https://one.homefirstindia.com/viewDocument?fid="
        else "https://test.homefirstindia.com/viewDocument?fid="

        documentHelper.getAttachmentFromDocJunction(EnDocumentType.PROPERTY_INSIGHT_LEGAL,
            id!!)?.let {
            val filePublicUrl = "$baseUrl${it.fileIdentifier}"
            sfJson.put("Legal_Report_URL__c", filePublicUrl)
        }

        legalReportGeneratedOn?.let {
            sfJson.put("Legal_report_date_and_time__c", DateTimeUtils.getDateForSalesforce(it))
        }

        return sfJson
    }

}

class PropertyInsightRequest {
    var typeOfProperty: String? = null
    var propertyAddress: String? = null
    var propertyOwners: ArrayList<PropertyOwner>? = null
    var referenceNumber: String? = null

    fun mandatoryFieldsCheck() : LocalResponse {

        val localResponse = LocalResponse()
            .setError(Errors.INVALID_DATA.value)
            .setAction(Actions.FIX_RETRY.value)

        when {
            typeOfProperty.isInvalid() -> localResponse.message = "Invalid typeOfProperty."
            propertyAddress.isInvalid() -> localResponse.message = "Invalid propertyAddress."
            propertyOwners?.isEmpty() == true -> localResponse.message = "Atleast one owner information required"
            else -> {
                localResponse.apply {
                    message = NA
                    error = NA
                    action = NA
                    isSuccess = true
                }
            }
        }

        return localResponse
    }

}

class PropertyOwner {
    var nameOfOwner: String? = null
    var aadhaarNumber: String? = null
    var panCardNumber: String? = null
}

class PropertyInsightDocumentDTO {

    var sfPropertyInsightId: String? = null
    var fileName: String? = null
    var fileData: String? = null
    var fileType: String? = null

    fun mandatoryFieldsCheck() : LocalResponse {

        val localResponse = LocalResponse()
            .setError(Errors.INVALID_DATA.value)
            .setAction(Actions.FIX_RETRY.value)

        when {
            sfPropertyInsightId.isInvalid() -> localResponse.message = "Invalid sfPropertyInsightId."
            fileName.isInvalid() -> localResponse.message = "Invalid fileName."
            fileData.isInvalid() -> localResponse.message = "Invalid fileData."
            else -> {
                localResponse.apply {
                    message = NA
                    error = NA
                    action = NA
                    isSuccess = true
                }
            }
        }

        return localResponse
    }

    fun getAddDocRequestJson(): JSONObject {
        val propertyRequestJson = JSONObject()
        propertyRequestJson.put("name", fileName)
        propertyRequestJson.put("file_type", fileType)
        propertyRequestJson.put("base64", fileData)

        return propertyRequestJson
    }

}

class PropertyInsightResponse {

    var ownerNameMatch = false
    var firstParty: ArrayList<String>? = null
    var secondParty: ArrayList<String>? = null
    var deedNo: String? = null
    var deedType: String? = null
    var deedDate: String? = null
    var year: String? = null
    var address: String? = null
    var sro: String? = null
    var litigationRecords = false
    var municipalProperty = false
    var landRecords = false
    var propertyTax = false
    var allotmentLetters = false
    var reportName: String? = null
    var isInsightAvailable = false

    fun parsePropertyInsightReportDataResponse(response: JSONObject) {

        ownerNameMatch = response.optBoolean("applicant_owner_name_match", false)

        response.optJSONArray("ownership_records")?.let {ownerJsonArray ->

            if (ownerJsonArray.length() < 0)
                return@let

            val ownerRecordJsonObject = ownerJsonArray.getJSONObject(0)

            ownerRecordJsonObject?.let {

                it.optJSONArray("first_party_names")?.forEach { partyNames ->
                    firstParty = ArrayList()
                    firstParty!!.add(partyNames as String)
                }

                it.optJSONArray("second_party_names")?.forEach {partyNames ->
                    secondParty = ArrayList()
                    secondParty!!.add(partyNames as String)
                }

                it.optString("address")?.let {add ->
                    if (!add.isInvalid()) address = add
                }

                it.optString("registered_at")?.let {regAt ->
                    if (!regAt.isInvalid()) sro = regAt
                }

                it.optString("registration_number")?.let {regNum ->
                    if (!regNum.isInvalid()) deedNo = regNum
                }

                it.optString("transaction_type")?.let {regType ->
                    if (!regType.isInvalid()) deedType = regType
                }

                it.optString("registration_date")?.let {regDate ->
                    if (!regDate.isInvalid() && DateTimeUtils.isValid(regDate, DateTimeFormat.dd_MM_yyyy)) {
                        deedDate = DateTimeUtils.getStringFromDateTimeString(
                            regDate, DateTimeFormat.dd_MM_yyyy, DateTimeFormat.yyyy_MM_dd_HH_mm_ss)
                    }
                }

                it.optString("digitization_after")?.let {yr ->
                    if (!yr.isInvalid()) year = yr
                }

            }

        }

        response.optJSONArray("municipal_records")?.let {
           municipalProperty = it.length() > 0
        }

        response.optJSONArray("revenue_land_records")?.let {
            landRecords = it.length() > 0
        }

        response.optJSONArray("litigation_records")?.let {
            litigationRecords = it.length() > 0
        }

    }

    fun parsePropertyInsightReportFile(responseArray: JSONArray) {

        responseArray.let {

            for (i in 0 until it.length()) {

                val report = it.getJSONObject(i)

                if(report.optString("name") == "Digital Property Search Report") {

                    val filesJsonArray = report.optJSONArray("files")

                    if (filesJsonArray.length() > 0)
                        reportName = filesJsonArray.getJSONObject(0).optString("filename")
                }

            }

        }

    }

    fun parsePropertyInsightReportFile(fReportName: String, response: JSONObject) {

        response.let { fileJson ->
            fileJson.optString("name")?.let { name ->
                if (name == fReportName) {
                    val fileName = fileJson.optString("filename")
                    reportName = fileName
                }
            }
        }

    }

}
