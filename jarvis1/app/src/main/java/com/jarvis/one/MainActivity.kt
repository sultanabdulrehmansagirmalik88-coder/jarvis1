package com.jarvis.one

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SecurePrefs
    private lateinit var client: GroqClient
    private lateinit var adapter: ChatAdapter
    private lateinit var rv: RecyclerView
    private lateinit var et: EditText
    private val messages = mutableListOf<Msg>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = SecurePrefs(this)
        client = GroqClient(prefs)

        rv = findViewById(R.id.rv)
        et = findViewById(R.id.etInput)
        adapter = ChatAdapter(messages)
        rv.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        rv.adapter = adapter

        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnSend).setOnClickListener { send() }
        et.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_SEND) { send(); true } else false
        }

        add(Msg("Salaam! Main Jarvis hoon. Pehle ⚙ Settings mein Groq API key daal do, phir baat karte hain.", false))
    }

    private fun add(m: Msg): Int {
        messages.add(m)
        val i = messages.size - 1
        adapter.notifyItemInserted(i)
        rv.scrollToPosition(i)
        return i
    }

    private fun set(i: Int, m: Msg) {
        messages[i] = m
        adapter.notifyItemChanged(i)
        rv.scrollToPosition(messages.size - 1)
    }

    private fun send() {
        val text = et.text.toString().trim()
        if (text.isEmpty()) return
        et.setText("")

        if (prefs.apiKey.isBlank()) {
            add(Msg("API key missing hai. ⚙ Settings kholo aur Groq key paste karo.", false))
            startActivity(Intent(this, SettingsActivity::class.java))
            return
        }

        add(Msg(text, true))
        val idx = add(Msg("…", false))

        lifecycleScope.launch {
            try {
                val raw = client.chat(text)
                val (clean, action) = ActionParser.parse(raw)
                set(idx, Msg(clean.ifBlank { "Ho gaya." }, false))
                if (action != null) {
                    add(Msg(Actions.run(this@MainActivity, action), false))
                }
            } catch (e: Exception) {
                set(idx, Msg("⚠ ${e.message}", false))
            }
        }
    }
}
