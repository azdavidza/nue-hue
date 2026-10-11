package com.example.nuehue.data.api

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import com.example.nuehue.data.api.model.HueBridge
import com.example.nuehue.domain.model.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

class MdnsDiscoveryServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MdnsDiscoveryService {

    private val nsdManager: NsdManager by lazy {
        context.getSystemService(Context.NSD_SERVICE) as NsdManager
    }

    private val wifiManager: WifiManager by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    override suspend fun discoverBridges(): Resource<List<HueBridge>> {
        val multicastLock = wifiManager.createMulticastLock("hue-mdns-lock").apply {
            setReferenceCounted(true)
        }

        return try {
            multicastLock.acquire()

            val bridges = discoverBridgesInternal()

            if (bridges.isEmpty()) {
                Resource.Error("No bridges found via mDNS")
            } else {
                Resource.Success(bridges)
            }
        } catch (e: Exception) {
            Resource.Error("Discovery error: ${e.message}")
        } finally {
            if (multicastLock.isHeld) {
                multicastLock.release()
            }
        }
    }

    private suspend fun discoverBridgesInternal(): List<HueBridge> {
        val discoveredBridges = mutableListOf<HueBridge>()
        val resolvedServices = mutableSetOf<String>()

        val discoveryFlow = callbackFlow<Resource<HueBridge>> {
            val discoveryListener = object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(serviceType: String) {
                    // Discovery started
                }

                override fun onDiscoveryStopped(serviceType: String) {
                    // Discovery stopped
                }

                override fun onServiceFound(service: NsdServiceInfo) {
                    // Filter for Hue bridges (service name typically contains "hue" or "Philips")
                    val serviceName = service.serviceName.lowercase()
                    if (serviceName.contains("hue") || serviceName.contains("philips")) {
                        if (!resolvedServices.contains(service.serviceName)) {
                            resolveService(service, discoveredBridges, resolvedServices)
                        }
                    }
                }

                override fun onServiceLost(service: NsdServiceInfo) {
                    // Service lost
                }

                override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                    trySend(Resource.Error("Discovery failed: error $errorCode"))
                    channel.close()
                }

                override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                    // Ignore stop failures
                }
            }

            try {
                nsdManager.discoverServices(
                    "_http._tcp",
                    NsdManager.PROTOCOL_DNS_SD,
                    discoveryListener
                )
            } catch (e: Exception) {
                trySend(Resource.Error("Failed to start discovery: ${e.message}"))
                channel.close()
            }

            awaitClose {
                try {
                    nsdManager.stopServiceDiscovery(discoveryListener)
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }

        // Collect results with timeout
        withTimeoutOrNull(10000) {
            discoveryFlow.collect { result ->
                if (result is Resource.Success) {
                    discoveredBridges.add(result.data)
                }
            }
        }

        return discoveredBridges
    }

    private fun resolveService(
        service: NsdServiceInfo,
        bridges: MutableList<HueBridge>,
        resolvedServices: MutableSet<String>
    ) {
        val resolveListener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                // Resolve failed
            }

            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                val ip = serviceInfo.host.hostAddress
                if (ip != null && !resolvedServices.contains(serviceInfo.serviceName)) {
                    resolvedServices.add(serviceInfo.serviceName)
                    bridges.add(
                        HueBridge(
                            id = serviceInfo.serviceName,
                            ip = ip,
                            name = serviceInfo.serviceName
                        )
                    )
                }
            }
        }

        try {
            nsdManager.resolveService(service, resolveListener)
        } catch (e: Exception) {
            // Ignore resolve errors
        }
    }
}
