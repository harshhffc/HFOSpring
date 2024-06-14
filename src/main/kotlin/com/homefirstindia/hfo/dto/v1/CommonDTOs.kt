package com.homefirstindia.hfo.dto.v1

import com.homefirstindia.hfo.utils.*

class MFile(
    val name: String,
    val path: String
)

class OTPVerificationDTO {

    val mobileNumber: String? = null
    val otp: String? = null

    fun mandatoryFieldsCheck(): LocalResponse {

        val localResponse = LocalResponse()
            .setError(Errors.INVALID_DATA.value)
            .setAction(Actions.FIX_RETRY.value)

        when {
            mobileNumber.isInvalid() -> localResponse.message = "Invalid mobile number"
            otp.isInvalid() -> localResponse.message = "Invalid OTP"

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
}
