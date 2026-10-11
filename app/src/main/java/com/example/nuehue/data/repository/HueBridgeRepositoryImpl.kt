package com.example.nuehue.data.repository

import com.example.nuehue.data.api.CloudDiscoveryService
import com.example.nuehue.data.api.HueLocalService
import com.example.nuehue.data.api.MdnsDiscoveryService
import com.example.nuehue.data.api.model.HueAuthRequest
import com.example.nuehue.data.api.model.HueAuthResponse
import com.example.nuehue.data.api.model.HueBridge
import com.example.nuehue.data.api.model.HueCredentials
import com.example.nuehue.data.storage.CredentialsStorage
import com.example.nuehue.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HueBridgeRepositoryImpl @Inject constructor(
    private val mdnsDiscoveryService: MdnsDiscoveryService,
    private val cloudDiscoveryService: CloudDiscoveryService,
    private val hueLocalService: HueLocalService,
    private val credentialsStorage: CredentialsStorage
) : HueBridgeRepository {

    override suspend fun discoverBridgesMdns(): Resource<List<HueBridge>> {
        return mdnsDiscoveryService.discoverBridges()
    }

    override suspend fun discoverBridgeCloud(): Resource<List<HueBridge>> {
        val cloudResult = cloudDiscoveryService.discoverBridge()
        return when (cloudResult) {
            is Resource.Success -> {
                val bridges = cloudResult.data.map { discoveryResponse ->
                    HueBridge(
                        id = discoveryResponse.id,
                        ip = discoveryResponse.internalipaddress,
                        name = discoveryResponse.name
                    )
                }
                Resource.Success(bridges)
            }
            is Resource.Error -> cloudResult
            is Resource.Loading -> cloudResult
            is Resource.Idle -> cloudResult
        }
    }

    override suspend fun validateConnection(ip: String): Resource<Boolean> {
        return hueLocalService.validateConnection(ip)
    }

    override suspend fun requestAuth(ip: String, deviceType: String): Resource<HueAuthResponse> {
        return hueLocalService.requestAuth(ip, deviceType)
    }

    override suspend fun saveCredentials(credentials: HueCredentials): Resource<Unit> {
        return credentialsStorage.saveCredentials(credentials)
    }

    override fun getCredentials(): Flow<Resource<HueCredentials?>> {
        return credentialsStorage.getCredentials()
    }

    override suspend fun clearCredentials(): Resource<Unit> {
        return credentialsStorage.clearCredentials()
    }
}
