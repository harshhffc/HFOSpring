package com.homefirstindia.hfo.helper.v1

import com.homefirstindia.hfo.dto.v1.externalpartner.EPAuthRequest
import com.homefirstindia.hfo.model.v1.BotUserLog
import com.homefirstindia.hfo.model.v1.PartnerLog
import com.homefirstindia.hfo.repository.v1.BotUserLogRepository
import com.homefirstindia.hfo.repository.v1.PartnerLogRepository
import com.homefirstindia.hfo.utils.THREAD_POOL_TASK_EXECUTOR
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Component

enum class UserActionStatus {
    SUCCESS, FAILURE
}

@EnableAsync
@Component
class PartnerLogHelper(
    @Autowired private val partnerLogRepository: PartnerLogRepository
) {

    inner class Builder() {

        constructor(epAuthRequest: EPAuthRequest): this() {

            epAuthRequest.run {
                epLog.orgId = orgId
                epLog.endpoint = requestUri
                epLog.ipAddress = ipAddress
            }

        }

        private var epLog = PartnerLog()

        fun setOrgId(value: String?) : Builder {
            epLog.orgId = value
            return this
        }

        fun setEndpoint(value: String?) : Builder {
            epLog.endpoint = value
            return this
        }

        fun setRequestDesc(value: String?) : Builder {
            epLog.requestDesc = value
            return this
        }

        fun setRequestStatus(value: UserActionStatus) : Builder {
            epLog.requestStatus = value.name
            return this
        }

        fun setIPAddress(value: String?) : Builder {
            epLog.ipAddress = value
            return this
        }

        fun setResponseStatus(value: Int?) : Builder {
            epLog.responseStatus = value
            return this
        }

        fun setServiceName(value: String?) : Builder {
            value?.let {
                epLog.serviceName = value
            }
            return this
        }

        @Async(THREAD_POOL_TASK_EXECUTOR)
        fun log() {
            partnerLogRepository.save(epLog)
        }

    }

}

@EnableAsync
@Component
class BotLogHelper(
    @Autowired private val botLogRepository: BotUserLogRepository
) {

    inner class Builder() {

        private var epLog = BotUserLog()

        fun setRequestDesc(value: String?) : Builder {
            epLog.requestDesc = value
            return this
        }

        fun setRequestStatus(value: UserActionStatus) : Builder {
            epLog.requestStatus = value.name
            return this
        }
        fun setResponseStatus(value: Boolean = false) : Builder {

            epLog.responseStatus = value
            return this
        }

        fun setServiceName(value: String?) : Builder {
            value?.let {
                epLog.serviceName = value
            }
            return this
        }
        fun setRawRequest(value: String) : Builder {
            epLog.rawRequest = value
            return this
        }

        fun setRawResponse(value: String) : Builder {
            epLog.rawResponse = value
            return this
        }

        //@Async(THREAD_POOL_TASK_EXECUTOR)
        fun log(): BotUserLog {
            return botLogRepository.save(epLog)
        }

    }

}