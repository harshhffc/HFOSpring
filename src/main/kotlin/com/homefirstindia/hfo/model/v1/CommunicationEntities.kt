package com.homefirstindia.hfo.model.v1

import com.homefirstindia.hfo.utils.*
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.GenericGenerator
import javax.persistence.*
import kotlin.jvm.Transient

@Entity
@Table(name = "`CallingInfo`")
class CallingInfo {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    var id: String? = null

    @Column(unique = true)
    var callerId: String? = null

    var apiKey: String? = null
    var description: String? = null

    var isActive = true

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var createDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME")
    var updateDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()
}

@Entity
@Table(name = "`CallLog`")
class CallLog {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    var id: String? = null

    var callId: String? = null

    @Column(nullable = false)
    var caller: String? = null

    @Column(nullable = false)
    var receiver: String? = null

    var callerName: String? = null

    @Column(columnDefinition = "DATETIME")
    var callStartTime: String? = null

    @Column(columnDefinition = "DATETIME")
    var callEndTime: String? = null

    var durationInSec: Int = 0
    var remark: String? = null

    var callerStatus: String? = null
    var receiverStatus: String? = null

    var callerLocation: String? = null
    var callerProvider: String? = null

    var recordingUrl: String? = null
    var response: String? = null

    var orgId: String? = null
    var objectId: String? = null
    var objectName: String? = null
    var callerId: String? = null

    @Column(nullable = false)
    var source: String? = null

    var userId: String? = null
    var userEmail: String? = null
    var userName: String? = null

    var callbackUrl: String? = null

    @ColumnDefault("0")
    var isNotified: Boolean = false

    @ColumnDefault("OUTBOUND")
    var type: String = EnCallType.OUTBOUND.value

    @ColumnDefault("CREATED")
    var status: String = CallStatus.CREATED.value

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "attachmentId", referencedColumnName = "id")
    var attachment: Attachment? = null

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var createDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME")
    var updateDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()

    fun mandatoryFieldsCheck(): LocalResponse {

        val lResponse = LocalResponse()
        lResponse.action = Actions.FIX_RETRY.value
        lResponse.error = Errors.INVALID_DATA.value

        val callSource = CallSource.get(source)

        when {
            caller.isInvalidMobileNumber() -> lResponse.message = "Invalid caller."
            receiver.isInvalidMobileNumber() -> lResponse.message = "Invalid receiver."
            callSource == null -> lResponse.message = "Invalid source"

            else -> {

                lResponse.apply {
                    message = "Request is valid"
                    error = NA
                    action = NA
                    isSuccess = true
                }
            }

        }
        return lResponse
    }

    fun mandatoryFieldsCheckForRemark(): LocalResponse {

        val lResponse = LocalResponse()
        lResponse.action = Actions.FIX_RETRY.value
        lResponse.error = Errors.INVALID_DATA.value

        when {
            id.isInvalid() -> lResponse.message = "Invalid call log id."
            remark.isInvalid() -> lResponse.message = "Invalid remark."

            else -> {

                lResponse.apply {
                    message = "Request is valid"
                    error = NA
                    action = NA
                    isSuccess = true
                }
            }

        }
        return lResponse
    }

}

@Entity
@Table(name = "`SMSLog`")
class SMSLog {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    var id: String? = null

    var smsId: String? = null

    @Column(nullable = false)
    var message: String? = null

    @Column(nullable = false)
    var mobileNumber: String? = null

    @Column(nullable = false)
    var approvedTemplateId: String? = null

    var smsCampaignId: String? = null

    @ColumnDefault("CREATED")
    var status: String? = null
    var objectId: String? = null
    var objectName: String? = null
    var orgId: String? = null

    @Column(nullable = false)
    var source: String? = null
    var userId: String? = null
    var userEmail: String? = null
    var userName: String? = null
    var response: String? = null

    @Transient
    var jsonMessage: ArrayList<SMSBody>? = null

    @Column(columnDefinition = "DATETIME")
    var sentDatetime: String? = null

    @Column(columnDefinition = "DATETIME")
    var deliveredDatetime: String? = null

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var createDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME")
    var updateDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()

    fun mandatoryFieldsCheck(): LocalResponse {

        val localResponse = LocalResponse()
            .setError(Errors.INVALID_DATA.value)
            .setAction(Actions.FIX_RETRY.value)

        when {
            approvedTemplateId.isInvalid() -> localResponse.message = "Invalid approved template Id."
            source.isInvalid() -> localResponse.message = "Invalid source."

            else -> {
                localResponse.apply {
                    message = "Request is valid"
                    error = NA
                    action = NA
                    isSuccess = true
                }
            }
        }

        return localResponse
    }

}

class SMSBody {
    var to: String? = null
    var message: String? = null
    var custom1: String? = null //This variable will contain object id
    var custom2: String? = null //This variable will contain sms message text

    fun mandatoryFieldsCheck(): LocalResponse {

        val localResponse = LocalResponse()
            .setError(Errors.INVALID_DATA.value)
            .setAction(Actions.FIX_RETRY.value)

        when {
            to.isInvalid() && to?.length != 10 -> localResponse.message = "Invalid mobile number."
            message.isInvalid() -> localResponse.message = "Invalid Message."
            custom1.isInvalid() -> localResponse.message = "Invalid object id ."

            else -> {
                localResponse.apply {
                    message = "Request is valid"
                    error = NA
                    action = NA
                    isSuccess = true
                }
            }
        }

        return localResponse
    }

}

@Entity
@Table(name = "`SMSTemplate`")
class SMSTemplate {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    var id: String? = null
    var name: String? = null
    var description: String? = null

    @Column(nullable = false)
    var message: String? = null

    @Column(nullable = false)
    var templateId: String? = null

    @Column(nullable = false)
    var entityId: String? = null

    @Column(nullable = false)
    var senderId: String? = null
    var communicationType: String? = null
    var category: String? = null
    var contentType: String? = null
    var registeredPlatform: String? = null

    @ColumnDefault("0")
    var isUnicode = true

    @ColumnDefault("1")
    var isActive = true

    @ColumnDefault("0")
    var isApproved = false
    var remark: String? = null

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var createDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME")
    var updateDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()

    fun areSMSTemplateParamsValid(): LocalResponse {

        val lResponse = LocalResponse()
        lResponse.action = Actions.FIX_RETRY.value
        lResponse.error = Errors.INVALID_DATA.value

        when {
            templateId.isInvalid() -> lResponse.message = "Invalid templateId."
            entityId.isInvalid() -> lResponse.message = "Invalid entityId."
            message.isInvalid() -> lResponse.message = "Invalid message."
            senderId.isInvalid() -> lResponse.message = "Invalid senderId."

            else -> {
                lResponse.apply {
                    message = "Request is valid"
                    error = NA
                    action = NA
                    isSuccess = true
                }
            }
        }

        return lResponse

    }

}

class RowCondition{

    var lso = NA
    var op:String? = null
    var rso: ArrayList<String>? = null


    fun mandatoryFieldCheck():LocalResponse {
        val localResponse = LocalResponse()
            .setError(Errors.INVALID_DATA.value)
            .setAction(Actions.FIX_RETRY.value)

        when{
            lso.isNotNullOrNA()  || lso.isInvalid() -> localResponse.message = "Invalid Left Side Operator $lso"
            op.isNotNullOrNA() || op.isInvalid() -> localResponse.message = "Invalid Operator $op"
            rso.isNullOrEmpty() -> localResponse.message = "Invalid Right Side Operator $rso"

        }

        return localResponse
    }
}