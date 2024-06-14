package com.homefirstindia.hfo.model.v1

import com.homefirstindia.hfo.utils.DateTimeUtils
import com.homefirstindia.hfo.utils.DateTimeUtils.getCurrentDateTimeInIST
import com.homefirstindia.hfo.utils.NA
import com.homefirstindia.hfo.utils.isNotNullOrNA
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.GenericGenerator
import org.json.JSONArray
import javax.persistence.*


@Entity
@Table(name = "`Partner`")
class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    var id = -1

    var orgName: String = NA
    var orgId: String = NA
    var destination: String = NA
    var leadSource: String = NA
    var leadOwner: String = NA
    var branch: String = NA
    var clientId: String = NA
    var clientSecret: String = NA

    @ColumnDefault("0")
    var isInternal = false

    @Column(columnDefinition = "JSON", name = "servicesAllowed")
    var services: String = NA

    @ColumnDefault("0")
    var isEnabled = false

    @ColumnDefault("0")
    var ipRestricted = false

    @ColumnDefault("0")
    var requiredConsent = false

    @ColumnDefault("1")
    var sessionEnabled = true
    var sessionPasscode: String = NA

    @Column(columnDefinition = "DATETIME")
    var sessionValidDatetime: String = NA

    @Column(columnDefinition = "DATETIME")
    var sessionUpdateDatetime: String = NA

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var createDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME")
    var updateDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()

    private fun servicesAllowed() : ArrayList<String> {

        var servicesAllowed = ArrayList<String>()

        if (services.isNotNullOrNA() && services.startsWith("[")) {
            servicesAllowed = ArrayList()
            val serviceArray = JSONArray(services)
            for (i in 0 until serviceArray.length())
                servicesAllowed.add(serviceArray.getString(i))
        }

        return servicesAllowed

    }

    fun isServiceAllowed(service: String) = servicesAllowed().contains(service)

    fun isSessionValid(): Boolean {

        val currentDateTime = DateTimeUtils.getDateFromDateTimeString(
            getCurrentDateTimeInIST()
        )

        val sessionValidDate = DateTimeUtils.getDateFromDateTimeString(
            sessionValidDatetime
        )

        return currentDateTime.before(sessionValidDate)

    }

    fun shouldIncreaseSessionValidity() : Boolean {

        val validityLeft = DateTimeUtils.getDateDifferenceInMinutes(
            getCurrentDateTimeInIST(),
            sessionValidDatetime
        )

        return validityLeft in 0..14

    }

}

@Entity
@Table(name = "`PartnerLog`")
class PartnerLog {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    var id: String? = null

    @Column(nullable = false)
    var orgId: String? = null

    var endpoint: String? = null
    var ipAddress: String? = null
    var requestDesc: String? = null
    var requestStatus: String? = null
    var serviceName = NA
    var responseStatus: Int? = -1

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var datetime: String = getCurrentDateTimeInIST()

}

@Entity
@Table(name = "`whitelistedIP`")
class WhitelistedIP {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    var id = -1

    @Column(nullable = false)
    var orgId = NA

    var orgDesc = NA

    @Column(nullable = false)
    var ipAddress = NA

    @ColumnDefault("0")
    var isActive = false

}

@Entity
@Table(name = "`creds`")
class Creds {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    var id: String? = null

    @Column(nullable = false)
    var partnerName: String? = null
    var credType: String? = null
    var username: String? = null
    var password: String? = null
    var memberId: String? = null
    var memberPasscode: String? = null
    var salt: String? = null
    var apiKey: String? = null
    var apiUrl: String? = null

    @ColumnDefault("1")
    var isValid = true

    @ColumnDefault("0")
    var isEncrypted = false

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var createDatetime = getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME")
    var updateDatetime = getCurrentDateTimeInIST()

}

@Entity
@Table(name = "`BotUserLog`")
class BotUserLog {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    var id: String? = null

    var requestDesc: String? = null
    var requestStatus: String? = null
    var serviceName = NA
    var responseStatus: Boolean = false
    @Column(columnDefinition = "JSON")
    var rawRequest: String? = null

    @Column(columnDefinition = "JSON")
    var rawResponse: String? = null

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var datetime: String = getCurrentDateTimeInIST()

}