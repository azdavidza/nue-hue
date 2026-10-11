package com.example.nuehue.domain.usecase

import com.example.nuehue.data.api.model.HueCredentials
import com.example.nuehue.data.repository.HueBridgeRepository
import com.example.nuehue.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStoredCredentialsUseCase @Inject constructor(
    private val repository: HueBridgeRepository
) {
    operator fun invoke(): Flow<Resource<HueCredentials?>> {
        return repository.getCredentials()
    }
}
