package com.homefirstindia.hfo.helper.v1

import com.homefirstindia.hfo.manager.v1.CredsManager
import com.homefirstindia.hfo.manager.v1.EnCredType
import com.homefirstindia.hfo.manager.v1.EnPartnerName
import com.homefirstindia.hfo.model.v1.Creds
import com.homefirstindia.hfo.model.v1.LocationDirections
import com.homefirstindia.hfo.model.v1.LocationDistanceMatrix
import com.homefirstindia.hfo.networking.v1.CommonNetworkingClient
import com.homefirstindia.hfo.utils.*
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


enum class EnTravelMode(
    val key: String,
    val value: String
) {
    BIKE("BIKE", "driving"),
    CAR("CAR", "driving"),
    WALK("WALK", "walking"),
    BUS("BUS", "transit");

    companion object {
        operator fun get(key: String): EnTravelMode? {
            return EnTravelMode.values().firstOrNull { it.key == key }
        }
    }
}

const val REQUEST_DENIED = "REQUEST_DENIED"

@Component
class LocationHelper(
    @Autowired val cnClient: CommonNetworkingClient,
    @Autowired val credentialManager: CredsManager,
    @Autowired val cryptoUtils: CryptoUtils
) {

    private var _gMapCred: Creds? = null
    private fun log(value: String) = LoggerUtils.log("LocationHelper.$value")

    @Throws(Exception::class)
    private fun gMapCred(): Creds {
        return _gMapCred ?: credentialManager.fetchCredentials(
            EnPartnerName.GOOGLE_MAPS,
            if (cryptoUtils.appProperty.isProduction()) EnCredType.PRODUCTION else EnCredType.UAT
        ).also {
            _gMapCred = it
        }!!
    }

    @Throws(Exception::class)
    fun getDistanceMatrix(
        distanceMatrix: LocationDistanceMatrix
    ): LocalResponse {

        val distanceMatrixRequest = StringBuilder()
        distanceMatrixRequest.append(gMapCred().apiUrl)
        distanceMatrixRequest.append("/distancematrix/json?")
        distanceMatrixRequest.append("key=${decryptAnyKey(gMapCred().apiKey!!)}")
        distanceMatrixRequest.append(distanceMatrix.requestUrlGoogleMaps())

        println("getDistanceMatrix ----- distanceMatrixRequest : $distanceMatrixRequest")

        val gResponse = cnClient
            .NewRequest()
            .getCall(distanceMatrixRequest.toString())
            .send()

        val lResponse = LocalResponse()

        if (!gResponse.isSuccess) {

            log("getDistanceMatrix - Error : ${gResponse.errorMessage}")
            lResponse.message = gResponse.errorMessage
            return lResponse

        }

        val rJson = JSONObject(gResponse.stringEntity)

        when (rJson.getString(STATUS)) {

            REQUEST_DENIED -> {
                lResponse.message = rJson.optString("error_message", NA)
            }

            OK -> {

                rJson.optJSONArray("rows")
                    .optJSONObject(0)
                    .optJSONArray("elements")
                    .optJSONObject(0)?.let { element ->

                        val resultStatus = element.getString(STATUS)

                        if (resultStatus.equals(OK, true)) {

                            lResponse.isSuccess = true
                            lResponse.message = JSONObject()
                                .put(
                                    "distance",
                                    element.getJSONObject("distance").getInt("value")
                                )
                                .put(
                                    "duration",
                                    element.getJSONObject("duration").getInt("value")
                                )
                                .put(
                                    "originAddress",
                                    rJson.getJSONArray("origin_addresses").get(0).toString()
                                )
                                .put(
                                    "destinationAddress",
                                    rJson.getJSONArray("destination_addresses").get(0).toString()
                                )
                                .toString()

                        } else {

                            log("getDistanceMatrix - Error : $resultStatus")
                            lResponse.message = resultStatus

                        }

                    } ?: run {

                    val msg = "No data found."
                    log("getDistanceMatrix - Error : $msg")
                    lResponse.message = msg

                }

            }
        }

        return lResponse

    }


    @Throws(Exception::class)
    fun getDirection(
        locationDirections: LocationDirections
    ): LocalResponse {
        val directionRequest = StringBuilder()
        directionRequest.append(gMapCred().apiUrl)
        directionRequest.append("/directions/json?")
        directionRequest.append("key=${decryptAnyKey(gMapCred().apiKey!!)}")
        directionRequest.append(locationDirections.requestUrlGoogleMaps())

        val gResponse = cnClient
            .NewRequest()
            .getCall(directionRequest.toString())
            .send()

        val lResponse = LocalResponse()

        if (!gResponse.isSuccess) {
            log("getDirection - Error : ${gResponse.errorMessage}")
            return lResponse
        }

        val rJson = JSONObject(gResponse.stringEntity)

        when (rJson.optString(STATUS)) {

            REQUEST_DENIED -> {
                lResponse.message = rJson.optString("error_message", NA)
            }

            OK -> {
                val routeArray = rJson.getJSONArray("routes")
                if (routeArray.length() > 0) {
                    val routeObject = routeArray.getJSONObject(0)

                    val legArray = routeObject.getJSONArray("legs")
                    if (legArray.length() > 0) {
                        val legObject = legArray.getJSONObject(0)

                        if (!legObject.isEmpty) {
                            lResponse.isSuccess = true

                            val messageObj = JSONObject()
                            messageObj.put("distance", legObject.getJSONObject("distance").getInt("value"))
                            messageObj.put("duration", legObject.getJSONObject("duration").getInt("value"))
                            messageObj.put("end_address", legObject.getString("end_address"))
                            messageObj.put("start_address", legObject.getString("start_address"))
                            messageObj.put(
                                "origin_latitude",
                                legObject.getJSONObject("start_location").getDouble("lat")
                            )
                            messageObj.put(
                                "origin_longitude",
                                legObject.getJSONObject("start_location").getDouble("lng")
                            )
                            messageObj.put(
                                "destination_latitude",
                                legObject.getJSONObject("end_location").getDouble("lat")
                            )
                            messageObj.put(
                                "destination_longitude",
                                legObject.getJSONObject("end_location").getDouble("lng")
                            )

                            val polyline = routeObject.getJSONObject("overview_polyline").getString("points")
                            val summary = routeObject.getString("summary")

                            messageObj.put("polyline", polyline)
                            messageObj.put("summary", summary)

                            lResponse.message = messageObj.toString()
                        } else {

                            log("getDirections - Error : ${rJson.optString(STATUS)}")
//                            log("getDistanceMatrix - Error : $resultStatus")
//                            lResponse.message = resultStatus

                        }

                    }
                }
            }
        }

        return lResponse

    }


}