package com.example.nuehue.data.di

import com.example.nuehue.data.storage.CredentialsStorage
import com.example.nuehue.data.storage.CredentialsStorageImpl
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageBindingModule {

    @Binds
    @Singleton
    abstract fun bindCredentialsStorage(
        credentialsStorageImpl: CredentialsStorageImpl
    ): CredentialsStorage

    companion object {
        @Provides
        @Singleton
        fun provideGson(): Gson {
            return com.google.gson.GsonBuilder()
                .setLenient()
                .create()
        }
    }
}
