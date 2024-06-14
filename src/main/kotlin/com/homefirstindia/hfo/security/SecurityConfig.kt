package com.homefirstindia.hfo.security

import com.homefirstindia.hfo.dto.v1.externalpartner.EPAuthRequest
import com.homefirstindia.hfo.repository.v1.PartnerMasterRepository
import com.homefirstindia.hfo.utils.*
import org.apache.http.entity.ContentType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Order(1)
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
    prePostEnabled = true,
    securedEnabled = true,
    jsr250Enabled = true
)
class ExternalPartnerSecurityConfig(
    @Autowired val partnerAuthentication: PartnerAuthentication
) {


    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors()
            .and()
            .csrf().disable()
//            .requestMatchers()
//            .antMatchers("/ep/**")
//            .and()
            .authorizeRequests()
            .antMatchers("/public/**").permitAll()
            .antMatchers("/ep/v1/authenticate").permitAll()
            .antMatchers("/ep/**", "/com/**", "/kyc/**").authenticated()
            .and()
            .addFilterBefore(partnerAuthentication, FilterSecurityInterceptor::class.java)

        return http.build()
    }

    private val domains = listOf(
        "https://lms.homefirstindia.com",
        "https://rm.homefirstindia.com",
        "https://developer.homefirstindia.com",
        "https://one.homefirstindia.com",
        "https://customers.homefirstindia.com",
        "https://test.homefirstindia.com"
    )

    private val allowHeader = listOf(
        "Origin",
        "X-Requested-With",
        "Content-Type",
        "Accept",
        "Key",
        "Authorization",
        "refreshToken",
        "crypt",
        "cypher",
        "userId",
        "sessionPasscode",
        "sourcePasscode"
    )

    private val maxAge: Long = 3600

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource? {
        val source = UrlBasedCorsConfigurationSource()

        val configuration = CorsConfiguration()
        configuration.allowedOrigins = domains
        configuration.allowedMethods = listOf("GET", "POST")
        configuration.maxAge = maxAge
        configuration.allowedHeaders = allowHeader

        source.registerCorsConfiguration("/**", configuration)

        return source
    }

}

private fun HttpServletResponse.setFailureResponse(
    statusCode: Int,
    localResponse: LocalResponse
) {
    this.apply {
        contentType = ContentType.APPLICATION_JSON.toString()
        status = statusCode
        outputStream.println(
            localResponse
                .toJson()
                .toString()
        )
    }
}

@Component
class PartnerAuthentication(
    @Autowired val partnerMasterRepository: PartnerMasterRepository,
    @Autowired val cryptoUtils: CryptoUtils
) : OncePerRequestFilter() {

    private fun log(value: String) = LoggerUtils.log("PartnerAuthentication.$value")

    @Throws(Exception::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        if (!(request.requestURI.contains("/hfo/ep/v1/")
                    || request.requestURI.contains("/hfo/com/v1/")
                    || request.requestURI.contains("/hfo/kyc/v1/"))
        ) {
            log("doFilterInternal - Path : ${request.requestURI}")
            filterChain.doFilter(request, response)
            return
        }

        val epAuthRequest = EPAuthRequest(request)
        log("doFilterInternal - OrgId : ${epAuthRequest.orgId}")
        log("doFilterInternal - IP Address : ${epAuthRequest.ipAddress}")
        log("doFilterInternal - Endpoint : ${epAuthRequest.requestUri}")

        if (!epAuthRequest.isRequestValid()) {
            log("doFilterInternal - Invalid request.")

            response.setFailureResponse(
                201,
                LocalResponse()
                    .setMessage("Invalid request.")
                    .setError(Errors.INVALID_REQUEST.value)
                    .setAction(Actions.FIX_RETRY.value)
            )

            return

        }

        val externalPartner = partnerMasterRepository.partnerRepository.findByOrgId(epAuthRequest.orgId)
        externalPartner ?: run {

            response.setFailureResponse(
                201,
                LocalResponse()
                    .setMessage("No partner found for orgId : ${epAuthRequest.orgId}")
                    .setError(Errors.RESOURCE_NOT_FOUND.value)
                    .setAction(Actions.CONTACT_ADMIN.value)
            )

            return
        }

        if (!externalPartner.isEnabled) {
            log("doFilterInternal - Partner is not enabled for orgId: ${epAuthRequest.orgId}")

            response.setFailureResponse(
                201,
                LocalResponse()
                    .setMessage("Your access is disabled. Please contact system admin.")
                    .setError(Errors.ACCESS_DENIED.value)
                    .setAction(Actions.CONTACT_ADMIN.value)
            )

            return

        }

        if (externalPartner.ipRestricted) {

            partnerMasterRepository.whitelistedIPRepository
                .findAllByOrgId(epAuthRequest.orgId)?.let { wi ->

                    if (wi.isNotEmpty()) {

                        val ipAddressed = ArrayList<String>()
                        for (ip in wi) {
                            ipAddressed.add(ip.ipAddress)
                        }

                        if (!ipAddressed.contains(epAuthRequest.ipAddress)) {
                            log("doFilterInternal - No ip whitelisted was found: ${epAuthRequest.orgId}")

                            response.setFailureResponse(
                                201,
                                LocalResponse()
                                    .setMessage("Your IP Address is blocked. Please contact system admin.")
                                    .setError(Errors.ACCESS_DENIED.value)
                                    .setAction(Actions.CONTACT_ADMIN.value)
                            )

                            return

                        }

                    } else {

                        log("doFilterInternal - No ip whitelisted was found: ${epAuthRequest.orgId}")

                        response.setFailureResponse(
                            201,
                            LocalResponse()
                                .setMessage("Your IP Address is blocked. Please contact system admin.")
                                .setError(Errors.ACCESS_DENIED.value)
                                .setAction(Actions.CONTACT_ADMIN.value)
                        )

                        return

                    }

                } ?: run {

                log("doFilterInternal - IP address is blocked.")

                response.setFailureResponse(
                    201,
                    LocalResponse()
                        .setMessage("Your IP Address is blocked. Please contact system admin.")
                        .setError(Errors.ACCESS_DENIED.value)
                        .setAction(Actions.CONTACT_ADMIN.value)
                )

                return
            }

        }

        val clientId = cryptoUtils.encryptAes(epAuthRequest.clientId)
        val clientSecret = cryptoUtils.encryptAes(epAuthRequest.clientSecret)

        when {

            clientId != externalPartner.clientId
                    || clientSecret != externalPartner.clientSecret -> {

                log("doFilterInternal - Incorrect client Id or Secret.")

                response.setFailureResponse(
                    201,
                    LocalResponse()
                        .setMessage("Incorrect client Id or Secret.")
                        .setError(Errors.INVALID_CREDENTIALS.value)
                        .setAction(Actions.FIX_RETRY.value)
                )

                return
            }


            !externalPartner.sessionEnabled
                    || epAuthRequest.requestUri.contains("/ep/v1/authenticate") -> {
                log("doFilterInternal - No session passcode required. Authenticated!")
                request.getRequestDispatcher(request.servletPath).forward(request, response)
                return
            }

            externalPartner.sessionEnabled
                    && epAuthRequest.sessionPasscode.isInvalid() -> throw Exception("Invalid sessionPasscode.")

            else -> {

                if (externalPartner.sessionEnabled) {

                    if (epAuthRequest.sessionPasscode != externalPartner.sessionPasscode) {
                        log("doFilterInternal - Invalid sessionPasscode.")

                        response.setFailureResponse(
                            401,
                            LocalResponse()
                                .setMessage("Invalid sessionPasscode.")
                                .setError(Errors.UNAUTHORIZED_ACCESS.value)
                                .setAction(Actions.AUTHENTICATE_AGAIN.value)
                        )

                        return

                    }

                    if (!externalPartner.isSessionValid()) {
                        log("doFilterInternal - sessionPasscode expired.")

                        response.setFailureResponse(
                            401,
                            LocalResponse()
                                .setMessage("sessionPasscode expired. Please authenticate again.")
                                .setError(Errors.UNAUTHORIZED_ACCESS.value)
                                .setAction(Actions.AUTHENTICATE_AGAIN.value)
                        )

                        return

                    }

                    if (externalPartner.shouldIncreaseSessionValidity()) {

                        externalPartner.apply {
                            updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
                            sessionUpdateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
                            sessionValidDatetime = DateTimeUtils.getDateTimeByAddingHours(1)
                        }

                        partnerMasterRepository.partnerRepository.save(externalPartner)

                    }

                }

                log("doFilterInternal - Partner and session authenticated.")
                request.getRequestDispatcher(request.servletPath).forward(request, response)

            }
        }

    }

}
