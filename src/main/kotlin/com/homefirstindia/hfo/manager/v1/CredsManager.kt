package com.homefirstindia.hfo.manager.v1

import com.homefirstindia.hfo.model.v1.Creds
import com.homefirstindia.hfo.repository.v1.CredsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.persistence.EntityManager

enum class EnCredType(val value: String) {
    PRODUCTION("PRODUCTION"),
    UAT("UAT"),
    PRE_PROD("PRE_PROD");
}

enum class EnPartnerName(val value: String) {
    HOMEFIRST("homefirst"),
    GUPSHUP("Gupshup"),
    GOOGLE_MAPS("Google_Maps"),
    SALESFORCE("Salesforce"),
    TEAL_V2("Teal-V2"),
    AMAZON("AWS-RABIT"),
    GOOGLE_DNR("Google_DNR"),
    KALEYRA_SMS("KaleyraSMS"),
    KALEYRA("Kaleyra"),
    DIGITAP("Digitap"),
}

@Component
class CredsManager(
    @Autowired private val credsRepository: CredsRepository,
    @Autowired private val entityManager: EntityManager
) {

    fun fetchCredentials(
        partnerName: EnPartnerName,
        credType: EnCredType
    ): Creds? {

        val cred =  credsRepository.findByPartnerNameAndCredType(
            partnerName.value,
            credType.value
        )?.apply {
            entityManager.detach(this)
        }

        return cred

    }

}