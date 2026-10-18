package com.jarvis.one

import org.json.JSONObject

object ActionParser {
    private val re = Regex("```action\\s*(\\{.*?\\})\\s*```", RegexOption.DOT_MATCHES_ALL)

    /** Returns (visible text, action or null) */
    fun parse(raw: String): Pair<String, JSONObject?> {
        val m = re.find(raw) ?: return raw.trim() to null
        val action = try { JSONObject(m.groupValues[1]) } catch (e: Exception) { null }
        return raw.replace(m.value, "").trim() to action
    }
}
