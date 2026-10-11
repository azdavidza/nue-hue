package com.example.nuehue.data.api

import com.example.nuehue.data.api.model.DiscoveryResponse
import com.example.nuehue.domain.model.Resource
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

class CloudDiscoveryServiceImpl @Inject constructor(
    private val cloudDiscoveryApi: CloudDiscoveryApi
) : CloudDiscoveryService {

    override suspend fun discoverBridge(): Resource<List<DiscoveryResponse>> {
        return try {
            val response = cloudDiscoveryApi.discoverBridge()

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Discovery failed: ${response.code()}")
            }
        } catch (e: SocketTimeoutException) {
            Resource.Error("Discovery timeout")
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            Resource.Error("Unexpected error: ${e.message}")
        }
    }
}
