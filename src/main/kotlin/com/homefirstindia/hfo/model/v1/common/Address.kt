package com.homefirstindia.hfo.model.v1.common

import com.homefirstindia.hfo.utils.DateTimeUtils.getCurrentDateTimeInIST
import com.homefirstindia.hfo.utils.NA
import com.homefirstindia.hfo.utils.isNotNullOrNA
import org.hibernate.annotations.GenericGenerator
import org.json.JSONException
import org.json.JSONObject
import javax.persistence.*


@Entity
@Table(name = "`Address`")
class Address {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    var id: String? = null
    var street: String? = null
    var city: String? = null
    var state: String? = null
    var country = "India"
    var postalCode: String? = null
    var mobile: String? = null
    var phone: String? = null

    @Column(columnDefinition = "JSON")
    var raw: String? = null

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var createDatetime: String = getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME")
    var updateDatetime: String = getCurrentDateTimeInIST()

    constructor() {}
    constructor(jsonObject: JSONObject?) {
        if (null == jsonObject) return
        city = jsonObject.optString("city")
        street = jsonObject.optString("street")
        state = jsonObject.optString("state")
        country = jsonObject.optString("country")
        postalCode = jsonObject.optString("postalCode")
    }

    @Throws(JSONException::class)
    fun toJson(): JSONObject {
        val addressObject = JSONObject()
        addressObject.put("street", street)
        addressObject.put("city", city)
        addressObject.put("state", state)
        addressObject.put("country", country)
        addressObject.put("postalCode", postalCode)
        return addressObject
    }

    val isValid: Boolean get() = hasData(city) && hasData(street) && hasData(state) && hasData(postalCode)
    val isStateValid: Boolean get() = hasData(state) && hasData(postalCode)

    private fun hasData(value: String?): Boolean {
        return value.isNotNullOrNA()
    }

    val address: String
        get() {
            val sb = StringBuilder()
            if (street.isNotNullOrNA()) sb.append("$street, ")
            if (city.isNotNullOrNA()) sb.append("$city, ")
            if (state.isNotNullOrNA()) sb.append("$state, ")
            if (country.isNotNullOrNA()) sb.append("$country, ")
            if (postalCode.isNotNullOrNA()) sb.append("$postalCode, ")
            if (sb.toString().isEmpty()) sb.append(NA)
            return sb.toString()
        }

    fun updateInfo(address: Address) {
        street = address.street
        city = address.city
        state = address.state
        if (isNotNullOrNA(address.postalCode)) postalCode = address.postalCode
        country = address.country
        updateDatetime = getCurrentDateTimeInIST()
    }


}