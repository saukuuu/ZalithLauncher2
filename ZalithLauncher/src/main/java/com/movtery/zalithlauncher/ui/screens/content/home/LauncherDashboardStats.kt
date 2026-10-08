package com.movtery.zalithlauncher.ui.screens.content.home

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.saves.SaveData
import com.movtery.zalithlauncher.game.version.saves.parseLevelDatFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Launch requests are kept separate from elapsed world time. */
data class DashboardSnapshot(
    val launches: Int = 0,
    val lastVersion: String? = null,
    val lastDate: String? = null,
    val mostPlayedVersion: String? = null,
    val uniqueVersions: Int = 0
)

data class DashboardWorlds(
    val count: Int = 0,
    val ticks: Long = 0,
    val latest: SaveData? = null
)

object LauncherDashboardStats {
    var revision by mutableStateOf(0)
        private set
    private const val PREFS = "home_dashboard_stats"

    fun read(context: Context): DashboardSnapshot {
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
        if (versionName.isNullOrBlank()) return
        val p = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        p.edit()
            .putInt("launches", p.getInt("launches", 0) + 1)
            .putString("last_version", versionName)
            .putInt("count_$versionName", p.getInt("count_$versionName", 0) + 1)
            .putString("last_date", formatDate(System.currentTimeMillis()))
            .apply()
        revision++
    }

    /** Read-only, off the UI thread. Shared save directories are counted once. */
    suspend fun readWorlds(versions: List<Version>): DashboardWorlds = withContext(Dispatchers.IO) {
        val directories = versions.mapNotNull { version ->
            runCatching { File(version.getGameDir(), "saves").canonicalFile }.getOrNull()
        }.distinctBy { it.path }
        val saves = mutableListOf<SaveData>()
        val seen = mutableSetOf<String>()
        for (directory in directories) {
            ensureActive()
            for (world in directory.listFiles().orEmpty()) {
                ensureActive()
                val canonical = runCatching { world.canonicalPath }.getOrNull() ?: continue
                if (!seen.add(canonical) || !world.isDirectory) continue
                val level = File(world, "level.dat")
                if (!level.isFile) continue
                val save = parseLevelDatFile(world, level)
                if (save.isValid) saves += save
            }
        }
        DashboardWorlds(
            count = saves.size,
            ticks = saves.sumOf { (it.playTime ?: 0L).coerceAtLeast(0L) },
            latest = saves.filter { (it.lastPlayed ?: 0L) > 0L }.maxByOrNull { it.lastPlayed ?: 0L }
        )
    }

    fun formatWorldTime(ticks: Long): String {
        val minutes = ticks.coerceAtLeast(0L) / 1200L
        return "${minutes / 60L}h ${minutes % 60L}min"
    }

    fun formatDate(timestamp: Long): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
}
