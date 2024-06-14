package com.homefirstindia.hfo.repository.v1

import com.homefirstindia.hfo.model.v1.WhatsAppAvailability
import com.homefirstindia.hfo.model.v1.WhatsAppMessage
import com.homefirstindia.hfo.model.v1.WhatsAppMessageDisposition
import com.homefirstindia.hfo.model.v1.WhatsAppOptIn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository


@Component
class WhatsappRepositoryMaster(
    @Autowired val whatsAppMessageRepository: WhatsAppMessageRepository,
    @Autowired val whatsAppAvailabilityRepository: WhatsAppAvailabilityRepository,
    @Autowired val whatsAppOptInRepository: WhatsAppOptInRepository,
    @Autowired val whatsAppMessageDispositionRepository: WhatsAppMessageDispositionRepository
)

@Repository
interface WhatsAppMessageRepository: JpaRepository<WhatsAppMessage, String> {
    fun findByMessageId(id: String): WhatsAppMessage?
}


@Repository
interface WhatsAppAvailabilityRepository: JpaRepository<WhatsAppAvailability,String>{
    fun findByMobileNumber(mobileNumber: String): WhatsAppAvailability?

}

@Repository
interface WhatsAppOptInRepository: JpaRepository<WhatsAppOptIn,String>{
    fun findByMobileNumber(mobileNumber: String): WhatsAppOptIn?

}

@Repository
interface WhatsAppMessageDispositionRepository: JpaRepository<WhatsAppMessageDisposition,String>