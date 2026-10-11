package com.example.nuehue.data.di

import com.example.nuehue.data.api.CloudDiscoveryApi
import com.example.nuehue.data.api.CloudDiscoveryServiceImpl
import com.example.nuehue.data.api.HueApi
import com.example.nuehue.data.api.HueLocalServiceImpl
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("HueRetrofit")
    fun provideHueRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("http://192.168.1.1/") // Placeholder, will use @Url
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @Named("CloudDiscoveryRetrofit")
    fun provideCloudDiscoveryRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://discovery.meethue.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideHueApi(@Named("HueRetrofit") retrofit: Retrofit): HueApi {
        return retrofit.create(HueApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCloudDiscoveryApi(@Named("CloudDiscoveryRetrofit") retrofit: Retrofit): CloudDiscoveryApi {
        return retrofit.create(CloudDiscoveryApi::class.java)
    }

    @Provides
    @Singleton
    fun provideHueLocalService(
        hueApi: HueApi
    ): com.example.nuehue.data.api.HueLocalService {
        return HueLocalServiceImpl(hueApi)
    }

    @Provides
    @Singleton
    fun provideCloudDiscoveryService(
        cloudDiscoveryApi: CloudDiscoveryApi
    ): com.example.nuehue.data.api.CloudDiscoveryService {
        return CloudDiscoveryServiceImpl(cloudDiscoveryApi)
    }
}
