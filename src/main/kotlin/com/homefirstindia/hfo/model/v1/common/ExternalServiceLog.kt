package com.homefirstindia.hfo.model.v1.common

import com.homefirstindia.hfo.utils.DateTimeUtils
import org.hibernate.annotations.GenericGenerator
import javax.persistence.*

@Entity
@Table(name = "`ExternalServiceLog`")
class ExternalServiceLog {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    var id: String? = null

    var orgId: String? = null
    var serviceName: String? = null
    var serviceType: String? = null
    var serviceUrl: String? = null
    var status: String? = null
    var responseCode: String? = null
    var objectId: String? = null
    var objectName: String? = null

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var createDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME")
    var updateDatetime: String? = null

}