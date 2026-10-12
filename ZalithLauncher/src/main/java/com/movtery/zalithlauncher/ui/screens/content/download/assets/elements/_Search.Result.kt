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

package com.movtery.zalithlauncher.ui.screens.content.download.assets.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.ModrinthSearchResult
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthSide
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.favorites.FavoriteProjectsRepository
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDisplayLabel
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformFilterCode
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchData
import com.movtery.zalithlauncher.game.download.assets.utils.ModTranslations
import com.movtery.zalithlauncher.game.download.assets.utils.getMcmodTitle
import com.movtery.zalithlauncher.game.version.mod.InstalledMod
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.components.SmallOutlinedEditField
import com.movtery.zalithlauncher.ui.screens.content.elements.backgroundGlass
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.formatNumberByLocale
import com.movtery.zalithlauncher.utils.string.isEmptyOrBlank

sealed interface SearchAssetsState {
    data object Searching: SearchAssetsState
    data class Success(val page: AssetsPage): SearchAssetsState
    data class Error(val message: AndroidStringText): SearchAssetsState
}

/**
 * 资源搜索结果展示列表
 * @param swapToDownload 跳转到下载详情页
 * @param installedInfo 查询项目本地是否已安装，键为平台与平台项目ID
 * @param onNavigatePage 导航到指定页面
 */
@Composable
fun ResultListLayout(
    modifier: Modifier = Modifier,
    classes: PlatformClasses,
    searchState: SearchAssetsState,
    controllerHeight: Dp = 54.dp,
    controllerMinScale: Float = 0.9f,
    controllerMinAlpha: Float = 0.8f,
    onReload: () -> Unit = {},
    onPreviousPage: (pageNumber: Int) -> Unit,
    onNextPage: (pageNumber: Int, isLastPage: Boolean) -> Unit,
    onNavigatePage: (Int) -> Unit,
    swapToDownload: (Platform, projectId: String, iconUrl: String?) -> Unit = { _, _, _ -> },
    installedInfo: ((Platform, projectId: String) -> InstalledMod?)? = null
) {
    when (searchState) {
        is SearchAssetsState.Searching -> {
            Box(modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                LinearProgressIndicator(modifier = Modifier.width(168.dp))
            }
        }
        is SearchAssetsState.Success -> {
            val page = searchState.page
            val listState = rememberLazyListState()
            LaunchedEffect(page.pageNumber) { listState.scrollToItem(0) }
            Column(modifier) {
                if (page.data.isEmpty()) {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("Nenhum resultado. Tente outro nome ou ajuste os filtros.", color = DownloadCatalogColors.secondary, modifier = Modifier.padding(20.dp))
                    }
                } else {
                    ResultList(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        state = listState,
                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 10.dp),
                        classes = classes,
                        data = page.data,
                        swapToDownload = swapToDownload,
                        installedInfo = installedInfo
                    )
                }
                if (page.data.isNotEmpty()) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        PageController(
                            page = page,
                            color = DownloadCatalogColors.card,
                            contentColor = DownloadCatalogColors.text,
                            influencedByBackground = false,
                            onPreviousPage = { onPreviousPage(page.pageNumber) },
                            onNextPage = { onNextPage(page.pageNumber, page.isLastPage) },
                            onNavigatePage = onNavigatePage
                        )
                    }
                }
            }
        }
        is SearchAssetsState.Error -> {
            Box(modifier.padding(12.dp)) {
                ScalingLabel(
                    modifier = Modifier.align(Alignment.Center),
                    text = { AndroidStringText(text = androidText(R.string.download_assets_failed_to_get_result, searchState.message)) },
                    onClick = onReload
                )
            }
        }
    }
}

@Composable
private fun PageController(
    modifier: Modifier = Modifier,
    page: AssetsPage,
    shape: Shape = MaterialTheme.shapes.large,
    influencedByBackground: Boolean = true,
    color: Color = cardColor(influencedByBackground),
    contentColor: Color = onCardColor(),
    blur: Int = AllSettings.backgroundBlur.state,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onNavigatePage: (Int) -> Unit
) {
    var editPageNumber by remember {
        mutableStateOf(false)
    }

    @Composable
    fun PageNumber(modifier: Modifier = Modifier) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.CenterStart
        ) {
            AnimatedVisibility(
                visible = editPageNumber,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut(),
            ) {
                var number by remember { mutableIntStateOf(page.pageNumber) }
                var numberText by remember { mutableStateOf("${page.pageNumber}") }
                //编辑页码
                SmallOutlinedEditField(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 2.dp)
                        .padding(start = 2.dp, end = 8.dp)
                        .width(72.dp),
                    value = numberText,
                    onValueChange = onValueChange@ { value ->
                        if (page.totalPage <= 0) return@onValueChange
                        val number0 = if (value.isEmptyOrBlank()) {
                            1 //为了编辑体验，留空时视为1
                        } else {
                            value.toIntOrNull() ?: return@onValueChange
                        }
                        number = number0.coerceIn(1, page.totalPage)
                        numberText = if (value.isEmptyOrBlank()) value else number.toString()
                    },
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (number != page.pageNumber) {
                                onNavigatePage(number)
                            }
                            editPageNumber = false
                        }
                    ),
                    singleLine = true
                )
            }

            //页码
            AnimatedVisibility(
                visible = !editPageNumber,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut(),
            ) {
                Text(
                    modifier = Modifier.padding(start = 16.dp),
                    text = "${page.pageNumber} ",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }

    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier
                .backgroundGlass(blur, color, influencedByBackground)
                .padding(all = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clickable(enabled = !editPageNumber && page.totalPage > 0) {
                        editPageNumber = true
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                PageNumber(
                    modifier = Modifier.fillMaxHeight()
                )

                Text(
                    modifier = Modifier.padding(end = 16.dp),
                    text = "/ ${page.totalPage}",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            IconButton(
                enabled = page.pageNumber > 1, //不是第一页
                onClick = {
                    onPreviousPage()
                    editPageNumber = false
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_left_rounded),
                    contentDescription = stringResource(R.string.download_assets_result_previous_page)
                )
            }

            IconButton(
                enabled = !page.isLastPage, //不是最后一页
                onClick = {
                    onNextPage()
                    editPageNumber = false
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_right_rounded),
                    contentDescription = stringResource(R.string.download_assets_result_next_page)
                )
            }
        }
    }
}

@Composable
private fun ResultList(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(),
    classes: PlatformClasses,
    data: List<Pair<PlatformSearchData, ModTranslations.McMod?>>,
    swapToDownload: (Platform, projectId: String, iconUrl: String?) -> Unit = { _, _, _ -> },
    installedInfo: ((Platform, projectId: String) -> InstalledMod?)? = null
) {
    val context = LocalContext.current
    BoxWithConstraints(modifier) {
        // Two columns on landscape phones/tablets; one column if the content becomes narrow.
        val columns = if (maxWidth >= 600.dp) 2 else 1
        val rows = remember(data, columns) { data.chunked(columns) }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = state,
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(rows, key = { index, row -> "${row.first().first.platform()}:${row.first().first.platformId()}:$index" }) { _, row ->
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { (item, mcmod) ->
                        val platform = item.platform()
                        val projectId = item.platformId()
                        val iconUrl = item.platformIconUrl()
                        val side = (item as? ModrinthSearchResult.ModrinthProject)?.clientSide
                        val clientSupported = side == ModrinthSide.REQUIRED || side == ModrinthSide.OPTIONAL
                        ResultProjectLayout(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            platform = platform,
                            title = mcmod.getMcmodTitle(item.platformTitle(), context),
                            description = item.platformDescription(),
                            iconUrl = iconUrl,
                            author = item.platformAuthor(),
                            downloads = item.platformDownloadCount(),
                            modloaders = item.platformModLoaders(),
                            categories = item.platformCategories(classes)?.sortedBy { it.index() },
                            isInstalled = installedInfo?.invoke(platform, projectId) != null,
                            isFavorite = FavoriteProjectsRepository.isFavorite(platform, projectId),
                            follows = item.platformFollows(),
                            clientSupported = clientSupported,
                            onFavoriteClick = { FavoriteProjectsRepository.toggle(item, classes) },
                            onClick = { swapToDownload(platform, projectId, iconUrl) }
                        )
                    }
                    if (row.size < columns) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ResultProjectLayout(
    modifier: Modifier = Modifier,
    platform: Platform,
    title: String,
    description: String,
    classes: PlatformClasses? = null,
    iconUrl: String? = null,
    author: String? = null,
    downloads: Long = 0L,
    modloaders: List<PlatformDisplayLabel>? = null,
    categories: List<PlatformFilterCode>? = null,
    isInstalled: Boolean = false,
    isFavorite: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(12.dp),
    influencedByBackground: Boolean = true,
    color: Color = DownloadCatalogColors.card,
    contentColor: Color = DownloadCatalogColors.text,
    blur: Int = AllSettings.backgroundBlur.state,
    follows: Long? = null,
    clientSupported: Boolean = false,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    Column(
        modifier.downloadCatalogGlow(12.dp, 0.95f)
            .clip(shape).background(color).clickable(onClick = onClick)
            .padding(12.dp).heightIn(min = 134.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
            AssetsIcon(modifier = Modifier.clip(RoundedCornerShape(9.dp)), size = 50.dp, iconUrl = iconUrl)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, color = contentColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (!author.isNullOrBlank()) {
                    Text("por $author", color = contentColor.copy(alpha = 0.72f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            OutlinedButton(
                onClick = onClick,
                modifier = Modifier.heightIn(min = 38.dp).downloadCatalogGlow(9.dp, 0.35f),
                shape = RoundedCornerShape(9.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                border = BorderStroke(0.6.dp, Color.White.copy(alpha = 0.65f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor, containerColor = Color.White.copy(alpha = 0.025f))
            ) {
                Icon(painterResource(if (isInstalled) R.drawable.ic_check else R.drawable.ic_download_2_outlined), contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(5.dp))
                Text(if (isInstalled) "Instalado" else "Instalar", fontSize = 11.sp, maxLines = 1)
            }
        }
        Text(
            description,
            Modifier.fillMaxWidth().weight(1f).padding(start = 60.dp),
            color = contentColor.copy(alpha = 0.82f), fontSize = 12.sp, lineHeight = 17.sp,
            minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis
        )
        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(Modifier.heightIn(min = 26.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(painterResource(R.drawable.ic_download_2_outlined), contentDescription = "Downloads", modifier = Modifier.size(15.dp), tint = contentColor)
                Text(formatNumberByLocale(context, downloads), color = contentColor.copy(alpha = 0.85f), fontSize = 11.sp)
            }
            Row(
                Modifier.clip(RoundedCornerShape(6.dp))
                    .then(if (onFavoriteClick != null) Modifier.clickable(onClickLabel = if (isFavorite) "Remover dos favoritos" else "Adicionar aos favoritos", onClick = onFavoriteClick) else Modifier)
                    .heightIn(min = 26.dp).padding(horizontal = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    painterResource(if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outlined),
                    contentDescription = if (isFavorite) "Favorito" else "Favoritar",
                    modifier = Modifier.size(15.dp), tint = if (isFavorite) Color(0xFFFFAAC5) else contentColor
                )
                if (follows != null) Text(formatNumberByLocale(context, follows), color = contentColor.copy(alpha = 0.85f), fontSize = 11.sp)
            }
            if (clientSupported) CatalogTag("Client", DownloadCatalogColors.blue)
            modloaders?.take(2)?.forEach { CatalogTag(it.getDisplayName(), Color(0xFF343B45)) }
            if (!clientSupported && modloaders.isNullOrEmpty()) {
                if (classes != null) ClassesIdentifier(classes = classes)
                else categories?.firstOrNull()?.let { CatalogTag(stringResource(it.getDisplayName()), DownloadCatalogColors.blue) }
            }
        }
    }
}

@Composable
private fun CatalogTag(text: String, color: Color) {
    Text(
        text,
        Modifier.clip(RoundedCornerShape(6.dp)).background(color).padding(horizontal = 8.dp, vertical = 4.dp),
        color = Color.White, fontSize = 10.sp, lineHeight = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun ProjectTitleHead(
    modifier: Modifier = Modifier,
    platform: Platform,
    title: String,
    author: String?,
    classes: PlatformClasses? = null,
    reserveAuthor: Boolean = false
) {
    //标题栏、作者栏、平台标签
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        //标题栏、作者栏
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                modifier = Modifier
                    .weight(0.6f, fill = false)
                    .basicMarquee(iterations = Int.MAX_VALUE),
                text = title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
            )
            author?.let {
                VerticalDivider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                MarqueeText(
                    modifier = Modifier
                        .weight(0.4f, fill = false)
                        .alpha(0.7f),
                    text = stringResource(R.string.download_assets_result_authors, it),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            if (author == null && reserveAuthor) {
                //作者信息缺失时保留占位
                Text(
                    modifier = Modifier
                        .weight(0.4f, fill = false)
                        .alpha(0.7f),
                    text = "",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
        }
        //资源的类别
        classes?.let {
            ClassesIdentifier(classes = it)
        }
        //平台标签
        PlatformIdentifier(platform = platform)
    }
}