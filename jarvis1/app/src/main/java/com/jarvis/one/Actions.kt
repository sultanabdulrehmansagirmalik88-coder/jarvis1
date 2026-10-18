package com.jarvis.one

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.json.JSONObject

object Actions {

    fun run(ctx: Context, a: JSONObject): String {
        try {
            when (a.optString("type")) {
                "open_app" -> return openApp(ctx, a.getString("name"))
                "youtube" -> {
                    val q = Uri.encode(a.getString("query"))
                    ctx.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$q"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                    return "▶ YouTube khul gaya"
                }
            }
            val s = JarvisAccessibilityService.instance
                ?: return "⚠ Screen Control on nahi hai (Settings → Enable Screen Control)"
            return when (a.optString("type")) {
                "back" -> { s.back(); "✔ Back" }
                "home" -> { s.home(); "✔ Home" }
                "tap" -> { s.tap(a.getDouble("x").toFloat(), a.getDouble("y").toFloat()); "✔ Tap" }
                "swipe" -> {
                    s.swipe(
                        a.getDouble("x1").toFloat(), a.getDouble("y1").toFloat(),
                        a.getDouble("x2").toFloat(), a.getDouble("y2").toFloat()
                    )
                    "✔ Swipe"
                }
                else -> "⚠ Unknown action"
            }
        } catch (e: Exception) {
            return "⚠ Action fail: ${e.message}"
        }
    }

    private fun openApp(ctx: Context, name: String): String {
        val pm = ctx.packageManager
        val probe = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val match = pm.queryIntentActivities(probe, 0)
            .firstOrNull { it.loadLabel(pm).toString().contains(name, ignoreCase = true) }
            ?: return "⚠ App nahi mila: $name"
        val launch = pm.getLaunchIntentForPackage(match.activityInfo.packageName)
            ?: return "⚠ App open nahi ho saka: $name"
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(launch)
        return "✔ ${match.loadLabel(pm)} khul gaya"
    }
}
