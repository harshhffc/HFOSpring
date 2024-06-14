package com.homefirstindia.hfo.dto.v1

import com.homefirstindia.hfo.utils.*
import com.opencsv.bean.CsvBindByName

class MessageDTO {

    @CsvBindByName(column = "Mobile Number")
    var mobileNumber: String? = null

    @CsvBindByName(column = "Customer Name")
    var customerName: String? = null

    @CsvBindByName(column = "Message")
    var message: String? = null

    @CsvBindByName(column = "Source")
    var source: String? = null

    fun mandatoryFieldCheck(): LocalResponse {

        val lResponse = LocalResponse()
        lResponse.action = Actions.FIX_RETRY.value
        lResponse.error = Errors.INVALID_DATA.value

        when {
            mobileNumber.isInvalidMobileNumber() -> lResponse.message = "Invalid mobileNumber."
            message.isInvalid() -> lResponse.message = "Invalid message."

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