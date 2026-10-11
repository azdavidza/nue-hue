package com.example.nuehue.data.api.model

data class DiscoveryResponse(
    val id: String,
    val internalipaddress: String,
    val macaddress: String? = null,
    val name: String? = null
)
