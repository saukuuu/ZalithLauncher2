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

import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.movtery.zalithlauncher.ui.screens.content.home.LocalDashboardOpenAccount
import com.movtery.zalithlauncher.ui.screens.content.home.LocalDashboardOpenVersions
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import com.movtery.zalithlauncher.ui.screens.content.home.LauncherDashboardStats
import com.movtery.zalithlauncher.ui.screens.content.home.launcherGlow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.cardgrid.state.rememberCardGridState
import com.movtery.guide.GuideSide
import com.movtery.guide.guideNode
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.getAccountTypeName
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.ActionMenuSide
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.guide.GuideKeys
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.CommonVersionInfoLayout
import com.movtery.zalithlauncher.ui.screens.content.elements.PlayerFace
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionIconImage
import com.movtery.zalithlauncher.ui.screens.content.home.HomeGrid
import com.movtery.zalithlauncher.ui.screens.content.home.LocalActionMenuDrag
import com.movtery.zalithlauncher.ui.screens.content.home.actionMenuDragAnchor
import com.movtery.zalithlauncher.ui.screens.content.home.actionMenuDragExclusion
import com.movtery.zalithlauncher.ui.screens.content.home.rememberActionMenuDragState
import com.movtery.zalithlauncher.ui.screens.content.home.version.LocalHomeCardLauncher
import com.movtery.zalithlauncher.ui.screens.content.home.version.LocalHomeCardVersionSettings
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import kotlin.math.roundToInt

private const val ContentWeight = 7f
private const val ActionMenuWeight = 3f

/**
 * 操作菜单停泊槽位与屏幕边缘的间距
 */
private val ActionMenuOuterPadding = 12.dp

@Composable
fun LauncherScreen(
    backStackViewModel: ScreenBackStackViewModel,
    navigateToVersions: (Version) -> Unit,
    onLaunchGame: (Version?) -> Unit,
    onOpenLink: (String) -> Unit,
    startGuideOnce: (GuideKeys.Keys) -> Unit,
) {
    val dashboardContext = LocalContext.current
    val trackedLaunch: (Version?) -> Unit = { version ->
        val actual = version ?: VersionsManager.currentVersion.value
        LauncherDashboardStats.recordLaunch(dashboardContext, actual?.takeIf { it.isValid() }?.getVersionName())
        onLaunchGame(version)
    }
    LaunchedEffect(Unit) {
        //发起新手引导
        startGuideOnce(GuideKeys.Main)
    }

    BaseScreen(
        screenKey = NormalNavKey.LauncherMain,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { isVisible ->
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        val dockedSide = AllSettings.launcherActionMenuSide.state
        val dragState = rememberActionMenuDragState(
            onCommit = { side -> AllSettings.launcherActionMenuSide.save(side) }
        )
        dragState.dockedSide = dockedSide
        dragState.isRtl = isRtl

        LaunchedEffect(isVisible) {
            //屏幕切走时打断进行中的拖拽
            if (!isVisible) dragState.onDragCancel()
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    dragState.parentOrigin = coordinates.positionInRoot()
                }
        ) {
            val parentWidthPx = constraints.maxWidth.toFloat()
            dragState.parentWidthPx = parentWidthPx
            dragState.outerPaddingPx = with(LocalDensity.current) { ActionMenuOuterPadding.toPx() }
            dragState.menuSpanPx = parentWidthPx * (ActionMenuWeight / (ActionMenuWeight + ContentWeight))

            //拖拽期间以预览侧为准
            val effectiveSide = dragState.previewSide ?: dockedSide

            //卡片尺寸与停泊槽内容区保持一致
            val cardWidth = maxWidth * (ActionMenuWeight / (ActionMenuWeight + ContentWeight)) - ActionMenuOuterPadding
            val cardHeight = maxHeight - ActionMenuOuterPadding * 2

            val toAccountManageScreen: () -> Unit = {
                backStackViewModel.mainScreen.navigateTo(
                    screenKey = NormalNavKey.AccountManager(FirstLoginMenu.NONE)
                )
            }
            val toVersionManageScreen: () -> Unit = {
                backStackViewModel.mainScreen.removeAndNavigateTo(
                    remove = NestedNavKey.VersionSettings::class,
                    screenKey = NormalNavKey.VersionsManager
                )
            }
            val toVersionSettingsScreen: () -> Unit = {
                VersionsManager.currentVersion.value?.let { version ->
                    navigateToVersions(version)
                }
            }

            // 内容区域
            Row(modifier = Modifier.fillMaxSize()) {
                // ActionMenu 对接到了 Start，留出空位
                if (dockedSide == ActionMenuSide.START) {
                    Spacer(modifier = Modifier.weight(ActionMenuWeight))
                }

                CompositionLocalProvider(
                    LocalUriHandler provides object : UriHandler {
                        override fun openUri(uri: String) {
                            onOpenLink(uri)
                        }
                    }
                ) {
                    ContentMenu(
                        modifier = Modifier
                            .guideNode(
                                key = GuideKeys.Main.Step.CardTip,
                                holeRadius = 0.dp
                            )
                            .weight(ContentWeight)
                            .offset { IntOffset(x = dragState.previewShift.value.roundToInt(), y = 0) },
                        isVisible = isVisible,
                        onLaunchGame = trackedLaunch,
                        onOpenVersionSettings = navigateToVersions,
                        onOpenAccount = toAccountManageScreen,
                        onOpenVersions = toVersionManageScreen
                    )
                }

                // ActionMenu 对接到了 End，留出空位
                if (dockedSide == ActionMenuSide.END) {
                    Spacer(modifier = Modifier.weight(ActionMenuWeight))
                }
            }

            CompositionLocalProvider(LocalActionMenuDrag provides dragState) {
                Box(
                    modifier = Modifier
                        .offset {
                            val translation = if (dragState.floating) {
                                dragState.cardPosition
                            } else {
                                dragState.landingOf(effectiveSide) + dragState.settleOffset.value
                            }
                            val x = if (isRtl) parentWidthPx - cardWidth.toPx() - translation.x else translation.x
                            IntOffset(x.roundToInt(), (translation.y - 8.dp.toPx()).roundToInt())
                        }
                        .size(cardWidth, cardHeight)
                ) {
                    ActionMenu(
                        modifier = Modifier.fillMaxSize(),
                        isVisible = isVisible,
                        onLaunchGame = trackedLaunch,
                        swapTargetValue = if (dockedSide == ActionMenuSide.END) 40.dp else (-40).dp,
                        pickUpScale = { dragState.scale },
                        toAccountManageScreen = toAccountManageScreen,
                        toVersionManageScreen = toVersionManageScreen,
                        toVersionSettingsScreen = toVersionSettingsScreen
                    )
                }
            }
        }
    }
}

@Composable
private fun ContentMenu(
    isVisible: Boolean,
    onLaunchGame: (Version) -> Unit,
    onOpenVersionSettings: (Version) -> Unit,
    onOpenAccount: () -> Unit,
    onOpenVersions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val yOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible
    )
    val gridState = rememberCardGridState()

    CompositionLocalProvider(
        LocalHomeCardLauncher provides onLaunchGame,
        LocalHomeCardVersionSettings provides onOpenVersionSettings,
        LocalDashboardOpenAccount provides onOpenAccount,
        LocalDashboardOpenVersions provides onOpenVersions
    ) {
        HomeGrid(
            state = gridState,
            modifier = modifier
                .fillMaxSize()
                .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
        )
    }
}

@Composable
private fun AccountAvatarCenter(
    modifier: Modifier = Modifier,
    account: Account?,
    refreshKey: Any? = null,
    onClick: () -> Unit = {}
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
            .actionMenuDragExclusion()
            .clip(RoundedCornerShape(15.dp))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        val compact = maxHeight < 145.dp
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (compact) 5.dp else 9.dp)
        ) {
            if (account != null) {
                PlayerFace(account = account, avatarSize = if (compact) 40.dp else 56.dp, refreshKey = refreshKey)
            } else {
                Icon(painterResource(R.drawable.ic_add), null, Modifier.size(if (compact) 32.dp else 42.dp), tint = Color.White)
            }
            Text(
                text = account?.username ?: "Adicionar Conta",
                color = Color.White,
                fontSize = if (compact) 15.sp else 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = account?.let { getAccountTypeName(it) }
                    ?: "Adicione sua conta Microsoft\npara jogar Minecraft e acessar\ntodos os seus recursos.",
                color = Color(0xFFD5DCE4),
                fontSize = if (compact) 10.sp else 12.sp,
                lineHeight = if (compact) 14.sp else 17.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun VersionsContent(
    onLaunchGame: (Version?) -> Unit,
    toVersionManageScreen: () -> Unit,
    toVersionSettingsScreen: () -> Unit,
    modifier: Modifier = Modifier,
    imageHeight: Dp = 110.dp,
    playHeight: Dp = 48.dp
) {
    val version by VersionsManager.currentVersion.collectAsStateWithLifecycle()
    val isRefreshing by VersionsManager.isRefreshing.collectAsStateWithLifecycle()
    val versions by VersionsManager.versions.collectAsStateWithLifecycle()
    var showList by remember { mutableStateOf(false) }
    val info = version?.getVersionInfo()
    val versionName = info?.minecraftVersion ?: version?.getVersionName()
    val title = versionName?.let { if (it.startsWith("Minecraft", ignoreCase = true)) it else "Minecraft $it" } ?: "Minecraft"
    val subtitle = when {
        isRefreshing -> "Carregando versões…"
        version == null -> "Selecione uma versão para jogar"
        version?.isValid() != true -> "Verifique os arquivos desta versão"
        version?.isSummaryValid() == true -> version?.getVersionSummary().orEmpty()
        else -> "Java Edition"
    }
    val loader = info?.loaderInfo?.loader?.displayName ?: "Vanilla"
    val shape = RoundedCornerShape(15.dp)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(imageHeight)
                    .launcherGlow(15.dp, 1f)
                    .clip(shape)
                    .actionMenuDragExclusion()
                    .combinedClickable(
                        role = Role.Button,
                        onClick = toVersionManageScreen,
                        onLongClick = { if (versions.isNotEmpty()) showList = true }
                    )
                    .guideNode(GuideKeys.Main.Step.VersionList, preferSide = GuideSide.Above)
            ) {
                Image(
                    painter = painterResource(R.drawable.launcher_update_art),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                )
                Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(
                    Color.Black.copy(alpha = 0.64f), Color.Black.copy(alpha = 0.08f), Color.Black.copy(alpha = 0.64f)
                ))))
                Column(modifier = Modifier.fillMaxSize().padding(9.dp)) {
                    Text(title, color = Color.White, fontSize = 17.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(subtitle, color = Color(0xFFE2E6EA), fontSize = 10.5.sp, lineHeight = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.weight(1f))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Row(
                            modifier = Modifier.weight(1f, fill = false)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xE6212F38))
                                .padding(horizontal = 7.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            VersionIconImage(version = version, modifier = Modifier.size(24.dp))
                            Text(if (version == null) "Selecionar" else loader, color = Color.White, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(
                            modifier = Modifier.size(34.dp).launcherGlow(10.dp, 0.4f)
                                .clip(RoundedCornerShape(10.dp)).background(Color(0xE6212F38)),
                            onClick = { if (version?.isValid() == true) toVersionSettingsScreen() else toVersionManageScreen() }
                        ) {
                            Icon(painterResource(R.drawable.ic_settings_filled), stringResource(R.string.versions_manage_settings), tint = Color.White, modifier = Modifier.size(23.dp))
                        }
                    }
                }
            }
            DropdownMenu(
                expanded = showList,
                onDismissRequest = { showList = false },
                modifier = Modifier.width(260.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                versions.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                CommonVersionInfoLayout(modifier = Modifier.weight(1f), version = item, iconSize = 28.dp)
                                IconButton(onClick = { showList = false; onLaunchGame(item) }) {
                                    Icon(painterResource(R.drawable.ic_play_arrow_filled), stringResource(R.string.main_launch_game), tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        },
                        onClick = {
                            if (version != item) VersionsManager.saveVersion(item)
                            showList = false
                        }
                    )
                }
            }
        }
        Button(
            modifier = Modifier.fillMaxWidth().height(playHeight).launcherGlow(15.dp, 1f),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D85A), contentColor = Color(0xFF041B0D)),
            contentPadding = PaddingValues(0.dp),
            onClick = { onLaunchGame(null) }
        ) {
            Text("Jogar", fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ActionMenuCardContent(
    modifier: Modifier = Modifier,
    account: Account?,
    onLaunchGame: (Version?) -> Unit,
    toAccountManageScreen: () -> Unit,
    toVersionManageScreen: () -> Unit,
    toVersionSettingsScreen: () -> Unit,
) {
    BoxWithConstraints(modifier = modifier) {
        val imageHeight = (maxHeight * 0.30f).coerceIn(82.dp, 165.dp)
        val playHeight = (maxHeight * 0.13f).coerceIn(42.dp, 58.dp)
        Column(
            modifier = Modifier.fillMaxSize().actionMenuDragAnchor().guideNode(GuideKeys.Main.Step.CardDrag),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth()
                    .launcherGlow(15.dp, 1f)
                    .background(Brush.verticalGradient(listOf(Color(0xFF2A363F), Color(0xFF26313B))), RoundedCornerShape(15.dp))
                    .guideNode(GuideKeys.Main.Step.Account, preferSide = GuideSide.Below),
                contentAlignment = Alignment.Center
            ) {
                AccountAvatarCenter(account = account, onClick = toAccountManageScreen)
            }
            VersionsContent(
                modifier = Modifier.fillMaxWidth(),
                onLaunchGame = onLaunchGame,
                toVersionManageScreen = toVersionManageScreen,
                toVersionSettingsScreen = toVersionSettingsScreen,
                imageHeight = imageHeight,
                playHeight = playHeight
            )
        }
    }
}

@Composable
private fun ActionMenu(
    isVisible: Boolean,
    onLaunchGame: (Version?) -> Unit,
    swapTargetValue: Dp,
    pickUpScale: () -> Float,
    modifier: Modifier = Modifier,
    toAccountManageScreen: () -> Unit = {},
    toVersionManageScreen: () -> Unit = {},
    toVersionSettingsScreen: () -> Unit = {}
) {
    val xOffset by swapAnimateDpAsState(
        targetValue = swapTargetValue,
        swapIn = isVisible,
        isHorizontal = true
    )

    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()

    val contentModifier = modifier.graphicsLayer {
        val scale = pickUpScale()
        scaleX = scale
        scaleY = scale
    }.offset { IntOffset(x = xOffset.roundToPx(), y = 0) }

    ActionMenuCardContent(
        modifier = contentModifier,
        account = account,
        onLaunchGame = onLaunchGame,
        toAccountManageScreen = toAccountManageScreen,
        toVersionManageScreen = toVersionManageScreen,
        toVersionSettingsScreen = toVersionSettingsScreen,
    )
}

