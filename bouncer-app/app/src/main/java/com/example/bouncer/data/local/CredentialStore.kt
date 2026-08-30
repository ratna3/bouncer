package com.example.bouncer.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Securely stores router admin credentials using EncryptedSharedPreferences.
 * The router password is never hardcoded — it flows through this store only.
 */
class CredentialStore(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "bouncer_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveCredentials(username: String, password: String) {
        prefs.edit()
            .putString(KEY_USERNAME, username)
            .putString(KEY_PASSWORD, password)
            .apply()
    }

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)
    fun getPassword(): String? = prefs.getString(KEY_PASSWORD, null)
    fun hasCredentials(): Boolean = !getUsername().isNullOrBlank() && !getPassword().isNullOrBlank()

    fun saveRouterIp(ip: String) {
        prefs.edit().putString(KEY_ROUTER_IP, ip).apply()
    }

    fun getRouterIp(): String = prefs.getString(KEY_ROUTER_IP, DEFAULT_ROUTER_IP) ?: DEFAULT_ROUTER_IP

    fun clearCredentials() {
        prefs.edit()
            .remove(KEY_USERNAME)
            .remove(KEY_PASSWORD)
            .apply()
    }

    companion object {
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_ROUTER_IP = "router_ip"
        private const val DEFAULT_ROUTER_IP = "http://192.168.1.1"
    }
}
