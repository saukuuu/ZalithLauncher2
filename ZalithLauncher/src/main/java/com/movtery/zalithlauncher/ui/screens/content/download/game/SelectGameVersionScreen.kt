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

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.nonInteractiveScrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersion
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersions
import com.movtery.zalithlauncher.game.versioninfo.models.isType
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.CheckChip
import com.movtery.zalithlauncher.ui.components.EdgeDirection
import com.movtery.zalithlauncher.ui.components.LittleTextLabel
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.components.SimpleTextInputField
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.backgroundGlass
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.classes.Quadruple
import com.movtery.zalithlauncher.utils.formatDate
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.toLocal
import com.movtery.zalithlauncher.utils.string.isEmptyOrBlank
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException

private const val TAG = "SelectGameVersion"

/** 版本列表加载状态 */
private sealed interface VersionState {
    /** 加载中 */
    data object Loading : VersionState
    /** 加载完成 */
    data class None(val versions: List<MinecraftVersion>) : VersionState
    /** 加载出现异常 */
    data class Failure(val message: AndroidStringText) : VersionState
}

/**
 * 版本过滤条件
 * @param release 是否保留正式版本
 * @param snapshot 是否保留快照版本
 * @param old 是否保留旧版本
 * @param id 搜索并过滤版本ID
 */
private data class VersionFilter(
    val release: Boolean = true,
    val snapshot: Boolean = false,
    val aprilFools: Boolean = false,
    val old: Boolean = false,
    val id: String = ""
)

private class VersionsViewModel : ViewModel() {
    var versionState by mutableStateOf<VersionState>(VersionState.Loading)
        private set

    var versionFilter by mutableStateOf(VersionFilter())
        private set

    fun filterWith(filter: VersionFilter) {
        versionFilter = filter
        viewModelScope.launch {
            val allVersions = MinecraftVersions.allVersions.value
            versionState = VersionState.None(
                versions = allVersions.filterVersions(versionFilter)
            )
        }
    }

    fun refresh(forceReload: Boolean = false) {
        viewModelScope.launch {
            versionState = VersionState.Loading
            versionState = runCatching {
                MinecraftVersions.refreshVersions(forceReload)
                val allVersions = MinecraftVersions.allVersions.value
                VersionState.None(allVersions.filterVersions(versionFilter))
            }.getOrElse { e ->
                Logger.warning(TAG, "Failed to get version manifest!", e)
                val message: AndroidStringText = when (e) {
                    is HttpRequestTimeoutException -> androidText(R.string.error_timeout)
                    is UnknownHostException, is UnresolvedAddressException -> androidText(R.string.error_network_unreachable)
                    is ConnectException -> androidText(R.string.error_connection_failed)
                    is ResponseException -> e.toLocal()
                    else -> {
                        Logger.error(TAG, "An unknown exception was caught!", e)
                        androidText(e.localizedMessage ?: e.message ?: e::class.qualifiedName ?: "Unknown error")
                    }
                }
                VersionState.Failure(message)
            }
        }
    }

    init {
        refresh()
    }

    override fun onCleared() {
        viewModelScope.cancel()
    }
}

/**
 * Host information for the Minecraft version selection screen.
 */
sealed interface SelectGameVersionHost {
    data class Download(
        val mainScreenKey: TitledNavKey?,
        val downloadScreenKey: TitledNavKey?,
        val downloadGameScreenKey: TitledNavKey?
    ) : SelectGameVersionHost

    data class VersionSettings(
        val mainScreenKey: TitledNavKey?,
        val versionSettingsScreenKey: TitledNavKey?
    ) : SelectGameVersionHost
}

@Composable
fun SelectGameVersionScreen(
    host: SelectGameVersionHost,
    eventViewModel: EventViewModel,
    onVersionSelect: (String) -> Unit = {}
) {
    val viewModel = viewModel(
        key = NormalNavKey.DownloadGame.SelectGameVersion.toString()
    ) {
        VersionsViewModel()
    }

    when (host) {
        is SelectGameVersionHost.Download -> BaseScreen(
            levels1 = listOf(
                Pair(NestedNavKey.Download::class.java, host.mainScreenKey),
                Pair(NestedNavKey.DownloadGame::class.java, host.downloadScreenKey)
            ),
            Triple(NormalNavKey.DownloadGame.SelectGameVersion, host.downloadGameScreenKey, false)
        ) { isVisible ->
            SelectGameVersionContent(
                isVisible = isVisible,
                viewModel = viewModel,
                eventViewModel = eventViewModel,
                onVersionSelect = onVersionSelect
            )
        }

        is SelectGameVersionHost.VersionSettings -> BaseScreen(
            levels1 = listOf(
                Pair(NestedNavKey.VersionSettings::class.java, host.mainScreenKey)
            ),
            Triple(NormalNavKey.Versions.ModifyVersion, host.versionSettingsScreenKey, false)
        ) { isVisible ->
            SelectGameVersionContent(
                isVisible = isVisible,
                viewModel = viewModel,
                eventViewModel = eventViewModel,
                onVersionSelect = onVersionSelect
            )
        }
    }
}

@Composable
private fun SelectGameVersionContent(
    isVisible: Boolean,
    viewModel: VersionsViewModel,
    eventViewModel: EventViewModel,
    onVersionSelect: (String) -> Unit
) {
    val yOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
    ) {
        when (val state = viewModel.versionState) {
            is VersionState.Loading -> {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LinearWavyProgressIndicator(
                        modifier = Modifier.width(168.dp),
                        wavelength = 32.dp
                    )
                }
            }

            is VersionState.Failure -> {
                Box(Modifier.fillMaxSize()) {
                    ScalingLabel(
                        modifier = Modifier.align(Alignment.Center),
                        text = {
                            AndroidStringText(
                                text = androidText(
                                    R.string.download_game_failed_to_get_versions,
                                    state.message
                                )
                            )
                        },
                        onClick = {
                            viewModel.refresh(true)
                        }
                    )
                }
            }

            is VersionState.None -> {
                Column {
                    VersionHeader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        versionFilter = viewModel.versionFilter,
                        onVersionFilterChange = { viewModel.filterWith(it) },
                        itemContainerColor = cardColor(),
                        itemContentColor = onCardColor(),
                        onRefreshClick = {
                            viewModel.refresh(true)
                        }
                    )

                    VersionList(
                        modifier = Modifier.weight(1f),
                        versions = state.versions,
                        onVersionSelect = onVersionSelect,
                        openLink = { url ->
                            eventViewModel.sendEvent(EventViewModel.Event.OpenLink(url))
                        }
                    )
                }
            }
        }
    }
}

/**
 * 简易过滤器，过滤特定类型的版本
 */
private fun List<MinecraftVersion>.filterVersions(
    versionFilter: VersionFilter
) = this.filter { version ->
    version.isType(
        release = versionFilter.release,
        snapshot = versionFilter.snapshot,
        aprilFools = versionFilter.aprilFools,
        old = versionFilter.old
    )
}.filter { version ->
    //Fix：单独过滤版本名称
    val versionId = versionFilter.id
    versionId.isEmptyOrBlank() || version.version.id.contains(versionId)
}

@Composable
private fun VersionHeader(
    modifier: Modifier = Modifier,
    versionFilter: VersionFilter,
    onVersionFilterChange: (VersionFilter) -> Unit,
    itemContainerColor: Color,
    itemContentColor: Color,
    onRefreshClick: () -> Unit = {}
) {
    Column(modifier = modifier) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fadeEdge(
                            state = scrollState,
                            direction = EdgeDirection.Horizontal
                        )
                        .widthIn(max = this@BoxWithConstraints.maxWidth / 5 * 3) //3/5
                        .horizontalScroll(scrollState),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    //版本筛选条件
                    VersionTypeItem(
                        selected = versionFilter.release,
                        onClick = {
                            onVersionFilterChange(versionFilter.copy(release = versionFilter.release.not()))
                        },
                        text = stringResource(R.string.download_game_type_release)
                    )
                    VersionTypeItem(
                        selected = versionFilter.snapshot,
                        onClick = {
                            onVersionFilterChange(versionFilter.copy(snapshot = versionFilter.snapshot.not()))
                        },
                        text = stringResource(R.string.download_game_type_snapshot)
                    )
                    VersionTypeItem(
                        selected = versionFilter.aprilFools,
                        onClick = {
                            onVersionFilterChange(versionFilter.copy(aprilFools = versionFilter.aprilFools.not()))
                        },
                        text = stringResource(R.string.download_game_type_april_fools)
                    )
                    VersionTypeItem(
                        selected = versionFilter.old,
                        onClick = {
                            onVersionFilterChange(versionFilter.copy(old = versionFilter.old.not()))
                        },
                        text = stringResource(R.string.download_game_type_old)
                    )
                }

                //搜索、刷新
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModernSearchField(
                        modifier = Modifier.weight(1f),
                        value = versionFilter.id,
                        onValueChange = { onVersionFilterChange(versionFilter.copy(id = it)) },
                        containerColor = itemContainerColor,
                        contentColor = itemContentColor
                    )

                    IconButton(
                        onClick = onRefreshClick
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_refresh),
                            contentDescription = stringResource(R.string.generic_refresh)
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun ModernSearchField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    containerColor: Color,
    contentColor: Color
) {
    val shape = MaterialTheme.shapes.large
    val glow = Color.White.copy(alpha = 0.22f)

    Surface(
        modifier = modifier
            .shadow(
                elevation = 7.dp,
                shape = shape,
                ambientColor = glow,
                spotColor = glow
            )
            .border(1.dp, Color.White.copy(alpha = 0.16f), shape),
        shape = shape,
        color = containerColor.copy(alpha = 0.96f),
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            SearchIcon(
                modifier = Modifier.size(18.dp),
                color = contentColor.copy(alpha = 0.88f)
            )

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = contentColor),
                cursorBrush = SolidColor(contentColor),
                decorationBox = { innerTextField ->
                    Box {
                        if (value.isEmpty()) {
                            Text(
                                text = stringResource(R.string.generic_search),
                                style = MaterialTheme.typography.bodyMedium,
                                color = contentColor.copy(alpha = 0.55f)
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

@Composable
private fun SearchIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val stroke = size.minDimension * 0.11f
        val radius = size.minDimension * 0.28f
        val center = Offset(size.width * 0.42f, size.height * 0.42f)
        drawCircle(
            color = color,
            radius = radius,
            center = center,
            style = Stroke(width = stroke)
        )
        drawLine(
            color = color,
            start = Offset(center.x + radius * 0.72f, center.y + radius * 0.72f),
            end = Offset(size.width * 0.88f, size.height * 0.88f),
            strokeWidth = stroke
        )
    }
}

@Composable
private fun VersionTypeItem(
    selected: Boolean,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CheckChip(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        label = {
            Text(text)
        },
    )
}

@Composable
private fun VersionList(
    modifier: Modifier = Modifier,
    versions: List<MinecraftVersion>,
    onVersionSelect: (String) -> Unit,
    openLink: (url: String) -> Unit
) {
    val gridState = rememberLazyGridState()
    val groupedVersions = remember(versions) {
        versions.groupBy { version ->
            if (version.type == MinecraftVersion.Type.Release) {
                releaseFamily(version.version.id)
            } else {
                version.version.id
            }
        }.toList()
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 205.dp),
        modifier = modifier.nonInteractiveScrollbar(
            state = gridState.scrollIndicatorState!!,
            orientation = Orientation.Vertical
        ),
        state = gridState,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(groupedVersions, key = { it.first }) { (family, familyVersions) ->
            VersionFamilyCard(
                family = family,
                versions = familyVersions,
                onVersionSelect = onVersionSelect,
                openLink = openLink
            )
        }
    }
}

/** Turns 1.17.1 into 1.17, so patch releases share one Minecraft update card. */
private fun releaseFamily(versionId: String): String {
    val parts = versionId.split('.')
    return if (parts.size >= 2 && parts[0].all(Char::isDigit) && parts[1].all(Char::isDigit)) {
        "${parts[0]}.${parts[1]}"
    } else versionId
}

@Composable
private fun VersionFamilyCard(
    modifier: Modifier = Modifier,
    family: String,
    versions: List<MinecraftVersion>,
    onVersionSelect: (String) -> Unit,
    openLink: (String) -> Unit,
    shape: Shape = MaterialTheme.shapes.large,
    influencedByBackground: Boolean = true,
    color: Color = cardColor(influencedByBackground),
    contentColor: Color = onCardColor(),
    blur: Int = AllSettings.backgroundBlur.state,
) {
    val newest = versions.maxByOrNull { it.version.releaseTime } ?: return
    var selectedId by remember(versions) { mutableStateOf(newest.version.id) }
    var menuOpen by remember { mutableStateOf(false) }
    val (icon, versionType, wikiUrl, _) = getVersionComponents(newest)
    val scale = remember { Animatable(initialValue = 0.96f) }
    val glowColor = updateGlowColor(family)

    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = getAnimateTween())
    }

    Surface(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = shape,
                ambientColor = glowColor.copy(alpha = 0.55f),
                spotColor = glowColor.copy(alpha = 0.70f)
            )
            .graphicsLayer(scaleX = scale.value, scaleY = scale.value),
        shape = shape,
        color = color,
        contentColor = contentColor
    ) {
        Column(
            modifier = Modifier
                .clip(shape)
                .backgroundGlass(blur, color, influencedByBackground)
        ) {
            // Large update-art area. For now it uses the launcher's built-in artwork.
            // Later each family can point to its own drawable (1.17, 1.18, etc.).
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(108.dp),
                contentAlignment = Alignment.Center
            ) {
                icon?.let {
                    Image(
                        painter = it,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                wikiUrl?.let { url ->
                    IconButton(
                        modifier = Modifier.align(Alignment.TopEnd),
                        onClick = { openLink(url) }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_link),
                            contentDescription = "Wiki"
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = if (newest.type == MinecraftVersion.Type.Release) "Minecraft $family" else newest.version.id,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                LittleTextLabel(text = versionType)

                Text(
                    modifier = Modifier.alpha(0.72f),
                    text = formatDate(
                        input = newest.version.releaseTime,
                        pattern = stringResource(R.string.date_format)
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        Surface(
                            onClick = { menuOpen = !menuOpen },
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.img_minecraft),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(MaterialTheme.shapes.extraSmall),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(7.dp))
                                Text(
                                    text = selectedId,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(if (menuOpen) "▲" else "▼", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Button(
                        onClick = { onVersionSelect(selectedId) },
                        shape = MaterialTheme.shapes.medium,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF21A83B),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Selecionar", maxLines = 1)
                    }
                }

                if (menuOpen && versions.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        versions.sortedByDescending { it.version.releaseTime }.forEach { item ->
                            Surface(
                                onClick = {
                                    selectedId = item.version.id
                                    menuOpen = false
                                },
                                shape = MaterialTheme.shapes.small,
                                color = if (item.version.id == selectedId)
                                    MaterialTheme.colorScheme.secondaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.img_minecraft),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(17.dp)
                                            .clip(MaterialTheme.shapes.extraSmall),
                                        contentScale = ContentScale.Crop
                                    )
                                    Text(
                                        text = item.version.id,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun updateGlowColor(family: String): Color = when (family) {
    "26.3" -> Color(0xFFFF7043)
    "26.2" -> Color(0xFFA5D66A)
    "26.1" -> Color(0xFFFFD54F)
    "1.21" -> Color(0xFFFFB74D)
    "1.20" -> Color(0xFFE879B9)
    "1.19" -> Color(0xFF66BB6A)
    "1.18" -> Color(0xFF64B5F6)
    "1.17" -> Color(0xFFB39DDB)
    "1.16" -> Color(0xFFFF7043)
    "1.15" -> Color(0xFFFFD54F)
    "1.14" -> Color(0xFFFFB74D)
    "1.13" -> Color(0xFF29B6F6)
    "1.12" -> Color(0xFFCE93D8)
    "1.11" -> Color(0xFF81C784)
    "1.10" -> Color(0xFFBCAAA4)
    "1.9" -> Color(0xFF9575CD)
    "1.6" -> Color(0xFF8BC34A)
    else -> Color.White.copy(alpha = 0.75f)
}

@Composable
private fun getVersionComponents(
    version: MinecraftVersion
): Quadruple<Painter?, String, String?, String?> {
    val vmVer = version.version
    val summary = version.summary?.let { stringResource(it) }
    val urlSuffix = version.urlSuffix ?: vmVer.id

    return when (version.type) {
        MinecraftVersion.Type.Release -> {
            Quadruple(
                painterResource(R.drawable.img_minecraft),
                stringResource(R.string.download_game_type_release),
                stringResource(R.string.url_wiki_minecraft_game_release, urlSuffix),
                summary
            )
        }
        MinecraftVersion.Type.Snapshot -> {
            Quadruple(
                painterResource(R.drawable.img_command_block),
                stringResource(R.string.download_game_type_snapshot),
                stringResource(R.string.url_wiki_minecraft_game_snapshot, urlSuffix),
                summary
            )
        }
        MinecraftVersion.Type.AprilFools -> {
            Quadruple(
                painterResource(R.drawable.img_diamond_block),
                stringResource(R.string.download_game_type_april_fools),
                stringResource(R.string.url_wiki_minecraft_game_snapshot, urlSuffix),
                summary
            )
        }
        MinecraftVersion.Type.OldBeta -> {
            Quadruple(
                painterResource(R.drawable.img_old_cobblestone),
                stringResource(R.string.download_game_type_old_beta),
                null,
                summary
            )
        }
        MinecraftVersion.Type.OldAlpha -> {
            Quadruple(
                painterResource(R.drawable.img_old_grass_block),
                stringResource(R.string.download_game_type_old_alpha),
                null,
                summary
            )
        }
        else -> {
            Quadruple(
                null,
                stringResource(R.string.generic_unknown),
                null,
                version.summary?.let { stringResource(it) }
            )
        }
    }
}