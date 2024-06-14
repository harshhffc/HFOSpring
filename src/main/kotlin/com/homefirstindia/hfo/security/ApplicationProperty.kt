package com.homefirstindia.hfo.security

import com.homefirstindia.hfo.manager.v1.CredsManager
import com.homefirstindia.hfo.manager.v1.EnCredType
import com.homefirstindia.hfo.manager.v1.EnPartnerName
import com.homefirstindia.hfo.model.v1.Creds
import com.homefirstindia.hfo.model.v1.Partner
import com.homefirstindia.hfo.repository.v1.PartnerRepository
import com.homefirstindia.hfo.utils.THREAD_POOL_TASK_EXECUTOR
import com.homefirstindia.hfo.utils.decryptAnyKey
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.Executor


enum class EnvProfile(
    val value : String
    ) {

    DEV("dev"),
    STAGING("staging"),
    UAT("uat"),
    PROD("prod")

}

@Component
class ContextProvider(
    @Autowired val partnerRepository: PartnerRepository
) {

    fun getPartner(orgId: String): Partner = partnerRepository.findByOrgId(orgId)!!

}

@Configuration
@EnableAsync
class AppProperty(
    @Autowired val credentialManager: CredsManager
) {

    companion object {
        private var _gDnrCred: Creds? = null
    }

    private fun gDnrCred(): Creds? {
        if (null == _gDnrCred) {
            _gDnrCred = credentialManager.fetchCredentials(
                EnPartnerName.GOOGLE_DNR,
                EnCredType.PRODUCTION
            )

            _gDnrCred?.apply {
                username = decryptAnyKey(username!!)
                password = decryptAnyKey(password!!)
            }

        }
        return _gDnrCred
    }

    @Bean
    fun getJavaMailSender(): JavaMailSender {

        val gCred = _gDnrCred ?: gDnrCred()

        val mailSender = JavaMailSenderImpl()
        mailSender.host = "smtp.gmail.com"
        mailSender.port = 587
        mailSender.username = gCred?.username
        mailSender.password = gCred?.password

        val props: Properties = mailSender.javaMailProperties
        props["mail.transport.protocol"] = "smtp"
        props["mail.smtp.auth"] = true
        props["mail.smtp.starttls.enable"] = true
        props["mail.debug"] = true

        return mailSender

    }

    @Bean(name = [THREAD_POOL_TASK_EXECUTOR])
    fun threadPoolTaskExecutor(): Executor {
        return ThreadPoolTaskExecutor().apply {
            corePoolSize = 5
            maxPoolSize = 20
            queueCapacity = 100
        }
    }

    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Value("\${spring.profiles.active}")
    lateinit var activeProfile : String

    @Value("\${application.flags.isStrictProduction}")
    var isStrictProduction : Boolean = false

    fun isProduction() = activeProfile == EnvProfile.PROD.value

    fun isUAT() = activeProfile == EnvProfile.UAT.value

    fun isStaging() = activeProfile == EnvProfile.DEV.value

    @Value("\${application.key.salt}")
    lateinit var salt: String

    @Value("\${application.flags.isSalesforceLive}")
    var isSalesforceLive: Boolean = false

    @Value("\${application.key.mamasSpaghetti}")
    lateinit var  mamasSpaghetti: String

    @Value("\${application.path.fileIdentifierURL}")
    lateinit var  fileIdentifierURL: String

    @Value("\${application.key.mamasSalt}")
    lateinit var  mamasSalt: String

    @Value("\${application.path.files}")
    lateinit var  filePath: String

    @Value("\${application.path.smsDispositionURL}")
    lateinit var smsDispositionURL: String

    @Value("\${application.s3Bucket.name}")
    lateinit var s3BucketName: String

    @Value("\${application.s3Bucket.region}")
    lateinit var s3BucketRegion: String

    @Value("\${spring.mail.username}")
    lateinit var senderEmail: String

    @Value("\${application.path.callDispositionURL}")
    lateinit var callDispositionURL: String

    @Value("\${application.key.runScheduler}")
    var  runScheduler: Boolean = false

}
