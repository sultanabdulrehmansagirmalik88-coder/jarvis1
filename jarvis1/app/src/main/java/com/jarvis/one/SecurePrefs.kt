package com.jarvis.one

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/** API key AES-256 encrypted, sirf phone par. */
class SecurePrefs(ctx: Context) {
    private val p = EncryptedSharedPreferences.create(
        ctx,
        "jarvis_secure",
        MasterKey.Builder(ctx).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var apiKey: String
        get() = p.getString("api_key", "") ?: ""
        set(v) = p.edit().putString("api_key", v).apply()

    var model: String
        get() = p.getString("model", DEFAULT_MODEL) ?: DEFAULT_MODEL
        set(v) = p.edit().putString("model", v).apply()

    companion object {
        const val DEFAULT_MODEL = "llama-3.3-70b-versatile"
    }
}
