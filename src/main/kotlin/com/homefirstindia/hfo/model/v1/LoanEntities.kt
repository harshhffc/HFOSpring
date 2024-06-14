package com.homefirstindia.hfo.model.v1

import com.homefirstindia.hfo.utils.NA
import com.homefirstindia.hfo.utils.isInvalid
import org.json.JSONObject


class Loan {

    var sfId: String? = null
    var loanAccountNumber: String? = null
    var productType: String? = null
    var productSubType: String? = null
    var opportunityId: String? = null
    var opportunityName: String? = null
    var loanStatus: String? = null
    var loanAmount: Double? = null
    var emiAmount: Double? = null
    var totalTenure: Int? = null
    var remainingTenure: Int? = null
    var interestRate: Double? = null
    var disbursalStatus: String? = null
    var disbursedAmount: Double? = null
    var principalAmount: Double? = null

    fun parseLoanForSF(loanJson: JSONObject?) : Loan? {

        loanJson?.let {

            sfId = loanJson.optString("Id")
            loanAccountNumber = loanJson.optString("Name")

            val opportunityJson = loanJson.optJSONObject("Opportunity__r")

            opportunityJson?.let {

                opportunityJson.optString("Id", NA)?.let {
                    if (!it.isInvalid()) opportunityId = it
                }

                opportunityJson.optString("Name", NA)?.let {
                    if (!it.isInvalid()) opportunityName = it
                }

            }

            loanStatus = loanJson.optString("loan__Loan_Status__c")

            loanJson.optJSONObject("loan__Loan_Product_Name__r")?.let {
                it.optString("Name", NA)?.let { prodType ->
                    productType = prodType
                }
            }

            productSubType = loanJson.optString("Sub_Product_Type__c", NA)
            loanAmount = loanJson.optDouble("loan__Loan_Amount__c", -1.0)
            totalTenure = loanJson.optInt("loan__Number_of_Installments__c", -1)
            interestRate = loanJson.optDouble("loan__Interest_Rate__c", -1.0)
            disbursalStatus = loanJson.optString("loan__Disbursal_Status__c", NA)
            disbursedAmount = loanJson.optDouble("loan__Disbursed_Amount__c", -1.0)
            principalAmount = loanJson.optDouble("loan__Principal_Remaining__c", -1.0)

            return this

        }

        return null

    }

}