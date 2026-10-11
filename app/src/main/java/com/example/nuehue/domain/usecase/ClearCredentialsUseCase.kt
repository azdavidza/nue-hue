package com.example.nuehue.domain.usecase

import com.example.nuehue.data.repository.HueBridgeRepository
import com.example.nuehue.domain.model.Resource
import javax.inject.Inject

class ClearCredentialsUseCase @Inject constructor(
    private val repository: HueBridgeRepository
) {
    suspend operator fun invoke(): Resource<Unit> {
        return repository.clearCredentials()
    }
}
