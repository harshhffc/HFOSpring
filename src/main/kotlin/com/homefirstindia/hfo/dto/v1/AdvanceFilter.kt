package com.homefirstindia.hfo.dto.v1

import com.homefirstindia.hfo.model.v1.RowCondition
import com.homefirstindia.hfo.utils.*


class AdvanceFilter {

    var groupConditionOp: String? = null
    var conditions:List<RowCondition> = listOf()

    fun mandatoryFieldsCheck(): LocalResponse {
        val localResponse = LocalResponse()
                .setError(Errors.INVALID_DATA.value)
                .setAction(Actions.FIX_RETRY.value)

        when {
            groupConditionOp.isInvalid() -> localResponse.message = "Invalid Group Condition"
            conditions.isEmpty() -> localResponse.message = "Invalid Conditions"
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