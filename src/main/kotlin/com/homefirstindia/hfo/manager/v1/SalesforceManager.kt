package com.homefirstindia.hfo.manager.v1

import com.homefirstindia.hfo.helper.v1.DocumentHelper
import com.homefirstindia.hfo.helper.v1.PropertyInsightHelper
import com.homefirstindia.hfo.model.v1.Loan
import com.homefirstindia.hfo.model.v1.PropertyInsight
import com.homefirstindia.hfo.model.v1.salesforce.ServiceRequest
import com.homefirstindia.hfo.networking.v1.EnSFObjects
import com.homefirstindia.hfo.networking.v1.EnSfObjectName
import com.homefirstindia.hfo.networking.v1.SFConnection
import com.homefirstindia.hfo.security.AppProperty
import com.homefirstindia.hfo.utils.*
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


@Component
class SalesforceManager(
    @Autowired val sfConnection: SFConnection,
    @Autowired val appProperty: AppProperty,
    @Autowired val documentHelper: DocumentHelper
) {
    private fun log(value: String) = LoggerUtils.log("v1/${this.javaClass.simpleName}.$value")
    private fun printLog(value: String) = LoggerUtils.printLog("v1/${this.javaClass.simpleName}.$value")

    @Throws(Exception::class)
    fun fetchLoanDetails(loanAccountNumber: String?): Loan? {

        val sb = StringBuilder()
        sb.append("SELECT")
        sb.append(" Id,")
        sb.append(" Name,")
        sb.append(" Opportunity__c,")
        sb.append(" Opportunity__r.Id,")
        sb.append(" Opportunity__r.Name,")
        sb.append(" loan__Loan_Status__c,")
        sb.append(" loan__Loan_Product_Name__r.Name,")
        sb.append(" Sub_Product_Type__c,")
        sb.append(" loan__Loan_Amount__c,")
        sb.append(" loan__Number_of_Installments__c,")
        sb.append(" loan__Interest_Rate__c,")
        sb.append(" loan__Disbursal_Status__c,")
        sb.append(" loan__Disbursed_Amount__c,")
        sb.append(" loan__Principal_Remaining__c")
        sb.append(" FROM loan__Loan_Account__c")
        sb.append(" WHERE Name = '")
        sb.append(loanAccountNumber)
        sb.append("'")

        val json = sfConnection.get(sb.toString())

        json?.let {
            if (it.getInt("totalSize") > 0) {
                val loanJson = json.getJSONArray("records").getJSONObject(0)

                return Loan().parseLoanForSF(loanJson!!)
            }
        }

        return null

    }

    @Throws(java.lang.Exception::class)
    fun getEmiAmount(loanAccountNumber: String?): Double? {

        val sb = java.lang.StringBuilder()
        sb.append("SELECT")
        sb.append(" loan__RSS_Repayment_Amt__c FROM loan__Repayment_Schedule_Summary__c")
        sb.append(" WHERE loan__RSS_Loan_Account__r.Name='")
        sb.append(loanAccountNumber)
        sb.append("' AND loan__RSS_Primary_flag__c = true ORDER BY loan__RSS_No_Of_Pmts__c desc limit 1")

        val emiJson = sfConnection.get(sb.toString())

        emiJson?.let {
            if (it.getInt("totalSize") > 0) {
                val records = emiJson.getJSONArray("records")
                val current = records.getJSONObject(0)
                return current.optDouble("loan__RSS_Repayment_Amt__c", 0.0)
            }
        }

        return null
    }

    @Throws(Exception::class)
    fun updatePropertyInsight(
        propertyInsight: PropertyInsight,
    ): LocalResponse {

        val uri = EnSFObjects.PROPERTY_INSIGHT.value + propertyInsight.sfPropertyInsightId

        val sfResponse = sfConnection.patch(propertyInsight.getPropertyInsightSfJson(appProperty.isProduction())!!, uri)

        if (sfResponse?.isSuccess == true) {

            println("updatePropertyInsight - SFResponse: ${sfResponse.message}")
            println(
                "updatePropertyInsight - SF Property insight updated " +
                        "successfully: ${propertyInsight.sfPropertyInsightId}"
            )

            return LocalResponse().apply {
                isSuccess = true
            }

        } else {
            printLog("updatePropertyInsight - SFResponse: ${sfResponse!!.message}")
            printLog(
                "updatePropertyInsight - failed to update property insight " +
                        "on SF : ${propertyInsight.sfPropertyInsightId}"
            )
            return LocalResponse().apply {
                isSuccess = false
                message = sfResponse.message
            }
        }

    }

    @Throws(Exception::class)
    fun updatePropertyInsightLegalReport(
        propertyInsight: PropertyInsight,
    ): LocalResponse {

        val uri = EnSFObjects.PROPERTY_INSIGHT.value + propertyInsight.sfPropertyInsightId

        val sfResponse = sfConnection.patch(
            propertyInsight.getLegalReportSfJson(documentHelper, appProperty),
            uri
        )

        if (sfResponse?.isSuccess == true) {

            println("updatePropertyInsight - SFResponse: ${sfResponse.message}")
            println("updatePropertyInsight - SF Property insight updated Successfully: ${propertyInsight.sfPropertyInsightId}")

            return LocalResponse().apply {
                isSuccess = true
            }

        } else {
            printLog("updatePropertyInsight - SFResponse: ${sfResponse!!.message}")
            printLog("updatePropertyInsight - failed to update property insight on SF : ${propertyInsight.sfPropertyInsightId}")
            return LocalResponse().apply {
                isSuccess = false
                message = sfResponse.message
            }
        }

    }

    @Throws(Exception::class)
    fun updatePIRemarkOrRequiredDocAndNotify(
        propertyInsight: PropertyInsight,
        remark: String?,
        requiredDoc: String?,
        propertyInsightHelper: PropertyInsightHelper
    ) {

        val uri = EnSFObjects.PROPERTY_INSIGHT.value + propertyInsight.sfPropertyInsightId

        val remarkJson = JSONObject().apply {
            remark?.let {
                this.put("Remarks__c", it)
            }
            requiredDoc?.let {
                this.put("Required_docs_list__c", it)
            }
        }

        val sfResponse = sfConnection.patch(remarkJson, uri)

        if (sfResponse?.isSuccess == true) {

            println("updatePropertyInsightRemarkAndSendMail - SFResponse: ${sfResponse.message}")
            println("updatePropertyInsightRemarkAndSendMail - SF Property insight remark updated Successfully: ${propertyInsight.sfPropertyInsightId}")

        } else {

            printLog("updatePropertyInsightRemarkAndSendMail - SFResponse: ${sfResponse!!.message}")
            printLog("updatePropertyInsightRemarkAndSendMail - failed to update property insight remark on SF : ${propertyInsight.sfPropertyInsightId} | Remark: $remark")

        }

        val msg = remark ?: run {
            requiredDoc
        } ?: DEFAULT_ERROR_MESSAGE

        val subject = requiredDoc?.let {
            "Property Insight | Document required"
        } ?: "Property Insight Remark"

        propertyInsightHelper.sendPropertyInsightFailedMail(propertyInsight, msg,
            subject, requiredDoc?.let { true } ?: false)

    }


    @Throws(java.lang.Exception::class)
    fun getLAIListByMobileNumber(mobileNumber: String?): ArrayList<String>? {

        val sb = java.lang.StringBuilder()
        sb.append(" SELECT")
        sb.append(" Name from loan__Loan_Account__c")
        sb.append(" WHERE (Opportunity__r.Primary_Contact__r.MobilePhone ='")
        sb.append(mobileNumber)
        sb.append("'")
        sb.append(" OR Opportunity__r.Co_Applicant_Name1__r.MobilePhone ='")
        sb.append(mobileNumber)
        sb.append("'")
        sb.append(" OR Opportunity__r.Co_Applicant_Name_2__r.MobilePhone ='")
        sb.append(mobileNumber)
        sb.append(
            "') AND loan__Loan_Status__c NOT IN " +
                    "('Closed - Obligations met', " +
                    "'Closed- Written Off','Canceled' , " +
                    "'Partial Application' , " +
                    "'Pending Approval' ," +
                    "'Sent to Reviewer')"
        )

        val emiJson = sfConnection.get(sb.toString())

        val loans = ArrayList<String>()

        return if (null != emiJson) {
            val totalCount: Int = emiJson.getInt("totalSize")
            if (totalCount > 0) {
                val records = emiJson.getJSONArray("records")
                for (i in 0 until records.length()) {
                    val current = records.getJSONObject(i)
                    loans.add(current.getString("Name"))
                }
            }
            loans
        } else
            ArrayList()


    }


    fun createCase(serviceRequest: ServiceRequest): LocalResponse {


        val metaData = getAccountNameAndOpportunity(serviceRequest.loanAccountNumber!!)

        if (metaData == null || metaData.length() == 0) {
            return LocalResponse().apply {
                isSuccess = false
                this.message = "loan Account Number not match ."
            }
        }


        val serviceRequestObject = JSONObject()

        serviceRequestObject.apply {
            put("Case_Type__c", "Query")
            put("Master_Case_Reason__c", serviceRequest.masterCaseReason)
            put("Case_Reason_sub__c", serviceRequest.caseReason)
            put("Status", "New")
            put("Origin", "Web")
            put("Subject", serviceRequest.subject)
            put("Description", serviceRequest.description)
            put("Priority", "Low")
            put("Reference_LAI__c", serviceRequest.loanAccountNumber)
            //serviceRequestObject.put(LOAN_ACCOUNT_NUMBER,serviceRequest.loanAccountNumber)

            metaData.let {
                put("Opportunity__c", it.optString("opportunityId"))
                put("AccountId", it.optString("accountId"))
            }

        }

        val contactId = metaData.optString("contactId")
        if (!contactId.isInvalid())
            serviceRequestObject.put("ContactId", contactId)


        sfConnection.post(serviceRequestObject, EnSFObjects.CASE).run {

            if (isSuccess) {

                JSONObject(stringEntity).let {

                    val sfCaseId = it.getString("id")

                    log("createCase - New case created successfully. sfID : $sfCaseId")

                    return LocalResponse().apply {
                        isSuccess = true
                        message = sfCaseId
                    }

                }

            } else {

                log("createCase - Failed to create Case. LAI : ${serviceRequest.loanAccountNumber} | Error : ${this@run.message}")

                return LocalResponse().apply {
                    isSuccess = false
                    this.message = "Failed to create a case, please try again"
                }

            }

        }

    }

    @Throws(java.lang.Exception::class)
    fun getAccountNameAndOpportunity(loanAccountNumber: String): JSONObject? {
        val responseJson = JSONObject()

        val query =
            ("select loan__Contact__c , Opportunity__c,loan__Account__c , X_Sell_Products__c, loan__Loan_Product_Name__c from loan__Loan_Account__c where Name='"
                    + loanAccountNumber + "'")

        val requestsJson = sfConnection.get(query)
        //val requestsJson: JSONObject = getSalesforceData(getModifiedQuery(query)) ?: return null
        if (requestsJson != null) {
            if (requestsJson.getInt("totalSize") > 0) {
                val records = requestsJson.getJSONArray("records")
                for (i in 0 until records.length()) {
                    val current = records.getJSONObject(i)
                    val accountId = current.optString("loan__Account__c", NA)
                    val opportunityId = current.optString("Opportunity__c", NA)
                    val contactId = current.optString("loan__Contact__c", NA)
                    val xSellProductId = current.optString("X_Sell_Products__c", NA)
                    val loanProductType = current.optString("loan__Loan_Product_Name__c", NA)
                    if (accountId != NA && opportunityId != NA) {
                        responseJson.put("accountId", accountId)
                        responseJson.put("opportunityId", opportunityId)
                        responseJson.put("contactId", contactId)
                        responseJson.put("xSellProductId", xSellProductId)
                        responseJson.put("loanProductType", loanProductType)
                        break
                    }
                }
            }
        }
        return responseJson
    }

    @Throws(Exception::class)
    fun fetchDisbursmentDetails(loanAccountNumber: String?): Triple<String, String, String> {


        val fieldList = ArrayList<String>().apply {
            add("Id")
            add("Name")
            add("loan__Disbursal_Status__c")
            add("loan__Disbursed_Amount__c")
            add("Percentage_Disbursed__c")
        }


        val sfResponseJson = getClContractLoanAccountObjectFields(loanAccountNumber, fieldList)

        sfResponseJson?.let {
            if (it.getInt("totalSize") > 0) {
                val loanJson = sfResponseJson.getJSONArray("records").getJSONObject(0)

                val loanDisbursalStatus = loanJson.optString("loan__Disbursal_Status__c")
                val percentageDisbursed = loanJson.optString("Percentage_Disbursed__c")
                val loanDisbursedAmount = loanJson.optString("loan__Disbursed_Amount__c")
                return Triple(loanDisbursalStatus, percentageDisbursed, loanDisbursedAmount)
            }
        }

        return Triple(NA, NA, NA)

    }

    @Throws(Exception::class)
    fun fetchEmiDueAmountDetails(loanAccountNumber: String): Double {

        val sfResponseJson = getSFObjectDetails(listOf("loan__Product_Type__c", "Sub_Product_Type__c"),
            EnSfObjectName.LOAN_ACCOUNT.value, "Name", loanAccountNumber)

        sfResponseJson?.let {

            if (it.getInt("totalSize") > 0) {

                val loanJson = sfResponseJson.getJSONArray("records").getJSONObject(0)

                val subProductType = loanJson.optString("Sub_Product_Type__c")

                return if (isLoanTopUp(subProductType)) fetchTopUpLoanEmiDueAmount(loanAccountNumber)
                else fetchHomeLoanEmiDueAmount(loanAccountNumber)

            }
        }

        return 0.0

    }

    fun fetchHomeLoanEmiDueAmount(loanAccountNumber: String): Double {

        val sfResponseJson = getSFObjectDetails(listOf("Payment_Excess_Shortfall__c"),
            EnSfObjectName.COLLECTION.value, "CL_Contract_No_LAI__c", loanAccountNumber)

        sfResponseJson?.let {

            if (it.getInt("totalSize") > 0) {

                val loanJson = sfResponseJson.getJSONArray("records").getJSONObject(0)
                return loanJson.optDouble("Payment_Excess_Shortfall__c", 0.0)

            }

        }

        return 0.0
    }

    fun fetchTopUpLoanEmiDueAmount(loanAccountNumber: String): Double {

        val sfResponseJson = getSFObjectDetails(listOf("Top_Up_Payment_Excess_Shortfall__c"),
            EnSfObjectName.COLLECTION.value, "Top_Up_CL_Contract_No_LAI__c", loanAccountNumber)

        sfResponseJson?.let {

            if (it.getInt("totalSize") > 0) {

                val loanJson = sfResponseJson.getJSONArray("records").getJSONObject(0)

                return loanJson.optDouble("Top_Up_Payment_Excess_Shortfall__c", 0.0)

            }

        }

        return 0.0

    }

    fun getCollectionObjectFields(
        loanAccountNumber: String?,
        whereConditionField: String,
        dynamicObjectFields: List<String>
    ): JSONObject? {
        val queryBuilder = StringBuilder()

        queryBuilder.append("SELECT ${dynamicObjectFields.joinToString()} ")
        queryBuilder.append("FROM Collection__c WHERE ${whereConditionField}='$loanAccountNumber' ")
        queryBuilder.append("order by CreatedDate desc limit 1")
        return sfConnection.get(queryBuilder.toString())

    }

    fun getClContractLoanAccountObjectFields(
        loanAccountNumber: String?,
        dynamicObjectFields: List<String>
    ): JSONObject? {
        val query =
            "SELECT ${dynamicObjectFields.joinToString()} FROM loan__Loan_Account__c WHERE Name='$loanAccountNumber' order by CreatedDate desc limit 1"

        return sfConnection.get(query)
    }

    fun getSFObjectDetails(
        dynamicObjectFields: List<String>,
        objectName: String,
        whereFieldName: String,
        whereFieldValue: String,
    ): JSONObject? {

        val queryBuilder = StringBuilder()

        queryBuilder.append("SELECT ${dynamicObjectFields.joinToString()} ")
        queryBuilder.append("FROM $objectName WHERE ${whereFieldName}='$whereFieldValue' ")
        queryBuilder.append("order by CreatedDate desc limit 1")

        return sfConnection.get(queryBuilder.toString())

    }
}