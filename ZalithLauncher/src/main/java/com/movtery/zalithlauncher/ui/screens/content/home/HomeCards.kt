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

package com.movtery.zalithlauncher.ui.screens.content.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.movtery.cardgrid.model.CardLimits
import com.movtery.cardgrid.model.CardType
import com.movtery.zalithlauncher.BuildConfig
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.screens.content.home.version.VersionCardContent

/** 系统卡片（不可变更），由启动器自行提供并绘制在网格之外 */
class SystemCard(val id: String, val content: @Composable () -> Unit)

/**
 * 主页卡片注册表
 */
object HomeCards {
    /** 版本卡片的类型 id */
    const val VERSION_CARD_TYPE_ID = "version_card"

    private val versionCardType = CardType(
        typeId = VERSION_CARD_TYPE_ID,
        defaultSpan = IntOffset(10, 6),
        limits = CardLimits(
            minWidth = 10,
            minHeight = 4,
            maxHeight = 12
        ),
        content = { cardId ->
            VersionCardContent(cardId)
        }
    )

    /** 版本卡片类型 */
    fun versionCardType(): CardType = versionCardType

    /** 用户卡片类型注册表 */
    val userCardTypes: List<CardType> = listOf(versionCardType)

    /** 系统卡片（不可变更） */
    fun systemCards(): List<SystemCard> = buildList {
        if (BuildConfig.DEBUG) {
            add(debugWarningCard())
        }
        add(dashboardCard())
    }

    /**
     * debug版本关不掉的警告，防止有人把测试版当正式版用 XD
     */
    private fun debugWarningCard() = SystemCard(id = "system_debug_warning") {
        BackgroundCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.generic_warning),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.launcher_version_debug_warning, BuildKeys.LAUNCHER_NAME),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    modifier = Modifier
                        .alpha(0.8f)
                        .align(Alignment.End),
                    text = stringResource(R.string.launcher_version_debug_warning_cant_close),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
    private fun dashboardCard() = SystemCard(id = "system_dashboard") {
        val context = LocalContext.current
        val stats = LauncherDashboardStats.read(context)
        val installed by VersionsManager.versions.collectAsStateWithLifecycle()
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DashboardTile("Horas Jogadas", "Ainda não monitoradas", "O launcher precisa medir o encerramento das sessões", Modifier.weight(1f))
                DashboardTile("Novidades", "Zalith Launcher 2", "Painel inicial personalizado", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DashboardTile("Última Partida", stats.lastVersion ?: "Nenhuma registrada", stats.lastDate ?: "Inicie um jogo para registrar", Modifier.weight(1f))
                DashboardTile("Estatísticas", "${stats.launches} inicializações", "${installed.size} versões instaladas", Modifier.weight(1f))
            }
        }
    }

    @Composable
    private fun DashboardTile(title: String, value: String, detail: String, modifier: Modifier = Modifier) {
        Column(
            modifier = modifier
                .border(0.8.dp, Color.White.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                .background(Color(0xFF242B33).copy(alpha = 0.92f), RoundedCornerShape(10.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = Color.White)
            Text(value, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.75f))
        }
    }

}
