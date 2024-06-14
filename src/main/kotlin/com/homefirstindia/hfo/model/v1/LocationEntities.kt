package com.homefirstindia.hfo.model.v1

import com.homefirstindia.hfo.helper.v1.EnTravelMode
import com.homefirstindia.hfo.utils.*
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.GenericGenerator
import org.json.JSONObject
import javax.persistence.*

@Entity
@Table(name = "`location_DistanceMatrix`")
class LocationDistanceMatrix {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    var id: String? = null

    @Column(nullable = false, length = 64)
    var orgId: String? = null

    var originLatitude: Double? = null
    var originLongitude: Double? = null
    var destinationLatitude: Double? = null
    var destinationLongitude: Double? = null

    @Column(length = 32)
    var travelMode: String? = null

    var distance: Int? = null
    var duration: Int? = null
    var originAddress: String? = null
    var destinationAddress: String? = null

    @ColumnDefault("0")
    var success: Boolean = false

    var error: String? = null

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var createDatetime = DateTimeUtils.getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME")
    var updateDatetime: String? = null

    fun setModifiers() {
        updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
    }

    fun mandatoryFieldsCheck(): LocalResponse {

        val localResponse = LocalResponse()
            .setError(Errors.INVALID_DATA.value)
            .setAction(Actions.FIX_RETRY.value)

        when {
            originLatitude.isInvalid() -> localResponse.message = "Invalid originLatitude."
            originLongitude.isInvalid() -> localResponse.message = "Invalid originLongitude."
            destinationLatitude.isInvalid() -> localResponse.message = "Invalid destinationLatitude."
            destinationLongitude.isInvalid() -> localResponse.message = "Invalid destinationLongitude."
        }

        if (travelMode.isNotNullOrNA()) {

            val enTravelMode = EnTravelMode[travelMode!!]

            if (enTravelMode == null)
                localResponse.message = "Invalid mode."
            else {
                localResponse.apply {
                    message = NA
                    error = NA
                    action = NA
                    isSuccess = true
                }
            }

        } else {

            localResponse.apply {
                message = NA
                error = NA
                action = NA
                isSuccess = true
            }

        }

        return localResponse

    }

    fun requestUrlGoogleMaps(): String {

        /*
        * https://maps.googleapis.com/maps/api/distancematrix/json?key=AIzaSyCns3vBk6xtEOK3fTV_edJaBn6VbHak8Z8
        * &travelMode=TWO_WHEELER
        * &origins=19.1130149,72.8656943
        * &destinations=19.116015,72.8635077
        * */

        val sb = StringBuilder()
        sb.append("&origins=$originLatitude,$originLongitude")
        sb.append("&destinations=$destinationLatitude,$destinationLongitude")

        if (travelMode.isNotNullOrNA()) {
            sb.append("&mode=${EnTravelMode[travelMode!!]?.value}")
        }

        return sb.toString()

    }

    fun updateFromJson(json: JSONObject) {

        distance = json.optInt("distance")
        duration = json.optInt("duration")
        originAddress = json.optString("originAddress")
        destinationAddress = json.optString("destinationAddress")

    }

}


@Entity
@Table(name = "`location_Directions`")
class LocationDirections {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    var id: String? = null

    @Column(nullable = false, length = 64)
    var orgId: String? = null

    var originLatitude: Double? = null
    var originLongitude: Double? = null
    var destinationLatitude: Double? = null
    var destinationLongitude: Double? = null

    @Column(length = 32)
    var travelMode: String? = null

    var distance: Int? = null
    var duration: Int? = null
    var originAddress: String? = null
    var destinationAddress: String? = null

    @Column(length = 1028)
    var overviewPolyline : String? = null
    var summary : String? = null

    @ColumnDefault("0")
    var success: Boolean = false

    var error: String? = null

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var createDatetime = DateTimeUtils.getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME")
    var updateDatetime: String? = null

    fun setModifiers() {
        updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
    }

    fun mandatoryFieldsCheck(): LocalResponse {

        val localResponse = LocalResponse()
            .setError(Errors.INVALID_DATA.value)
            .setAction(Actions.FIX_RETRY.value)

        when {
            originLatitude.isInvalid() -> localResponse.message = "Invalid originLatitude."
            originLongitude.isInvalid() -> localResponse.message = "Invalid originLongitude."
            destinationLatitude.isInvalid() -> localResponse.message = "Invalid destinationLatitude."
            destinationLongitude.isInvalid() -> localResponse.message = "Invalid destinationLongitude."
        }

        if (travelMode.isNotNullOrNA()) {

            val enTravelMode = EnTravelMode[travelMode!!]

            if (enTravelMode == null)
                localResponse.message = "Invalid mode."
            else {
                localResponse.apply {
                    message = NA
                    error = NA
                    action = NA
                    isSuccess = true
                }
            }

        } else {

            localResponse.apply {
                message = NA
                error = NA
                action = NA
                isSuccess = true
            }

        }

        return localResponse

    }

    fun requestUrlGoogleMaps(): String {

        val sb = StringBuilder()
        sb.append("&origin=$originLatitude,$originLongitude")
        sb.append("&destination=$destinationLatitude,$destinationLongitude")

        if (travelMode.isNotNullOrNA()) {
            sb.append("&mode=${EnTravelMode[travelMode!!]?.value}")
        }

        return sb.toString()

    }

    fun updateFromJson(json: JSONObject) {

        distance = json.optInt("distance")
        duration = json.optInt("duration")
        originAddress = json.optString("start_address")
        destinationAddress = json.optString("end_address")
        overviewPolyline = json.optString("polyline")
        summary = json.optString("summary")

    }

}



