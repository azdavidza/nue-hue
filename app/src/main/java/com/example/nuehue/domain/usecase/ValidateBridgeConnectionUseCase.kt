package com.example.nuehue.domain.usecase

import com.example.nuehue.data.repository.HueBridgeRepository
import com.example.nuehue.domain.model.Resource
import javax.inject.Inject

class ValidateBridgeConnectionUseCase @Inject constructor(
    private val repository: HueBridgeRepository
) {
    suspend operator fun invoke(ip: String): Resource<Boolean> {
        return repository.validateConnection(ip)
    }
}
