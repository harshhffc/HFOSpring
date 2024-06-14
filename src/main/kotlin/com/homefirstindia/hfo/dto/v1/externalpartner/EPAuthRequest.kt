package com.homefirstindia.hfo.dto.v1.externalpartner

import com.homefirstindia.hfo.utils.*
import javax.servlet.http.HttpServletRequest

class EPAuthRequest() {

    var authorization = NA
    var clientId = NA
    var clientSecret = NA
    var orgId = NA
    var sessionPasscode = NA
    var ipAddress = NA
    var requestUri = NA

    constructor(request: HttpServletRequest): this() {

        request.run {

            authorization = getHeader(AUTHORIZATION)
            orgId = getHeader(ORG_ID)
            ipAddress = getIPAddress(this)
            requestUri = requestURI

//            if (hasSession || !requestURI.contains("/ep/v1/authenticate"))
//                sessionPasscode = getHeader(SESSION_PASSCODE)

            getHeader(SESSION_PASSCODE)?.let {
                sessionPasscode = it
            }

            getClientCreds(authorization).let {
                clientId = it.clientId
                clientSecret = it.clientSecret
            }

        }

    }

    fun isRequestValid(): Boolean {
        return authorization.isNotNullOrNA() && orgId.isNotNullOrNA()
    }

}

class BasicAuthCreds(
    var clientId: String = NA,
    var clientSecret: String = NA
)
