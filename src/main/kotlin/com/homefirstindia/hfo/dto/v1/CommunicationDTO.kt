package com.homefirstindia.hfo.dto.v1

import com.homefirstindia.hfo.helper.v1.CommunicationSearchQueryCriteria
import com.homefirstindia.hfo.utils.*
import com.opencsv.bean.CsvBindByName
import org.json.JSONObject
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root

class SMSStats() {

    var smsCampaignIds: String? = null
    var failedCount: Long? = null
    var successCount: Long? = null
    var totalCount: Long? = null

    constructor(
        smsCampaignIds: String?,
        successCount: Long?,
        failedCount: Long?,
        totalCount: Long?,
    ) : this() {
        this.smsCampaignIds = smsCampaignIds
        this.failedCount = failedCount
        this.successCount = successCount
        this.totalCount = totalCount
    }

}
class CallLogExport() {

    @CsvBindByName(column = "ID", required = true)
    var id: String? = null

    @CsvBindByName(column = "SF Id")
    var objectId: String? = null

    @CsvBindByName(column = "Source")
    var source: String? = null

    @CsvBindByName(column = "Owner name")
    var userName: String? = null

    @CsvBindByName(column = "Owner email")
    var userEmail: String? = null

    @CsvBindByName(column = "Caller Status")
    var callerStatus: String? = null

    @CsvBindByName(column = "Receiver Status")
    var receiverStatus: String? = null

    @CsvBindByName(column = "Create Datetime")
    var createDatetime: String? = null

    @CsvBindByName(column = "Remark")
    var remark: String? = null

    constructor(
        id: String?,
        objectId: String?,
        source: String?,
        userName: String?,
        userEmail: String?,
        callerStatus: String?,
        receiverStatus: String?,
        createDatetime: String?,
        remark: String?,
    ) : this() {
        this.id = id
        this.objectId = objectId
        this.source = source
        this.userName = userName
        this.userEmail = userEmail
        this.callerStatus = callerStatus
        this.receiverStatus = receiverStatus
        this.createDatetime = createDatetime
        this.remark = remark
    }

}

class CallActivity() {

    var id: String? = null
    var type: String? = null
    var page: JSONObject? = null
}

data class CallLogList(
    var advanceFilter: AdvanceFilter?,
    var builder: CriteriaBuilder?,
    var query: CriteriaQuery<*>?,
    var r: Root<*>?,
    var searchCollection: CommunicationSearchQueryCriteria?,
)

class CallLogDTO {
    var id: String? = null
    var createDateTime: String? = null
    var type: String? = null
    var callStartTime: String? = null
    var callEndTime: String? = null
    var callerProvider: String? = null
    var callerStatus: String? = null
    var receiverStatus: String? = null
    var receiver: String? = null
    var caller: String? = null
    var callerLocation: String? = null
    var source: String? = null
    var status: String? = null
    var updateDatetime: String? = null
    var userName: String? = null
    var objectId: String? = null
    var objectName: String? = null
    var userId: String? = null
    var durationInSec: Int = 0
    var remark: String? = null

    constructor(
        id: String?,
        createDatetime: String?,
        type: String?,
        callStartTime: String?,
        callEndTime: String?,
        callerProvider: String?,
        callerStatus: String?,
        receiverStatus: String?,
        receiver: String?,
        caller: String?,
        callerLocation: String?,
        source: String?,
        status: String?,
        updateDatetime: String?,
        userName: String?,
        objectId: String?,
        objectName: String?,
        userId: String?,
        durationInSec: Int,
        remark: String?
    ) : this() {
        this.id = id
        this.createDateTime = createDatetime
        this.type = type
        this.callStartTime = callStartTime
        this.callEndTime = callEndTime
        this.callerProvider = callerProvider
        this.callerStatus = callerStatus
        this.receiverStatus = receiverStatus
        this.receiver = receiver
        this.caller = caller
        this.callerLocation = callerLocation
        this.source = source
        this.status = status
        this.updateDatetime = updateDatetime
        this.userName = userName
        this.objectId = objectId
        this.objectName = objectName
        this.userId = userId
        this.durationInSec = durationInSec
        this.remark = remark
    }

    constructor()

}

class PaymentLinkDTO{
    var amount: Double = 0.0
    var customerMobileNumber: String? = null
    var loanAccountNumber: String? = null
    var paymentNature: String? = null
    var paymentSubType: String? = null
    var linkType: String = EnLinkType.STANDARD.value
    var sendSMS: Boolean? = false
    var sendEmail: Boolean? = false

    fun mandatoryFieldsCheck() : LocalResponse {

        val localResponse = LocalResponse()
            .setError(Errors.INVALID_DATA.value)
            .setAction(Actions.FIX_RETRY.value)

        customerMobileNumber = if (customerMobileNumber!!.length > 10)
            customerMobileNumber!!.substring(customerMobileNumber!!.length - 10)
        else customerMobileNumber

        val ePaymentNature = EnPaymentType.get(paymentNature)

        when {
            amount < 50 -> localResponse.message = "Invalid amount."
            loanAccountNumber.isInvalidLAI() -> localResponse.message = "Invalid loan account number."
            customerMobileNumber.isInvalidMobileNumber() -> localResponse.message = "Invalid mobile number."
            ePaymentNature == null -> localResponse.message = "Invalid payment nature"
            ePaymentNature.nature == EnPaymentType.PARTIAL_PRE_PAYMENT.nature
                    && !ePaymentNature.subtypes.contains(paymentSubType) -> {
               localResponse.message = "Invalid payment sub type"
            }
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

    fun paymentLinkReqJson(oppId: String): JSONObject {
        return JSONObject().apply {
            put("customerMobileNumber", customerMobileNumber)
            put("sfLoanAccountNumber", loanAccountNumber)
            put("sfPaymentNature", paymentNature)
            put("sfPaymentSubType", paymentSubType)
            put("amount", amount)
            put("source", SOURCE_WHATSAPP_BOT)
            put("sfOpportunityId", oppId)
            put("linkType", linkType)
            put("sendSMS", sendSMS)
            put("sendEmail", sendEmail)
        }
    }

}

