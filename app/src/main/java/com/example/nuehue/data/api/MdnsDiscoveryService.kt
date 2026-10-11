package com.example.nuehue.data.api

import com.example.nuehue.data.api.model.HueBridge
import com.example.nuehue.domain.model.Resource

interface MdnsDiscoveryService {
    suspend fun discoverBridges(): Resource<List<HueBridge>>
}
