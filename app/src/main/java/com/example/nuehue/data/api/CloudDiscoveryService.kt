package com.example.nuehue.data.api

import com.example.nuehue.data.api.model.DiscoveryResponse
import com.example.nuehue.domain.model.Resource

interface CloudDiscoveryService {
    suspend fun discoverBridge(): Resource<List<DiscoveryResponse>>
}
