package com.example.nuehue.data.repository

import com.example.nuehue.data.api.model.HueAuthRequest
import com.example.nuehue.data.api.model.HueAuthResponse
import com.example.nuehue.data.api.model.HueBridge
import com.example.nuehue.data.api.model.HueCredentials
import com.example.nuehue.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface HueBridgeRepository {
    suspend fun discoverBridgesMdns(): Resource<List<HueBridge>>
    suspend fun discoverBridgeCloud(): Resource<List<HueBridge>>
    suspend fun validateConnection(ip: String): Resource<Boolean>
    suspend fun requestAuth(ip: String, deviceType: String): Resource<HueAuthResponse>
    suspend fun saveCredentials(credentials: HueCredentials): Resource<Unit>
    fun getCredentials(): Flow<Resource<HueCredentials?>>
    suspend fun clearCredentials(): Resource<Unit>
}
