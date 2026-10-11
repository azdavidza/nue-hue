package com.example.nuehue.domain.usecase

import com.example.nuehue.data.api.model.HueBridge
import com.example.nuehue.data.repository.HueBridgeRepository
import com.example.nuehue.domain.model.Resource
import javax.inject.Inject

class DiscoverBridgeUseCase @Inject constructor(
    private val repository: HueBridgeRepository
) {
    suspend operator fun invoke(method: DiscoveryMethod = DiscoveryMethod.MDNS): Resource<List<HueBridge>> {
        return when (method) {
            DiscoveryMethod.MDNS -> repository.discoverBridgesMdns()
            DiscoveryMethod.CLOUD -> repository.discoverBridgeCloud()
        }
    }

    enum class DiscoveryMethod {
        MDNS,
        CLOUD
    }
}
