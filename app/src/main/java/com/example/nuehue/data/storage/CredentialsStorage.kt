package com.example.nuehue.data.storage

import com.example.nuehue.data.api.model.HueCredentials
import com.example.nuehue.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface CredentialsStorage {
    suspend fun saveCredentials(credentials: HueCredentials): Resource<Unit>
    fun getCredentials(): Flow<Resource<HueCredentials?>>
    suspend fun clearCredentials(): Resource<Unit>
}
