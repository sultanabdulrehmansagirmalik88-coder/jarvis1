package com.jarvis.one

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = SecurePrefs(this)
        val etKey = findViewById<TextInputEditText>(R.id.etKey)
        val etModel = findViewById<TextInputEditText>(R.id.etModel)
        etKey.setText(prefs.apiKey)
        etModel.setText(prefs.model)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            prefs.apiKey = etKey.text.toString().trim()
            prefs.model = etModel.text.toString().trim().ifBlank { SecurePrefs.DEFAULT_MODEL }
            Toast.makeText(this, "Saved ✔", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<MaterialButton>(R.id.btnAccess).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }
}
