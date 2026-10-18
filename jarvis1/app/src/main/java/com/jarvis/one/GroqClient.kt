package com.jarvis.one

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class GroqClient(private val prefs: SecurePrefs) {

    private val http = OkHttpClient.Builder()
        .callTimeout(60, TimeUnit.SECONDS)
        .build()
    private val history = JSONArray()

    private val system = """
You are Jarvis, a smart, friendly and concise phone assistant.
Reply in the same language the user writes (Urdu, Roman Urdu, Hindi or English). Keep answers short.
If the user asks for a phone action, add exactly ONE block at the very end:
```action
{"type":"open_app","name":"WhatsApp"}
```
Allowed actions:
{"type":"open_app","name":"<app name>"}
{"type":"youtube","query":"<search text>"}
{"type":"back"}
{"type":"home"}
{"type":"tap","x":540,"y":1200}
{"type":"swipe","x1":540,"y1":1600,"x2":540,"y2":600}
Only add an action block when the user clearly asks for one.
""".trim()

    fun reset() {
        while (history.length() > 0) history.remove(0)
    }

    suspend fun chat(userText: String): String = withContext(Dispatchers.IO) {
        val key = prefs.apiKey
        if (key.isBlank()) throw IllegalStateException("NO_KEY")

        history.put(JSONObject().put("role", "user").put("content", userText))
        while (history.length() > 20) history.remove(0)

        val msgs = JSONArray().put(JSONObject().put("role", "system").put("content", system))
        for (i in 0 until history.length()) msgs.put(history.get(i))

        // xai- keys = Grok (x.ai), baaki sab Groq
        val url = if (key.startsWith("xai-")) "https://api.x.ai/v1/chat/completions"
        else "https://api.groq.com/openai/v1/chat/completions"

        val body = JSONObject()
            .put("model", prefs.model)
            .put("messages", msgs)
            .put("temperature", 0.7)

        val req = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $key")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        http.newCall(req).execute().use { r ->
            val s = r.body?.string().orEmpty()
            if (!r.isSuccessful) {
                history.remove(history.length() - 1)
                val msg = try {
                    JSONObject(s).getJSONObject("error").getString("message")
                } catch (e: Exception) { s.take(200) }
                throw IOException("Error ${r.code}: $msg")
            }
            val reply = JSONObject(s).getJSONArray("choices")
                .getJSONObject(0).getJSONObject("message").getString("content")
            history.put(JSONObject().put("role", "assistant").put("content", reply))
            reply
        }
    }
}
