/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.favorites.FavoriteProjectsRepository
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.screens.BackStackNavKey
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.download.*
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.DownloadCatalogColors
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.DownloadCatalogTheme
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.downloadCatalogGlow
import com.movtery.zalithlauncher.ui.screens.content.download.assets.search.SearchIdScreen
import com.movtery.zalithlauncher.ui.screens.navigateOnce
import com.movtery.zalithlauncher.ui.screens.onBack
import com.movtery.zalithlauncher.ui.screens.rememberTransitionSpec
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.ModpackImportViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel

/**
 * 导航至DownloadScreen
 */
fun ScreenBackStackViewModel.navigateToDownload(targetScreen: TitledNavKey? = null) {
    downloadScreen.clearWith(targetScreen ?: downloadGameScreen)
    mainScreen.removeAndNavigateTo(
        removes = clearBeforeNavKeys,
        screenKey = downloadScreen,
        useClassEquality = true
    )
}

/**
 * 跳转到资源类型对应的下载分类屏幕，并进入项目详情页
 */
private fun ScreenBackStackViewModel.swapToCategoryAssets(
    platform: Platform,
    classes: PlatformClasses,
    projectId: String,
    iconUrl: String?
) {
    val targetScreen = when (classes) {
        PlatformClasses.MOD -> downloadModScreen
        PlatformClasses.MOD_PACK -> downloadModPackScreen
        PlatformClasses.RESOURCE_PACK -> downloadResourcePackScreen
        PlatformClasses.SAVES -> downloadSavesScreen
        PlatformClasses.SHADERS -> downloadShadersScreen
    }
    navigateToDownload(
        targetScreen = targetScreen.apply {
            navigateTo(
                NormalNavKey.DownloadAssets(
                    platform = platform,
                    projectId = projectId,
                    classes = classes,
                    iconUrl = iconUrl
                )
            )
        }
    )
}

@Composable
fun DownloadScreen(
    key: NestedNavKey.Download,
    backScreenViewModel: ScreenBackStackViewModel,
    modpackImportViewModel: ModpackImportViewModel,
    eventViewModel: EventViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    taskCount: Int = 0,
    onShowTasks: () -> Unit = {}
) {
    LaunchedEffect(Unit) { FavoriteProjectsRepository.ensureLoaded() }
    BaseScreen(
        screenKey = key,
        currentKey = backScreenViewModel.mainScreen.currentKey,
        useClassEquality = true
    ) {
        DownloadCatalogTheme {
            BoxWithConstraints(Modifier.fillMaxSize().background(DownloadCatalogColors.background)) {
                val compact = maxHeight < 480.dp
                val narrowRail = maxWidth < 760.dp
                val sidebarWidth = if (narrowRail) 64.dp else 166.dp
                val currentCategory = key.backStack.lastOrNull()
                val nested = currentCategory as? BackStackNavKey<*>
                val canGoBack = (nested?.backStack?.size ?: 0) > 1
                Row(Modifier.fillMaxSize()) {
                    CatalogSidebar(
                        modifier = Modifier.width(sidebarWidth).fillMaxHeight(),
                        compact = compact,
                        iconsOnly = narrowRail,
                        currentCategory = currentCategory,
                        backScreenViewModel = backScreenViewModel
                    )
                    Box(Modifier.fillMaxHeight().width(0.5.dp).background(Color.White.copy(alpha = 0.16f)))
                    Column(Modifier.weight(1f).fillMaxHeight()) {
                        Row(
                            Modifier.fillMaxWidth().padding(start = 18.dp, end = 12.dp, top = if (compact) 8.dp else 16.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (canGoBack) {
                                IconButton(onClick = { nested?.backStack?.removeLastOrNull() }) {
                                    Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Voltar", tint = DownloadCatalogColors.text)
                                }
                            }
                            Column(Modifier.weight(1f)) {
                                Text("Download", color = DownloadCatalogColors.text, fontSize = if (compact) 22.sp else 28.sp, fontWeight = FontWeight.Bold)
                                if (!compact) {
                                    Text(
                                        "Encontre e instale mods, shaders, texturas, mapas e modpacks.",
                                        color = DownloadCatalogColors.secondary, fontSize = 13.sp,
                                        maxLines = 1, overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            if (taskCount > 0) {
                                Row(
                                    Modifier.downloadCatalogGlow(10.dp, 0.55f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable(role = Role.Button, onClickLabel = "Mostrar tarefas", onClick = onShowTasks)
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(painterResource(R.drawable.ic_download_2_outlined), null, Modifier.size(18.dp), tint = DownloadCatalogColors.text)
                                    Text("Tarefas ($taskCount)", color = DownloadCatalogColors.text, fontSize = 12.sp)
                                }
                            }
                        }
                        CatalogTabs(
                            selectedKey = currentCategory,
                            backScreenViewModel = backScreenViewModel,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)
                        )
                        NavigationUI(
                            key = key,
                            backScreenViewModel = backScreenViewModel,
                            eventViewModel = eventViewModel,
                            modpackImportViewModel = modpackImportViewModel,
                            submitError = submitError,
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        )
                    }
                }
            }
        }
    }
}

private data class CatalogDestination(val key: TitledNavKey, val label: String, val icon: Int)

@Composable
private fun CatalogTabs(
    selectedKey: TitledNavKey?,
    backScreenViewModel: ScreenBackStackViewModel,
    modifier: Modifier = Modifier
) {
    val entries = listOf(
        CatalogDestination(backScreenViewModel.downloadModScreen, "Mods", R.drawable.ic_extension_outlined),
        CatalogDestination(backScreenViewModel.downloadShadersScreen, "Shaders", R.drawable.ic_lightbulb),
        CatalogDestination(backScreenViewModel.downloadResourcePackScreen, "Texturas", R.drawable.ic_format_paint_outlined),
        CatalogDestination(backScreenViewModel.downloadSavesScreen, "Mapas", R.drawable.ic_public),
        CatalogDestination(backScreenViewModel.downloadModPackScreen, "Modpacks", R.drawable.ic_package_2_outlined)
    )
    Column(modifier) {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            entries.forEach { entry ->
                val active = selectedKey == entry.key
                Column(
                    Modifier.semantics { selected = active }
                        .clickable(role = Role.Tab) { backScreenViewModel.downloadScreen.backStack.navigateOnce(entry.key) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Icon(painterResource(entry.icon), null, Modifier.size(20.dp), tint = if (active) Color.White else DownloadCatalogColors.secondary)
                        Text(entry.label, color = if (active) Color.White else DownloadCatalogColors.secondary, fontSize = 13.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
                    }
                    Spacer(Modifier.height(2.dp))
                    // A fixed underline occupies the tab width without an infinite Row constraint.
                    if (active) {
                        Box(Modifier.width(68.dp).height(1.dp).downloadCatalogGlow(1.dp).background(Color.White))
                    } else {
                        Spacer(Modifier.height(1.dp))
                    }
                }
            }
        }
        HorizontalDivider(color = Color.White.copy(alpha = 0.14f), thickness = 0.5.dp)
    }
}

@Composable
private fun CatalogSidebar(
    compact: Boolean,
    iconsOnly: Boolean,
    currentCategory: TitledNavKey?,
    backScreenViewModel: ScreenBackStackViewModel,
    modifier: Modifier = Modifier
) {
    fun navigateMain(target: TitledNavKey) {
        backScreenViewModel.mainScreen.removeAndNavigateTo(
            removes = backScreenViewModel.clearBeforeNavKeys,
            screenKey = target
        )
    }
    Column(
        modifier.background(DownloadCatalogColors.sidebar).verticalScroll(rememberScrollState()).padding(horizontal = 8.dp, vertical = if (compact) 10.dp else 20.dp),
        verticalArrangement = Arrangement.spacedBy(if (compact) 3.dp else 8.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(painterResource(R.drawable.ic_package_2_outlined), contentDescription = "Zalith Launcher 2", modifier = Modifier.size(30.dp), tint = Color.White)
            if (!iconsOnly) {
                Column {
                    Text("Zalith", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 23.sp)
                    Text("Launcher 2", color = DownloadCatalogColors.text, fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(if (compact) 2.dp else 12.dp))
        SidebarItem("Início", R.drawable.ic_home_filled, iconsOnly) { backScreenViewModel.mainScreen.clearWith(NormalNavKey.LauncherMain) }
        SidebarItem("Instâncias", R.drawable.ic_folder_outlined, iconsOnly) { navigateMain(NormalNavKey.VersionsManager) }
        SidebarItem("Contas", R.drawable.ic_person_outlined, iconsOnly) { navigateMain(NormalNavKey.AccountManager()) }
        SidebarItem("Download", R.drawable.ic_download_2_outlined, iconsOnly, currentCategory != backScreenViewModel.downloadFavoritesScreen) {
            backScreenViewModel.downloadScreen.backStack.navigateOnce(backScreenViewModel.downloadModScreen)
        }
        SidebarItem("Favoritos", R.drawable.ic_favorite_outlined, iconsOnly, currentCategory == backScreenViewModel.downloadFavoritesScreen) {
            backScreenViewModel.downloadScreen.backStack.navigateOnce(backScreenViewModel.downloadFavoritesScreen)
        }
        SidebarItem("Configurações", R.drawable.ic_settings_filled, iconsOnly) { navigateMain(backScreenViewModel.settingsScreen) }
        HorizontalDivider(Modifier.padding(vertical = 8.dp, horizontal = 8.dp), color = Color.White.copy(alpha = 0.12f), thickness = 0.5.dp)
        SidebarItem("Jogo", R.drawable.ic_sports_esports_outlined, iconsOnly, currentCategory == backScreenViewModel.downloadGameScreen) {
            backScreenViewModel.downloadScreen.backStack.navigateOnce(backScreenViewModel.downloadGameScreen)
        }
        SidebarItem("Buscar por ID", R.drawable.ic_search, iconsOnly, currentCategory == NormalNavKey.SearchId) {
            backScreenViewModel.downloadScreen.backStack.navigateOnce(NormalNavKey.SearchId)
        }
    }
}

@Composable
private fun SidebarItem(label: String, icon: Int, iconsOnly: Boolean, active: Boolean = false, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        Modifier.fillMaxWidth().heightIn(min = 44.dp)
            .then(if (active) Modifier.downloadCatalogGlow(10.dp, 0.35f) else Modifier)
            .clip(shape).background(if (active) DownloadCatalogColors.selected else Color.Transparent)
            .semantics { selected = active }
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(painterResource(icon), contentDescription = if (iconsOnly) label else null, modifier = Modifier.size(22.dp), tint = if (active) Color.White else DownloadCatalogColors.secondary)
        if (!iconsOnly) {
            Text(label, color = if (active) Color.White else DownloadCatalogColors.secondary, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
        }
    }
}

@Composable
private fun NavigationUI(
    key: NestedNavKey.Download,
    backScreenViewModel: ScreenBackStackViewModel,
    eventViewModel: EventViewModel,
    modpackImportViewModel: ModpackImportViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    val backStack = key.backStack
    val stackTopKey = backStack.lastOrNull()
    LaunchedEffect(stackTopKey) {
        backScreenViewModel.downloadScreen.currentKey = stackTopKey
    }

    if (backStack.isNotEmpty()) {
        NavDisplay(
            backStack = backStack,
            modifier = modifier,
            onBack = {
                onBack(backStack)
            },
            transitionSpec = rememberTransitionSpec(),
            popTransitionSpec = rememberTransitionSpec(),
            entryProvider = entryProvider {
                entry<NestedNavKey.DownloadGame> { key ->
                    DownloadGameScreen(
                        key = key,
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        downloadGameScreenKey = backScreenViewModel.downloadGameScreen.currentKey,
                        onCurrentKeyChange = { newKey ->
                            backScreenViewModel.downloadGameScreen.currentKey = newKey
                        },
                        eventViewModel = eventViewModel
                    )
                }
                entry<NestedNavKey.DownloadModPack> { key ->
                    DownloadModPackScreen(
                        key = key,
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        downloadModPackScreenKey = backScreenViewModel.downloadModPackScreen.currentKey,
                        onCurrentKeyChange = { newKey ->
                            backScreenViewModel.downloadModPackScreen.currentKey = newKey
                        },
                        eventViewModel = eventViewModel,
                        importerViewModel = modpackImportViewModel
                    )
                }
                entry<NestedNavKey.DownloadMod> { key ->
                    DownloadModScreen(
                        key = key,
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        downloadModScreenKey = backScreenViewModel.downloadModScreen.currentKey,
                        onCurrentKeyChange = { newKey ->
                            backScreenViewModel.downloadModScreen.currentKey = newKey
                        },
                        submitError = submitError,
                        eventViewModel = eventViewModel
                    )
                }
                entry<NestedNavKey.DownloadResourcePack> { key ->
                    DownloadResourcePackScreen(
                        key = key,
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        downloadResourcePackScreenKey = backScreenViewModel.downloadResourcePackScreen.currentKey,
                        onCurrentKeyChange = { newKey ->
                            backScreenViewModel.downloadResourcePackScreen.currentKey = newKey
                        },
                        submitError = submitError,
                        eventViewModel = eventViewModel
                    )
                }
                entry<NestedNavKey.DownloadSaves> { key ->
                    DownloadSavesScreen(
                        key = key,
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        downloadSavesScreenKey = backScreenViewModel.downloadSavesScreen.currentKey,
                        onCurrentKeyChange = { newKey ->
                            backScreenViewModel.downloadSavesScreen.currentKey = newKey
                        },
                        submitError = submitError,
                        eventViewModel = eventViewModel
                    )
                }
                entry<NestedNavKey.DownloadShaders> { key ->
                    DownloadShadersScreen(
                        key = key,
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        downloadShadersScreenKey = backScreenViewModel.downloadShadersScreen.currentKey,
                        onCurrentKeyChange = { newKey ->
                            backScreenViewModel.downloadShadersScreen.currentKey = newKey
                        },
                        submitError = submitError,
                        eventViewModel = eventViewModel
                    )
                }
                entry<NormalNavKey.SearchId> {
                    SearchIdScreen(
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        swapToDownload = { platform, classes, projectId, iconUrl ->
                            backScreenViewModel.swapToCategoryAssets(platform, classes, projectId, iconUrl)
                        },
                        openLink = { link ->
                            eventViewModel.sendEvent(EventViewModel.Event.OpenLink(link))
                        }
                    )
                }
                entry<NestedNavKey.DownloadFavorites> { key ->
                    DownloadFavoritesScreen(
                        key = key,
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        downloadFavoritesScreenKey = backScreenViewModel.downloadFavoritesScreen.currentKey,
                        onCurrentKeyChange = { newKey ->
                            backScreenViewModel.downloadFavoritesScreen.currentKey = newKey
                        },
                        swapToDownload = { platform, classes, projectId, iconUrl ->
                            backScreenViewModel.swapToCategoryAssets(platform, classes, projectId, iconUrl)
                        }
                    )
                }
            }
        )
    } else {
        Box(modifier)
    }
}