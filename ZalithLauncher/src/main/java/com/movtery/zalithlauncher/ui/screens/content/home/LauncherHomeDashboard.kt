package com.movtery.zalithlauncher.ui.screens.content.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.screens.content.elements.PlayerFace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val LocalDashboardOpenAccount = staticCompositionLocalOf<() -> Unit> { {} }
val LocalDashboardOpenVersions = staticCompositionLocalOf<() -> Unit> { {} }

private val DashboardText = Color(0xFFEDF1F5)
private val DashboardSecondary = Color(0xFFBCC8D2)

@Composable
fun LauncherHomeDashboard() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uriHandler = LocalUriHandler.current
    val openAccount = LocalDashboardOpenAccount.current
    val openVersions = LocalDashboardOpenVersions.current
    val installed by VersionsManager.versions.collectAsStateWithLifecycle()
    val refreshing by VersionsManager.isRefreshing.collectAsStateWithLifecycle()
    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()
    val revision = LauncherDashboardStats.revision
    var stats by remember { mutableStateOf(DashboardSnapshot()) }
    var worlds by remember { mutableStateOf<DashboardWorlds?>(null) }
    var detail by remember { mutableStateOf<String?>(null) }
    val scale = (LocalConfiguration.current.screenHeightDp / 430f).coerceIn(0.78f, 1.25f)

    LaunchedEffect(installed, refreshing, revision, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            if (!refreshing) {
                stats = withContext(Dispatchers.IO) { LauncherDashboardStats.read(context) }
                worlds = LauncherDashboardStats.readWorlds(installed)
            }
        }
    }

    val total = worlds?.let { LauncherDashboardStats.formatWorldTime(it.ticks) } ?: "—"
    val latest = worlds?.latest
    val lastVersion = latest?.levelMCVersion ?: stats.lastVersion ?: "Nenhuma"
    val lastDate = latest?.lastPlayed?.let(LauncherDashboardStats::formatDate) ?: stats.lastDate ?: "—"
    val mode = latest?.gameMode?.let { stringResource(it.nameRes) } ?: "—"

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy((10 * scale).dp)) {
        Box(
            modifier = Modifier
                .padding(top = (5 * scale).dp)
                .size((32 * scale).dp)
                .clip(CircleShape)
                .background(Color(0xFFDBDFE8))
                .clickable(role = Role.Button, onClick = openAccount),
            contentAlignment = Alignment.Center
        ) {
            if (account != null) {
                PlayerFace(account = account!!, avatarSize = (32 * scale).dp)
            } else {
                Icon(painterResource(R.drawable.ic_person_outlined), "Gerenciar conta", Modifier.size((25 * scale).dp), tint = Color(0xFFB3B9C6))
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy((5 * scale).dp)) {
            DashboardTile("Horas Jogadas", "Veja o tempo registrado nos seus mundos locais.", R.drawable.ic_schedule_outlined, Color(0xFF3997FF), scale, { detail = "Horas Jogadas" }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Metric("Total nos mundos", total, Modifier.weight(1f), scale)
                    MetricDivider(scale)
                    Metric("Hoje", "—", Modifier.weight(0.8f), scale)
                    MetricDivider(scale)
                    Metric("Mais iniciada", stats.mostPlayedVersion ?: "Nenhuma", Modifier.weight(1.2f), scale)
                }
            }
            DashboardTile("Novidades", "Fique por dentro do Minecraft e do Zalith Launcher.", R.drawable.ic_article_outlined, Color(0xFFCAD6EE), scale, { detail = "Novidades" }) {
                Row(horizontalArrangement = Arrangement.spacedBy((6 * scale).dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painterResource(R.drawable.launcher_update_art), null,
                        modifier = Modifier.size(width = (74 * scale).dp, height = (37 * scale).dp).clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        modifier = Modifier.weight(1f).background(Color.White.copy(alpha = 0.035f), RoundedCornerShape(4.dp)).padding((5 * scale).dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        DashboardLabel("Novidades do Minecraft", 11f * scale, true)
                        DashboardLabel("Confira as atualizações e seus recursos.", 9.5f * scale)
                        DashboardLabel("Toque para abrir os sites oficiais", 9f * scale)
                    }
                }
            }
            DashboardTile("Última Partida", "Veja as informações do seu último mundo salvo.", R.drawable.ic_sports_esports_filled, Color(0xFF19C774), scale, { detail = "Última Partida" }) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy((7 * scale).dp)) {
                    DashboardLabel(lastVersion, 10.5f * scale, modifier = Modifier.weight(0.8f))
                    MetricDivider(scale)
                    DashboardLabel(mode, 10f * scale, modifier = Modifier.weight(1f))
                    MetricDivider(scale)
                    DashboardLabel(lastDate, 10f * scale, modifier = Modifier.weight(1.5f))
                }
            }
            DashboardTile("Estatísticas", "Acompanhe suas estatísticas e o uso das versões.", R.drawable.ic_launcher_statistics, Color(0xFFA06AFF), scale, { detail = "Estatísticas" }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Metric("Versões instaladas", installed.size.toString(), Modifier.weight(1f), scale)
                    MetricDivider(scale)
                    Metric("Tempo nos mundos", total, Modifier.weight(1.1f), scale)
                    MetricDivider(scale)
                    Metric("Mundos salvos", worlds?.count?.toString() ?: "—", Modifier.weight(0.8f), scale)
                }
            }
        }
    }

    detail?.let { title ->
        val description = when (title) {
            "Horas Jogadas" -> "Tempo acumulado nos mundos locais: $total.\n\nEsse valor vem dos mundos salvos; não inclui servidores nem mede tempo em menus. A divisão por dia ainda não está disponível.\n\nMais iniciada: ${stats.mostPlayedVersion ?: "Nenhuma"}. Aberturas registradas: ${stats.launches}."
            "Novidades" -> "Acompanhe as novidades nos sites oficiais do Minecraft e do Zalith Launcher."
            "Última Partida" -> if (latest != null) "Mundo: ${latest.levelName.orEmpty()}\nMinecraft: $lastVersion\nModo: $mode\nÚltimo salvamento: $lastDate" else "Ainda não há um mundo salvo disponível.\n\nÚltima solicitação de abertura: ${stats.lastVersion ?: "Nenhuma"}\n${stats.lastDate.orEmpty()}"
            else -> "Versões instaladas: ${installed.size}\nMundos salvos: ${worlds?.count ?: 0}\nTempo acumulado nos mundos: $total\nAberturas registradas: ${stats.launches}\n\nPastas de mundos compartilhadas entre versões são contadas uma única vez."
        }
        AlertDialog(
            onDismissRequest = { detail = null },
            title = { Text(title) },
            text = { Text(description) },
            confirmButton = {
                TextButton(onClick = {
                    detail = null
                    if (title == "Novidades") uriHandler.openUri("https://www.minecraft.net/updates") else openVersions()
                }) { Text(if (title == "Novidades") "Minecraft" else "Ver versões") }
            },
            dismissButton = {
                Row {
                    if (title == "Novidades") {
                        TextButton(onClick = { detail = null; uriHandler.openUri("https://github.com/ZalithLauncher/ZalithLauncher2/releases") }) { Text("Zalith") }
                    }
                    TextButton(onClick = { detail = null }) { Text("Fechar") }
                }
            }
        )
    }
}

@Composable
private fun DashboardTile(
    title: String,
    subtitle: String,
    icon: Int,
    accent: Color,
    scale: Float,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape((11 * scale).dp)
    Row(
        modifier = Modifier.fillMaxWidth()
            .launcherGlow((11 * scale).dp, 0.68f)
            .clip(shape).background(Color(0xFF202E38).copy(alpha = 0.9f))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = (6 * scale).dp, vertical = (5 * scale).dp),
        horizontalArrangement = Arrangement.spacedBy((12 * scale).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size((43 * scale).dp).clip(RoundedCornerShape((6 * scale).dp)).background(accent.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(icon), null, Modifier.size((29 * scale).dp), tint = accent)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy((3 * scale).dp)) {
            DashboardLabel(title, 12f * scale, true)
            DashboardLabel(subtitle, 10f * scale)
            content()
        }
        Icon(painterResource(R.drawable.ic_keyboard_arrow_right), null, Modifier.size((20 * scale).dp), tint = DashboardSecondary)
    }
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier, scale: Float) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy((2 * scale).dp)) {
        DashboardLabel(label, 9f * scale)
        DashboardLabel(value, 11.5f * scale, true)
    }
}

@Composable
private fun MetricDivider(scale: Float) {
    Spacer(Modifier.padding(horizontal = (8 * scale).dp).width(0.5.dp).height((19 * scale).dp).background(Color.White.copy(alpha = 0.18f)))
}

@Composable
private fun DashboardLabel(text: String, size: Float, bold: Boolean = false, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        color = if (bold) DashboardText else DashboardSecondary,
        fontSize = size.sp,
        lineHeight = (size + 2).sp,
        fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
