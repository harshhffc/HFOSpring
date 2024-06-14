package com.homefirstindia.hfo.repository.v1

import com.homefirstindia.hfo.model.v1.MobileNumberValidation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MobileNumberValidationRepository : JpaRepository<MobileNumberValidation, String> {

    fun findByTransactionIdAndToken(transactionId: String?, token: String?): MobileNumberValidation?

    fun findFirstByMobileNumberOrderByCreateDatetimeDesc(mobileNumber: String?): MobileNumberValidation?

    fun findByReferenceNumber(referenceNumber: String?): MobileNumberValidation?
}