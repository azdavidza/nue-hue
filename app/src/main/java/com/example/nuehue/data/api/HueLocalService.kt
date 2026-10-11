package com.example.nuehue.data.api

import com.example.nuehue.data.api.model.HueAuthRequest
import com.example.nuehue.data.api.model.HueAuthResponse
import com.example.nuehue.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface HueLocalService {
    suspend fun validateConnection(ip: String): Resource<Boolean>
    suspend fun requestAuth(ip: String, deviceType: String): Resource<HueAuthResponse>
}
