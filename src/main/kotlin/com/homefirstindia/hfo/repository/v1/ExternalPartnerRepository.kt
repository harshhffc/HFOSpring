package com.homefirstindia.hfo.repository.v1

import com.homefirstindia.hfo.model.v1.Creds
import com.homefirstindia.hfo.model.v1.Partner
import com.homefirstindia.hfo.model.v1.PartnerLog
import com.homefirstindia.hfo.model.v1.WhitelistedIP
import com.homefirstindia.hfo.model.v1.common.ExternalServiceLog
import com.homefirstindia.hfo.model.v1.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Component
class PartnerMasterRepository(
    @Autowired val partnerRepository: PartnerRepository,
    @Autowired val partnerLogRepository: PartnerLogRepository,
    @Autowired val whitelistedIPRepository: WhitelistedIPRepository
)

@Repository
interface PartnerRepository : JpaRepository<Partner, Int> {

    fun findByOrgId(orgId: String): Partner?

}

@Repository
interface PartnerLogRepository : JpaRepository<PartnerLog, String>

@Repository
interface WhitelistedIPRepository : JpaRepository<WhitelistedIP, Int> {

    @Query("from WhitelistedIP where orgId = :orgId and isActive = true")
    fun findAllByOrgId(orgId: String?): ArrayList<WhitelistedIP>?

}

@Repository
interface CredsRepository : JpaRepository<Creds, String> {

    @Query("from Creds where partnerName = :partnerName and credType = :credType and isValid = true")
    fun findByPartnerNameAndCredType(partnerName: String, credType: String): Creds?

}

@Repository
interface ExternalServiceLogRepository : JpaRepository<ExternalServiceLog, String>

@Repository
interface BotUserLogRepository: JpaRepository<BotUserLog, String>

