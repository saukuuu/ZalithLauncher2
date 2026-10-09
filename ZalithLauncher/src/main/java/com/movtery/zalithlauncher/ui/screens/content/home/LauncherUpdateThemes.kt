package com.movtery.zalithlauncher.ui.screens.content.home

import com.movtery.zalithlauncher.R

/** One artwork and update name per release family, shared by both screens. */
data class LauncherUpdateTheme(
    val family: String,
    val title: String,
    val artwork: Int
)

object LauncherUpdateThemes {
    val all: List<LauncherUpdateTheme> = listOf(
        LauncherUpdateTheme("26.3", "Wilderness Bound", R.drawable.launcher_update_26_3),
        LauncherUpdateTheme("26.2", "Chaos Cubed", R.drawable.launcher_update_26_2),
        LauncherUpdateTheme("26.1", "Tiny Takeover", R.drawable.launcher_update_26_1),
        LauncherUpdateTheme("1.21", "Tricky Trials", R.drawable.launcher_update_1_21),
        LauncherUpdateTheme("1.20", "Trails & Tales", R.drawable.launcher_update_1_20),
        LauncherUpdateTheme("1.19", "The Wild Update", R.drawable.launcher_update_1_19),
        LauncherUpdateTheme("1.18", "Caves & Cliffs: Part II", R.drawable.launcher_update_1_18),
        LauncherUpdateTheme("1.17", "Caves & Cliffs: Part I", R.drawable.launcher_update_1_17),
        LauncherUpdateTheme("1.16", "Nether Update", R.drawable.launcher_update_1_16),
        LauncherUpdateTheme("1.15", "Buzzy Bees", R.drawable.launcher_update_1_15)
    )

    private val byFamily = all.associateBy { it.family }
    private val releaseId = Regex(
        """^(?:Minecraft\s+)?(\d+\.\d+)(?:\.\d+)*(?:[-_ ].*)?$""",
        RegexOption.IGNORE_CASE
    )

    /** 1.17.1 -> 1.17; 26.3-rc1 -> 26.3. Unknown snapshots stay unthemed. */
    fun familyOf(versionId: String?): String? = versionId?.trim()?.let {
        releaseId.matchEntire(it)?.groupValues?.get(1)
    }

    fun forVersion(versionId: String?): LauncherUpdateTheme? =
        familyOf(versionId)?.let(byFamily::get)
}
