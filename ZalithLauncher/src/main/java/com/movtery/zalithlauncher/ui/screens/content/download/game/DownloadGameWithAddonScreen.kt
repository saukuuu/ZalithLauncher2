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

package com.movtery.zalithlauncher.ui.screens.content.download.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.addons.modloader.cleanroom.CleanroomVersions
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.fabric.FabricAPIVersions
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.fabric.FabricVersions
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.legacyfabric.LegacyFabricAPIVersions
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.legacyfabric.LegacyFabricVersions
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.quilt.QuiltAPIVersions
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.quilt.QuiltVersions
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.forge.ForgeVersion
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.forge.ForgeVersions
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.neoforge.NeoForgeVersions
import com.movtery.zalithlauncher.game.addons.modloader.optifine.OptiFineVersion
import com.movtery.zalithlauncher.game.addons.modloader.optifine.OptiFineVersions
import com.movtery.zalithlauncher.game.download.game.GameDownloadInfo
import com.movtery.zalithlauncher.game.version.installed.VersionsManager.isVersionExists
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedColumn
import com.movtery.zalithlauncher.ui.components.SimpleTextInputField
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.isFilenameInvalid
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

private class AddonsViewModel(
    private val gameVersion: String,
    loaderSupports: LoaderVerSupports
) : ViewModel() {
    val addonList = AddonList()
    val currentAddon = CurrentAddon()
    var refreshIcon by mutableStateOf(false)
        private set

    fun refreshIcon() {
        refreshIcon = !refreshIcon
    }

    fun reloadOptiFine() = launchAddonReload(
        { currentAddon.optifineState = it },
        { OptiFineVersions.fetchOptiFineList(gameVersion = gameVersion) },
        { addonList.optifineList = it }
    )

    fun reloadForge() = launchAddonReload(
        { currentAddon.forgeState = it },
        { ForgeVersions.fetchForgeList(gameVersion) },
        { addonList.forgeList = it }
    )

    fun reloadNeoForge() = launchAddonReload(
        { currentAddon.neoforgeState = it },
        { NeoForgeVersions.fetchNeoForgeList(gameVersion = gameVersion) },
        { addonList.neoforgeList = it }
    )

    fun reloadFabric() = launchAddonReload(
        { currentAddon.fabricState = it },
        { FabricVersions.fetchFabricLoaderList(gameVersion) },
        { addonList.fabricList = it }
    )

    fun reloadFabricAPI() = launchAddonReload(
        { currentAddon.fabricAPIState = it },
        { FabricAPIVersions.fetchVersionList(gameVersion) },
        {
            addonList.fabricAPIList = it
            //检查用户是否已经选择了 Fabric Loader
            if (currentAddon.fabricVersion.value != null) {
                //如果已经选择，这里将会自动选择 Fabric API
                currentAddon.fabricAPIVersion.value = it?.firstOrNull()
            }
        }
    )

    fun reloadLegacyFabric() = launchAddonReload(
        { currentAddon.legacyFabricState = it },
        { LegacyFabricVersions.fetchFabricLoaderList(gameVersion) },
        { addonList.legacyFabricList = it }
    )

    fun reloadLegacyFabricAPI() = launchAddonReload(
        { currentAddon.legacyFabricAPIState = it },
        { LegacyFabricAPIVersions.fetchVersionList(gameVersion) },
        {
            addonList.legacyFabricAPIList = it
            //检查用户是否已经选择了 Legacy Fabric
            if (currentAddon.legacyFabricVersion.value != null) {
                //如果已经选择，这里将会自动选择 Legacy Fabric API
                currentAddon.legacyFabricAPIVersion.value = it?.firstOrNull()
            }
        }
    )

    fun reloadQuilt() = launchAddonReload(
        { currentAddon.quiltState = it },
        { QuiltVersions.fetchQuiltLoaderList(gameVersion) },
        { addonList.quiltList = it }
    )

    fun reloadQuiltAPI() = launchAddonReload(
        { currentAddon.quiltAPIState = it },
        { QuiltAPIVersions.fetchVersionList(gameVersion) },
        {
            addonList.quiltAPIList = it
            //检查用户是否已经选择了 Quilt Loader
            if (currentAddon.quiltVersion.value != null) {
                //如果已经选择，这里将会自动选择 Quilted Fabric API
                currentAddon.quiltAPIVersion.value = it?.firstOrNull()
            }
        }
    )

    fun reloadCleanroom() = launchAddonReload(
        { currentAddon.cleanroomState = it },
        { CleanroomVersions.fetchLoaderList(gameVersion) },
        { addonList.cleanroomList = it }
    )

    private fun <T> launchAddonReload(
        updateState: (AddonState) -> Unit,
        fetch: suspend () -> T?,
        onSuccess: (T?) -> Unit
    ) {
        viewModelScope.launch {
            runWithState(updateState, fetch).also(onSuccess)
        }
    }

    init {
        reloadOptiFine()
        reloadForge()
        if (loaderSupports.isNeoForgeSupports) {
            reloadNeoForge()
        }
        if (loaderSupports.isFabricSupports) {
            reloadFabric()
            reloadFabricAPI()
        }
        if (loaderSupports.isLegacyFabricSupports) {
            reloadLegacyFabric()
            reloadLegacyFabricAPI()
        }
        if (loaderSupports.isQuiltSupports) {
            reloadQuilt()
            reloadQuiltAPI()
        }
        if (loaderSupports.isCleanroomSupports) {
            reloadCleanroom()
        }
    }

    override fun onCleared() {
        viewModelScope.cancel()
    }
}

/**
 * 下载游戏页面（选择附加内容）
 * @param refreshErrorCheck 刷新版本名称错误检查
 */
@Composable
fun DownloadGameWithAddonScreen(
    mainScreenKey: TitledNavKey?,
    downloadScreenKey: TitledNavKey?,
    downloadGameScreenKey: TitledNavKey?,
    key: NormalNavKey.DownloadGame.Addons,
    refreshErrorCheck: Any? = null,
    onInstall: (GameDownloadInfo) -> Unit = {}
) {
    val loaderSupports = rememberLoaderVerSupports(key.gameVersion)

    val viewModel = viewModel(
        key = key.toString() + "_" + loaderSupports
    ) {
        AddonsViewModel(key.gameVersion, loaderSupports)
    }

    BaseScreen(
        listOf(
            Pair(NestedNavKey.Download::class.java, mainScreenKey),
            Pair(NestedNavKey.DownloadGame::class.java, downloadScreenKey),
            Pair(NormalNavKey.DownloadGame.Addons::class.java, downloadGameScreenKey)
        )
    ) { isVisible ->
        val yOffset by swapAnimateDpAsState(
            targetValue = (-40).dp,
            swapIn = isVisible
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
        ) {
            ScreenHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                itemContainerColor = cardColor(),
                itemContentColor = onCardColor(),
                gameVersion = key.gameVersion,
                currentAddon = viewModel.currentAddon,
                refreshIcon = viewModel.refreshIcon,
                refreshErrorCheck = refreshErrorCheck,
                onVanilla = { customVersionName ->
                    onInstall(GameDownloadInfo(gameVersion = key.gameVersion, customVersionName = customVersionName))
                },
                onInstall = { customVersionName ->
                    onInstall(
                        GameDownloadInfo(
                            gameVersion = key.gameVersion,
                            customVersionName = customVersionName,
                            optifine = viewModel.currentAddon.optifineVersion.value,
                            forge = viewModel.currentAddon.forgeVersion.value,
                            neoforge = viewModel.currentAddon.neoforgeVersion.value
                                .takeIf { loaderSupports.isNeoForgeSupports },
                            fabric = viewModel.currentAddon.fabricVersion.value
                                .takeIf { loaderSupports.isFabricSupports },
                            fabricAPI = viewModel.currentAddon.fabricAPIVersion.value
                                .takeIf { loaderSupports.isFabricSupports },
                            legacyFabric = viewModel.currentAddon.legacyFabricVersion.value
                                .takeIf { loaderSupports.isLegacyFabricSupports },
                            legacyFabricAPI = viewModel.currentAddon.legacyFabricAPIVersion.value
                                .takeIf { loaderSupports.isLegacyFabricSupports },
                            quilt = viewModel.currentAddon.quiltVersion.value
                                .takeIf { loaderSupports.isQuiltSupports },
                            quiltAPI = viewModel.currentAddon.quiltAPIVersion.value
                                .takeIf { loaderSupports.isQuiltSupports },
                            cleanroom = viewModel.currentAddon.cleanroomVersion.value
                                .takeIf { loaderSupports.isCleanroomSupports }
                        )
                    )
                }
            )

            AnimatedColumn(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .verticalScrollWithBar(state = rememberScrollState()),
                isVisible = isVisible
            ) { scope ->
                Spacer(Modifier)

                AnimatedItem(scope) { yOffset ->
                    OptiFineList(
                        modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                        currentAddon = viewModel.currentAddon,
                        onValueChanged = { viewModel.refreshIcon() },
                        addonList = viewModel.addonList
                    ) { viewModel.reloadOptiFine() }
                }

                AnimatedItem(scope) { yOffset ->
                    ForgeList(
                        modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                        currentAddon = viewModel.currentAddon,
                        onValueChanged = { viewModel.refreshIcon() },
                        addonList = viewModel.addonList
                    ) { viewModel.reloadForge() }
                }

                if (loaderSupports.isNeoForgeSupports) {
                    AnimatedItem(scope) { yOffset ->
                        NeoForgeList(
                            modifier = Modifier.offset {
                                IntOffset(
                                    x = 0,
                                    y = yOffset.roundToPx()
                                )
                            },
                            currentAddon = viewModel.currentAddon,
                            onValueChanged = { viewModel.refreshIcon() },
                            addonList = viewModel.addonList
                        ) { viewModel.reloadNeoForge() }
                    }
                }

                if (loaderSupports.isCleanroomSupports) {
                    AnimatedItem(scope) { yOffset ->
                        CleanroomList(
                            modifier = Modifier.offset {
                                IntOffset(
                                    x = 0,
                                    y = yOffset.roundToPx()
                                )
                            },
                            currentAddon = viewModel.currentAddon,
                            onValueChanged = { viewModel.refreshIcon() },
                            addonList = viewModel.addonList
                        ) { viewModel.reloadCleanroom() }
                    }
                }

                if (loaderSupports.isFabricSupports) {
                    AnimatedItem(scope) { yOffset ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                        ) {
                            val isFabricAPIWarning =
                                viewModel.currentAddon.fabricVersion.value != null &&
                                        viewModel.currentAddon.fabricAPIState == AddonState.None &&
                                        !viewModel.addonList.fabricAPIList.isNullOrEmpty() &&
                                        viewModel.currentAddon.fabricAPIVersion.value == null

                            AnimatedVisibility(
                                visible = isFabricAPIWarning
                            ) {
                                AddonWarningItem(
                                    modifier = Modifier.padding(bottom = 12.dp),
                                    text = stringResource(
                                        R.string.download_game_addon_warning_api,
                                        ModLoader.FABRIC_API.displayName
                                    )
                                )
                            }

                            FabricList(
                                modifier = Modifier.fillMaxWidth(),
                                currentAddon = viewModel.currentAddon,
                                onValueChanged = { version ->
                                    viewModel.refreshIcon()
                                    //如果用户手动选择了 Fabric
                                    if (version != null) {
                                        //这里将会自动选择最新的 Fabric API
                                        val lastAPIVersion =
                                            viewModel.addonList.fabricAPIList?.firstOrNull()
                                        viewModel.currentAddon.fabricAPIVersion.value = lastAPIVersion
                                    }
                                },
                                addonList = viewModel.addonList
                            ) { viewModel.reloadFabric() }
                        }
                    }

                    AnimatedItem(scope) { yOffset ->
                        FabricAPIList(
                            modifier = Modifier.offset {
                                IntOffset(
                                    x = 0,
                                    y = yOffset.roundToPx()
                                )
                            },
                            currentAddon = viewModel.currentAddon,
                            onValueChanged = { viewModel.refreshIcon() },
                            addonList = viewModel.addonList
                        ) { viewModel.reloadFabricAPI() }
                    }
                }

                if (loaderSupports.isLegacyFabricSupports) {
                    AnimatedItem(scope) { yOffset ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                        ) {
                            val isFabricAPIWarning =
                                viewModel.currentAddon.legacyFabricVersion.value != null &&
                                        viewModel.currentAddon.legacyFabricAPIState == AddonState.None &&
                                        !viewModel.addonList.legacyFabricAPIList.isNullOrEmpty() &&
                                        viewModel.currentAddon.legacyFabricAPIVersion.value == null

                            AnimatedVisibility(
                                visible = isFabricAPIWarning
                            ) {
                                AddonWarningItem(
                                    modifier = Modifier.padding(bottom = 12.dp),
                                    text = stringResource(
                                        R.string.download_game_addon_warning_api,
                                        ModLoader.LEGACY_FABRIC_API.displayName
                                    )
                                )
                            }

                            LegacyFabricList(
                                modifier = Modifier.fillMaxWidth(),
                                currentAddon = viewModel.currentAddon,
                                onValueChanged = { version ->
                                    viewModel.refreshIcon()
                                    //如果用户手动选择了 Legacy Fabric
                                    if (version != null) {
                                        //这里将会自动选择最新的 Legacy Fabric API
                                        val lastAPIVersion =
                                            viewModel.addonList.legacyFabricAPIList?.firstOrNull()
                                        viewModel.currentAddon.legacyFabricAPIVersion.value = lastAPIVersion
                                    }
                                },
                                addonList = viewModel.addonList
                            ) { viewModel.reloadLegacyFabric() }
                        }
                    }

                    AnimatedItem(scope) { yOffset ->
                        LegacyFabricAPIList(
                            modifier = Modifier.offset {
                                IntOffset(
                                    x = 0,
                                    y = yOffset.roundToPx()
                                )
                            },
                            currentAddon = viewModel.currentAddon,
                            onValueChanged = { viewModel.refreshIcon() },
                            addonList = viewModel.addonList
                        ) { viewModel.reloadLegacyFabricAPI() }
                    }
                }

                if (loaderSupports.isQuiltSupports) {
                    AnimatedItem(scope) { yOffset ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                        ) {
                            val isQuiltAPIWarning =
                                viewModel.currentAddon.quiltVersion.value != null &&
                                        viewModel.currentAddon.quiltAPIState == AddonState.None &&
                                        !viewModel.addonList.quiltAPIList.isNullOrEmpty() &&
                                        viewModel.currentAddon.quiltAPIVersion.value == null

                            AnimatedVisibility(
                                visible = isQuiltAPIWarning
                            ) {
                                AddonWarningItem(
                                    modifier = Modifier.padding(bottom = 12.dp),
                                    text = stringResource(
                                        R.string.download_game_addon_warning_api,
                                        ModLoader.QUILT_API.displayName
                                    )
                                )
                            }

                            QuiltList(
                                modifier = Modifier.fillMaxWidth(),
                                currentAddon = viewModel.currentAddon,
                                onValueChanged = { version ->
                                    viewModel.refreshIcon()
                                    //如果用户手动选择了 Quilt
                                    if (version != null) {
                                        //这里将会自动选择最新的 Quilted Fabric API
                                        val lastAPIVersion =
                                            viewModel.addonList.quiltAPIList?.firstOrNull()
                                        viewModel.currentAddon.quiltAPIVersion.value = lastAPIVersion
                                    }
                                },
                                addonList = viewModel.addonList
                            ) { viewModel.reloadQuilt() }
                        }
                    }

                    AnimatedItem(scope) { yOffset ->
                        QuiltAPIList(
                            modifier = Modifier.offset {
                                IntOffset(
                                    x = 0,
                                    y = yOffset.roundToPx()
                                )
                            },
                            currentAddon = viewModel.currentAddon,
                            onValueChanged = { viewModel.refreshIcon() },
                            addonList = viewModel.addonList
                        ) { viewModel.reloadQuiltAPI() }
                    }
                }

                Spacer(Modifier)
            }
        }
    }
}

private data class UpdateArtwork(val drawable: Int, val theme: String, val release: String)

private fun artworkFor(version: String): UpdateArtwork? {
    val minor = version.split('.').getOrNull(1)?.toIntOrNull() ?: return null
    if (!version.startsWith("1.")) return null
    return when (minor) {
        21 -> UpdateArtwork(R.drawable.update_121, "Tricky Trials", "13 de junho de 2024")
        20 -> UpdateArtwork(R.drawable.update_120, "Trails & Tales", "7 de junho de 2023")
        19 -> UpdateArtwork(R.drawable.update_119, "The Wild Update", "7 de junho de 2022")
        18 -> UpdateArtwork(R.drawable.update_118, "Caves & Cliffs: Part II", "30 de novembro de 2021")
        17 -> UpdateArtwork(R.drawable.update_117, "Caves & Cliffs: Part I", "8 de junho de 2021")
        15 -> UpdateArtwork(R.drawable.update_115, "Buzzy Bees", "10 de dezembro de 2019")
        16 -> UpdateArtwork(R.drawable.update_116, "Nether Update", "23 de junho de 2020")
        14 -> UpdateArtwork(R.drawable.update_114, "Village & Pillage", "23 de abril de 2019")
        12 -> UpdateArtwork(R.drawable.update_112, "World of Color Update", "7 de junho de 2017")
        11 -> UpdateArtwork(R.drawable.update_111, "Exploration Update", "14 de novembro de 2016")
        9 -> UpdateArtwork(R.drawable.update_19, "Combat Update", "29 de fevereiro de 2016")
        8 -> UpdateArtwork(R.drawable.update_18, "Bountiful Update", "2 de setembro de 2014")
        else -> null
    }
}

@Composable
private fun VersionArtworkCard(version: String) {
    val artwork = artworkFor(version)
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier.fillMaxWidth().height(180.dp).clip(shape).background(cardColor())
    ) {
        if (artwork != null) {
            Image(
                painter = painterResource(artwork.drawable),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.78f), Color.Transparent))
            )
        )
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
            Text("Minecraft $version", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            if (artwork != null) {
                Text(artwork.theme, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Text("Lançado em ${artwork.release}", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ScreenHeader(
    modifier: Modifier = Modifier,
    itemContainerColor: Color,
    itemContentColor: Color,
    gameVersion: String,
    currentAddon: CurrentAddon,
    refreshIcon: Any? = null,
    refreshErrorCheck: Any? = null,
    onVanilla: (String) -> Unit = {},
    onInstall: (String) -> Unit = {}
) {
    Column(modifier = modifier) {
        VersionArtworkCard(gameVersion)
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.width(8.dp))

            VersionIconPreview(
                modifier = Modifier.size(28.dp),
                currentAddon = currentAddon,
                refreshIcon = refreshIcon
            )

            var nameValue by remember { mutableStateOf(gameVersion) }
            //用户是否对版本名称进行过编辑
            var editedByUser by remember { mutableStateOf(false) }

            AutoChangeVersionName(
                gameVersion = gameVersion,
                currentAddon = currentAddon,
                editedByUser = editedByUser,
                changeValue = {
                    nameValue = it
                }
            )

            val emptyError = stringResource(R.string.generic_cannot_empty)
            val existsError = stringResource(R.string.versions_manage_install_exists)

            val filenameInvalidMessage = key(nameValue) {
                isFilenameInvalid(nameValue)
            }
            //目标版本已存在时，阻止安装
            val isVersionExists = remember(nameValue, refreshErrorCheck) {
                isVersionExists(nameValue, true)
            }

            val isError = remember(nameValue, refreshErrorCheck) {
                nameValue.isEmpty() || filenameInvalidMessage != null || isVersionExists
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
                    .animateContentSize(animationSpec = getAnimateTween())
            ) {
                SimpleTextInputField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 4.dp),
                    value = nameValue,
                    onValueChange = {
                        nameValue = it
                        if (!editedByUser) {
                            //用户已经对版本名称进行了编辑
                            editedByUser = true
                        }
                    },
                    color = itemContainerColor,
                    contentColor = itemContentColor,
                    singleLine = true,
                    hint = {
                        Text(
                            text = stringResource(R.string.download_game_version_name),
                            style = TextStyle(color = itemContentColor).copy(fontSize = 12.sp)
                        )
                    }
                )

                if (isError) {
                    val message = filenameInvalidMessage
                        ?: (if (isVersionExists) existsError else emptyError)

                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            IconButton(
                onClick = {
                    if (!isError) {
                        onInstall(nameValue)
                    }
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_download_2_filled),
                    contentDescription = stringResource(R.string.download_install)
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(itemContainerColor)
                .border(0.6.dp, Color.White.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                .clickable(enabled = !isError) { onVanilla(nameValue) }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.img_minecraft),
                    contentDescription = null,
                    modifier = Modifier.size(34.dp)
                )
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("Vanilla", fontWeight = FontWeight.SemiBold, color = itemContentColor)
                    Text("Instalar Minecraft sem mods", color = itemContentColor.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun VersionIconPreview(
    modifier: Modifier = Modifier,
    currentAddon: CurrentAddon,
    refreshIcon: Any? = null
) {
    val iconRes = remember(refreshIcon) {
        when {
            currentAddon.optifineVersion.value != null && currentAddon.forgeVersion.value != null -> R.drawable.img_anvil //OptiFine & Forge 同时选择
            currentAddon.optifineVersion.value != null -> R.drawable.img_loader_optifine
            currentAddon.forgeVersion.value != null -> R.drawable.img_anvil
            currentAddon.neoforgeVersion.value != null -> R.drawable.img_loader_neoforge
            currentAddon.fabricVersion.value != null -> R.drawable.img_loader_fabric
            currentAddon.legacyFabricVersion.value != null -> R.drawable.img_loader_legacy_fabric
            currentAddon.quiltVersion.value != null -> R.drawable.img_loader_quilt
            currentAddon.cleanroomVersion.value != null -> R.drawable.img_loader_cleanroom
            else -> R.drawable.img_minecraft
        }
    }

    Image(
        modifier = modifier,
        painter = painterResource(id = iconRes),
        contentDescription = null
    )
}

/**
 * 根据当前已选择的Addon，自动修改版本名称
 * @param editedByUser 版本名称是否已被用户修改，如果用户已经修改过版本名称，则阻止自动修改
 */
@Composable
private fun AutoChangeVersionName(
    gameVersion: String,
    currentAddon: CurrentAddon,
    editedByUser: Boolean,
    changeValue: (String) -> Unit = {}
) {
    LaunchedEffect(
        currentAddon.optifineVersion.value,
        currentAddon.forgeVersion.value,
        currentAddon.neoforgeVersion.value,
        currentAddon.fabricVersion.value,
        currentAddon.legacyFabricVersion.value,
        currentAddon.quiltVersion.value,
        currentAddon.cleanroomVersion.value,
    ) {
        if (editedByUser) return@LaunchedEffect

        fun formatModloader(name: String, version: String) = "$name $version"
        fun formatOptiFine(optifine: OptiFineVersion) = formatModloader(ModLoader.OPTIFINE.displayName, optifine.realVersion)
        fun formatForge(forge: ForgeVersion) = formatModloader(ModLoader.FORGE.displayName, forge.versionName)

        val modloaderValue = buildString {
            with(currentAddon) {
                when {
                    optifineVersion.value != null && forgeVersion.value != null -> {
                        append(formatForge(forgeVersion.value!!))
                        append('-')
                        append(formatOptiFine(optifineVersion.value!!))
                    }
                    optifineVersion.value != null -> append(formatOptiFine(optifineVersion.value!!))
                    forgeVersion.value != null -> append(formatForge(forgeVersion.value!!))
                    neoforgeVersion.value != null -> append(formatModloader(ModLoader.NEOFORGE.displayName, neoforgeVersion.value!!.versionName))
                    fabricVersion.value != null -> append(formatModloader(ModLoader.FABRIC.displayName, fabricVersion.value!!.version))
                    legacyFabricVersion.value != null -> append(formatModloader(ModLoader.LEGACY_FABRIC.displayName, legacyFabricVersion.value!!.version))
                    quiltVersion.value != null -> append(formatModloader(ModLoader.QUILT.displayName, quiltVersion.value!!.version))
                    cleanroomVersion.value != null -> append(formatModloader(ModLoader.CLEANROOM.displayName, cleanroomVersion.value!!.version))
                    else -> {
                        changeValue(gameVersion)
                        return@LaunchedEffect
                    }
                }
            }
        }

        changeValue("$gameVersion $modloaderValue")
    }
}