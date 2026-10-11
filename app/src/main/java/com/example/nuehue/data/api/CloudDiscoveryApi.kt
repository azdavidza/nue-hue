package com.example.nuehue.data.api

import com.example.nuehue.data.api.model.DiscoveryResponse
import retrofit2.Response
import retrofit2.http.GET

interface CloudDiscoveryApi {
    @GET("https://discovery.meethue.com/")
    suspend fun discoverBridge(): Response<List<DiscoveryResponse>>
}
