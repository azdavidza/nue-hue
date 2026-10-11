package com.example.nuehue.data.api

import com.example.nuehue.data.api.model.HueAuthRequest
import com.example.nuehue.data.api.model.HueAuthResponse
import com.example.nuehue.domain.model.Resource
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

class HueLocalServiceImpl @Inject constructor(
    private val hueApi: HueApi
) : HueLocalService {

    override suspend fun validateConnection(ip: String): Resource<Boolean> {
        return try {
            val url = "http://$ip/api/config"
            val response = hueApi.validateConnection(url)

            if (response.isSuccessful) {
                Resource.Success(true)
            } else {
                Resource.Error("Connection failed: ${response.code()}")
            }
        } catch (e: SocketTimeoutException) {
            Resource.Error("Connection timeout. Check if the bridge is on the same network.")
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            Resource.Error("Unexpected error: ${e.message}")
        }
    }

    override suspend fun requestAuth(ip: String, deviceType: String): Resource<HueAuthResponse> {
        return try {
            val url = "http://$ip/api"
            val request = HueAuthRequest(deviceType)
            val response = hueApi.requestAuth(url, request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!.first()
                if (authResponse.success != null) {
                    Resource.Success(authResponse)
                } else if (authResponse.error != null) {
                    Resource.Error(authResponse.error.description)
                } else {
                    Resource.Error("Unknown authentication error")
                }
            } else {
                Resource.Error("Authentication request failed: ${response.code()}")
            }
        } catch (e: SocketTimeoutException) {
            Resource.Error("Connection timeout")
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            Resource.Error("Unexpected error: ${e.message}")
        }
    }
}
