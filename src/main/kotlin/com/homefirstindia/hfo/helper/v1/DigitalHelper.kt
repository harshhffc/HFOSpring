package com.homefirstindia.hfo.helper.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.homefirstindia.hfo.manager.v1.CredsManager
import com.homefirstindia.hfo.networking.v1.CommonNetworkingClient
import com.homefirstindia.hfo.utils.CryptoUtils
import com.homefirstindia.hfo.utils.OneResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class DigitalHelper(
    @Autowired val credentialManager: CredsManager,
    @Autowired val cryptoUtils: CryptoUtils,
    @Autowired val commonNetworkingClient: CommonNetworkingClient,
    @Autowired val oneResponse: OneResponse,
    @Autowired val objectMapper: ObjectMapper
)