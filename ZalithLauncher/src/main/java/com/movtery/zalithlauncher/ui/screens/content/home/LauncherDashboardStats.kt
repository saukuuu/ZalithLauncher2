package com.movtery.zalithlauncher.ui.screens.content.home

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DashboardSnapshot(val launches: Int, val lastVersion: String?, val lastDate: String?, val mostPlayedVersion: String?, val uniqueVersions: Int)

/** Records launch requests, not confirmed playtime. */
object LauncherDashboardStats {
    private var revision by mutableStateOf(0)
    private const val PREFS = "home_dashboard_stats"

    fun read(context: Context): DashboardSnapshot {
        revision // Observe writes in Compose
        val p = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val versionCounts = p.all.filterKeys { it.startsWith("count_") }
        val top = versionCounts.maxByOrNull { (it.value as? Int) ?: 0 }?.key?.removePrefix("count_")
        return DashboardSnapshot(
            launches = p.getInt("launches", 0),
            lastVersion = p.getString("last_version", null),
            lastDate = p.getString("last_date", null),
            mostPlayedVersion = top,
            uniqueVersions = versionCounts.size
        )
    }

    fun recordLaunch(context: Context, versionName: String?) {
        val p = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val name = versionName ?: "Versão não identificada"
        val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        p.edit()
            .putInt("launches", p.getInt("launches", 0) + 1)
            .putString("last_version", name)
            .putInt("count_$name", p.getInt("count_$name", 0) + 1)
            .putString("last_date", date)
            .apply()
        revision++
    }
}
