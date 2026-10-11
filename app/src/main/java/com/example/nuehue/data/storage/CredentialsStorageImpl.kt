package com.example.nuehue.data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.nuehue.data.api.model.HueCredentials
import com.example.nuehue.domain.model.Resource
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.credentialsDataStore: DataStore<Preferences> by preferencesDataStore(name = "hue_credentials")

@Singleton
class CredentialsStorageImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) : CredentialsStorage {

    private val secretKey by lazy { CryptoUtils.getOrCreateSecretKey() }
    private val credentialsKey = stringPreferencesKey("encrypted_credentials")

    override suspend fun saveCredentials(credentials: HueCredentials): Resource<Unit> {
        return try {
            val json = gson.toJson(credentials)
            val encrypted = CryptoUtils.encrypt(json, secretKey)

            context.credentialsDataStore.edit { preferences ->
                preferences[credentialsKey] = encrypted
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to save credentials: ${e.message}")
        }
    }

    override fun getCredentials(): Flow<Resource<HueCredentials?>> {
        return context.credentialsDataStore.data.map { preferences ->
            try {
                val encrypted = preferences[credentialsKey]
                if (encrypted.isNullOrBlank()) {
                    Resource.Success(null)
                } else {
                    val decrypted = CryptoUtils.decrypt(encrypted, secretKey)
                    val credentials = gson.fromJson(decrypted, HueCredentials::class.java)
                    Resource.Success(credentials)
                }
            } catch (e: IOException) {
                Resource.Error("Failed to read credentials: ${e.message}")
            } catch (e: Exception) {
                Resource.Error("Failed to decrypt credentials: ${e.message}")
            }
        }
    }

    override suspend fun clearCredentials(): Resource<Unit> {
        return try {
            context.credentialsDataStore.edit { preferences ->
                preferences.remove(credentialsKey)
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to clear credentials: ${e.message}")
        }
    }
}
