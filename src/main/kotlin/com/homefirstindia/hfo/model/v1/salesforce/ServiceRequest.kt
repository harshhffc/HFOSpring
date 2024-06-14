package com.homefirstindia.hfo.model.v1.salesforce

import com.homefirstindia.hfo.utils.DateTimeUtils
import com.homefirstindia.hfo.utils.NA
import org.hibernate.annotations.GenericGenerator
import org.json.JSONObject
import javax.persistence.*
import kotlin.jvm.Transient


@Entity
@Table(name = "`ServiceRequest`")
class ServiceRequest {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    var id: String? = null
    var caseId: String? = null
    var masterCaseReason: String? = null
    var caseReason: String? = null
    var status: String? = null
    var subject: String? = null
    var description: String? = null

    @Transient
    var attachmentFile: String? = null
    var attachmentName: String? = null
    var loanAccountNumber: String? = null

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var createDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME")
    var updateDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()
    fun initForSR(json: JSONObject?): ServiceRequest {
        if (null != json) {
            subject = json.optString("Subject", NA)
            masterCaseReason = json.optString("Master_Case_Reason__c", NA)
            caseReason = json.optString("Case_Reason_sub__c", NA)
            status = json.optString("Status", NA)
            description = json.optString("Description", NA)
            loanAccountNumber = json.optString("loanAccountNumber", NA)
            attachmentFile = json.optString("attachment", NA)
            attachmentName = json.optString("attachmentName", NA)
        }
        return this
    }
}

