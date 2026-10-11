package com.example.nuehue.domain.usecase

import com.example.nuehue.data.api.model.HueAuthResponse
import com.example.nuehue.data.repository.HueBridgeRepository
import com.example.nuehue.domain.model.Resource
import kotlinx.coroutines.delay
import javax.inject.Inject

class PairWithBridgeUseCase @Inject constructor(
    private val repository: HueBridgeRepository
) {
    suspend operator fun invoke(
        ip: String,
        deviceType: String = "hue_app#android",
        pollIntervalMs: Long = 1500,
        maxDurationMs: Long = 120000,
        onProgress: (String) -> Unit = {}
    ): Resource<HueAuthResponse> {
        val startTime = System.currentTimeMillis()

        while (System.currentTimeMillis() - startTime < maxDurationMs) {
            val elapsed = System.currentTimeMillis() - startTime
            val remainingSeconds = ((maxDurationMs - elapsed) / 1000).toInt()
            onProgress("Waiting for link button... ${remainingSeconds}s remaining")

            val result = repository.requestAuth(ip, deviceType)

            when (result) {
                is Resource.Success -> return result
                is Resource.Error -> {
                    // Error type 101 means link button not pressed, continue polling
                    if (result.message.contains("link button", ignoreCase = true)) {
                        delay(pollIntervalMs)
                        continue
                    }
                    // Other errors, return immediately
                    return result
                }
                is Resource.Loading -> {
                    delay(pollIntervalMs)
                    continue
                }
                is Resource.Idle -> {
                    delay(pollIntervalMs)
                    continue
                }
            }
        }

        return Resource.Error("Timeout: Link button not pressed within ${maxDurationMs / 1000} seconds")
    }
}
