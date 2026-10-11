package com.example.nuehue.data.api

import com.example.nuehue.data.api.model.HueAuthRequest
import com.example.nuehue.data.api.model.HueAuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface HueApi {
    @GET
    suspend fun validateConnection(@Url url: String): Response<Unit>

    @POST
    suspend fun requestAuth(
        @Url url: String,
        @Body request: HueAuthRequest
    ): Response<List<HueAuthResponse>>
}
