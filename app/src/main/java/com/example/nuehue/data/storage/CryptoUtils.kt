package com.example.nuehue.data.storage

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoUtils {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "hue_credentials_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_SIZE = 12
    private const val TAG_LENGTH_BITS = 128

    fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        val existingKey = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existingKey != null) {
            return existingKey.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val keyGenSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setIsStrongBoxBacked(true)
            .build()

        keyGenerator.init(keyGenSpec)
        return keyGenerator.generateKey()
    }

    fun encrypt(data: String, secretKey: SecretKey): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = ByteArray(IV_SIZE)
        java.security.SecureRandom().nextBytes(iv)

        cipher.init(
            Cipher.ENCRYPT_MODE,
            secretKey,
            GCMParameterSpec(TAG_LENGTH_BITS, iv)
        )

        val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        val combined = iv + encryptedBytes

        return android.util.Base64.encodeToString(combined, android.util.Base64.NO_WRAP)
    }

    fun decrypt(encryptedBase64: String, secretKey: SecretKey): String {
        val decoded = android.util.Base64.decode(encryptedBase64, android.util.Base64.NO_WRAP)

        val iv = decoded.copyOfRange(0, IV_SIZE)
        val encryptedBytes = decoded.copyOfRange(IV_SIZE, decoded.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            secretKey,
            GCMParameterSpec(TAG_LENGTH_BITS, iv)
        )

        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }
}
