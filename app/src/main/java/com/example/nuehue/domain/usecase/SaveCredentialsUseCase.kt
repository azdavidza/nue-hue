package com.example.nuehue.domain.usecase

import com.example.nuehue.data.api.model.HueCredentials
import com.example.nuehue.data.repository.HueBridgeRepository
import com.example.nuehue.domain.model.Resource
import javax.inject.Inject

class SaveCredentialsUseCase @Inject constructor(
    private val repository: HueBridgeRepository
) {
    suspend operator fun invoke(credentials: HueCredentials): Resource<Unit> {
        return repository.saveCredentials(credentials)
    }
}
