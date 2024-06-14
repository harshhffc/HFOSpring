package com.homefirstindia.hfo.manager.v1

import com.homefirstindia.hfo.model.v1.common.ExternalServiceLog
import com.homefirstindia.hfo.repository.v1.ExternalServiceLogRepository
import com.homefirstindia.hfo.utils.DateTimeUtils
import com.homefirstindia.hfo.utils.EnExternalServiceName
import com.homefirstindia.hfo.utils.EnUserRequestStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ExternalServiceManager(
    @Autowired val externalServiceLogRepository: ExternalServiceLogRepository,
) {

    fun logDigitapService(
        url: String,
        objectId: String? = null,
        objectName: String? = null
    ): ExternalServiceLog? {

        val serviceLog = ExternalServiceLog().apply {

            serviceName = EnExternalServiceName.DIGITAP.value
            serviceUrl = url
            status = EnUserRequestStatus.CREATED.value

            objectId?.let {
                this.objectId = objectId
            }

            objectName?.let {
                this.objectName = objectName
            }

            updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
        }

        return externalServiceLogRepository.save(serviceLog)

    }

}