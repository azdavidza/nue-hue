package com.example.nuehue.data.di

import com.example.nuehue.data.repository.HueBridgeRepository
import com.example.nuehue.data.repository.HueBridgeRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHueBridgeRepository(
        hueBridgeRepositoryImpl: HueBridgeRepositoryImpl
    ): HueBridgeRepository
}
