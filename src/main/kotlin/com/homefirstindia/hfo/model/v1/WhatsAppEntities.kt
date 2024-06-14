package com.homefirstindia.hfo.model.v1

import com.homefirstindia.hfo.dto.v1.MessageDTO
import com.homefirstindia.hfo.utils.DateTimeUtils.getCurrentDateTimeInIST
import com.homefirstindia.hfo.utils.EnWhatsAppNumberStatus
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.GenericGenerator
import org.json.JSONObject
import javax.persistence.*

@Entity
@Table(name = "wa_OptIn")
class WhatsAppOptIn{

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    var id: String? = null

    var optInId: String? = null
    var mobileNumber : String? = null
    var status: String? = null

    @Column(columnDefinition = "JSON")
    var response: String? = null

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var createDatetime = getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var updateDatetime = getCurrentDateTimeInIST()

}

@Entity
@Table(name = "`wa_CustomerMessage`")
class WhatsAppMessage {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    var id: String? = null

    @Column(nullable = false, length = 10)
    var mobileNumber: String? = null

    var messageBody: String? = null

    var customerName: String? = null

    @Column(nullable = false)
    var source: String? =null

    var orgId: String? = null

    var status: String? = null

    var messageId: String? = null
    var response: String? = null
    var error: String? = null

    var deliveryStatus: String? = null

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var createDatetime = getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME")
    var updateDatetime = getCurrentDateTimeInIST()

    fun setUpdateParams() {
        updateDatetime = getCurrentDateTimeInIST()
    }

    fun fromDTO(singleTextTemplate: MessageDTO): WhatsAppMessage {

        singleTextTemplate.let {

            messageBody = it.message
            mobileNumber = it.mobileNumber
            customerName = it.customerName
            source = it.source

        }
        return this
    }

}


@Entity
@Table(name = "`wa_Availability`")
class WhatsAppAvailability{

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    var id: String? = null

    @Column(unique = true)
    var mobileNumber : String? = null

    @ColumnDefault("0")
    var isAvailable: Boolean = false

    var availabilityReason: String? = null

    @Column(columnDefinition = "DATETIME", nullable = true)
    var lastTriedDatetime: String? = null

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var createDatetime = getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var updateDatetime = getCurrentDateTimeInIST()

    fun parseDisposition(waDisposition: WhatsAppMessageDisposition): WhatsAppAvailability {

        mobileNumber = waDisposition.mobileNumber
        availabilityReason = waDisposition.deliveryReason
        isAvailable = waDisposition.deliveryReason != EnWhatsAppNumberStatus.UNKNOWN_SUBSCRIBER.key
        lastTriedDatetime = getCurrentDateTimeInIST()

        return this

    }

}

@Entity
@Table(name = "`wa_MessageDisposition`")
class WhatsAppMessageDisposition{

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    var id: String? = null

    var mobileNumber: String? = null

    var messageId: String? = null

    var channel: String? = null
    var sourceAddress: String? = null

    var deliveryStatus: String? = null
    var deliveryReason: String? = null

    var errorCode: String? = null

    fun parseDisposition(responseJson: JSONObject): WhatsAppMessageDisposition {

        sourceAddress = responseJson.optString("srcAddr")
        channel = responseJson.optString("channel")
        messageId = responseJson.optString("externalId")
        deliveryStatus = responseJson.optString("eventType")
        deliveryReason = responseJson.optString("cause")
        errorCode = responseJson.optString("errorCode")
        mobileNumber = responseJson.optString("destAddr")

        return this

    }

}

