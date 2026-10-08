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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.movtery.cardgrid.model.CardLimits
import com.movtery.cardgrid.model.CardType
import com.movtery.zalithlauncher.BuildConfig
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.R
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
        Box(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF2A353E), RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(R.string.generic_warning),
                    color = Color(0xFFCBD4DD),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp)
                )
                Text(
                    text = stringResource(R.string.launcher_version_debug_warning, BuildKeys.LAUNCHER_NAME),
                    color = Color(0xFFDDE3E9),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, lineHeight = 17.sp)
                )
                Text(
                    modifier = Modifier
                        .alpha(0.8f)
                        .align(Alignment.End),
                    text = stringResource(R.string.launcher_version_debug_warning_cant_close),
                    color = Color(0xFFDDE3E9),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
                )
            }
        }
    }
    private fun dashboardCard() = SystemCard(id = "system_dashboard") {
        LauncherHomeDashboard()
    }
}
