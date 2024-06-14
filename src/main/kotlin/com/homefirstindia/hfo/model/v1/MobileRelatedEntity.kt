package com.homefirstindia.hfo.model.v1

import com.homefirstindia.hfo.utils.DateTimeUtils
import com.homefirstindia.hfo.utils.EnumStatus
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.GenericGenerator
import javax.persistence.*

@Entity
@Table(name = "`MobileNumberValidation`")
class MobileNumberValidation {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    var id: String? = null

    var mobileNumber: String? = null

    var referenceNumber: String? = null

    var source: String? = null

    var status: String? = EnumStatus.INITIATED.value

    var transactionId: String? = null
    var token: String? = null

    @ColumnDefault("0")
    var isValidated = false

    @Column(columnDefinition = "JSON")
    var generateOtpResponse: String? = null

    @Column(columnDefinition = "JSON")
    var verifyOtpResponse: String? = null

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var createDatetime = DateTimeUtils.getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME")
    var updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()

}