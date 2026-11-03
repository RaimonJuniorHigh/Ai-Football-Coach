package com.aifootballcoach.trainer.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aifootballcoach.trainer.data.DevelopmentPoint
import com.aifootballcoach.trainer.data.Player
import com.aifootballcoach.trainer.data.PlayerStats
import com.aifootballcoach.trainer.theme.CoachColors
import com.aifootballcoach.trainer.ui.components.CoachIcons
import com.aifootballcoach.trainer.ui.components.InitialsAvatar
import com.aifootballcoach.trainer.ui.components.PillButton
import com.aifootballcoach.trainer.ui.components.SectionLabel
import com.aifootballcoach.trainer.ui.components.StatCard
import kotlin.math.abs
import kotlin.math.roundToInt

/** Highest score on the 1–5 scale; the progress bars fill up to this. */
private const val MaxScore = 5.0

/** Design screen 04 — "Player", with the player's development points. */
@Composable
fun PlayerScreen(
    player: Player,
    stats: PlayerStats,
    points: List<DevelopmentPoint>,
    onBack: () -> Unit,
    onAddPoint: () -> Unit,
    onOpenPoint: (DevelopmentPoint) -> Unit,
    onLogObservation: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(CoachColors.Paper)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp),
    ) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(8.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(CoachColors.Card)
                    .clickable(onClick = onBack),
            ) {
                Icon(CoachIcons.ChevronLeft, contentDescription = "Terug", tint = CoachColors.Ink, modifier = Modifier.size(18.dp))
            }

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                InitialsAvatar(
                    player.initials,
                    size = 64.dp,
                    textStyle = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                )
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        player.name,
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 26.sp, lineHeight = 30.sp),
                        color = CoachColors.Ink,
                    )
                    Text(subtitle(player), style = MaterialTheme.typography.labelSmall, color = CoachColors.Muted)
                }
            }

            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(stats.sessions.toString(), "trainingen", Modifier.weight(1f))
                StatCard("${stats.attendancePercent}%", "aanwezig", Modifier.weight(1f))
                StatCard(points.size.toString(), "open punten", Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionLabel("Ontwikkelpunten", Modifier.weight(1f))
                Text(
                    "+ Nieuw punt",
                    style = MaterialTheme.typography.labelMedium,
                    color = CoachColors.Pitch,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .clickable(onClick = onAddPoint)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
            if (points.isEmpty()) {
                Text(
                    "Nog geen ontwikkelpunten. Voeg er een toe om ${player.firstName} te volgen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CoachColors.Muted,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                points.forEach { point -> PointCard(point) { onOpenPoint(point) } }
            }
            Spacer(Modifier.height(16.dp))
        }

        PillButton(
            text = "Observatie noteren",
            onClick = onLogObservation,
            enabled = points.isNotEmpty(),
            containerColor = CoachColors.Pitch,
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
        )
    }
}

private fun subtitle(player: Player) = listOfNotNull(
    player.role,
    player.age?.let { "$it jaar" },
    player.shirtNumber?.let { "#$it" },
).joinToString(" · ")

@Composable
private fun PointCard(point: DevelopmentPoint, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CoachColors.Card)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                point.title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                color = CoachColors.Ink,
                modifier = Modifier.weight(1f),
            )
            Text(
                point.score.oneDecimal(),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = CoachColors.Pitch,
            )
        }
        Spacer(Modifier.height(10.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(CoachColors.Chip),
        ) {
            Box(
                Modifier
                    .fillMaxWidth((point.score / MaxScore).toFloat().coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(CoachColors.Pitch),
            )
        }
        Spacer(Modifier.height(8.dp))
        Row {
            Text(weeksOpen(point.weeksOpen), style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = CoachColors.Muted)
            Spacer(Modifier.weight(1f))
            Trend(point.change)
        }
    }
}

@Composable
private fun Trend(change: Double?) {
    val (text, color) = when {
        change == null -> "Eerste meting" to CoachColors.Muted
        abs(change) < 0.05 -> "Geen vooruitgang" to CoachColors.Alert
        change > 0 -> "+${change.oneDecimal()}" to CoachColors.Pitch
        else -> "−${(-change).oneDecimal()}" to CoachColors.Alert
    }
    Text(
        text,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
        color = color,
    )
}

private fun weeksOpen(weeks: Int) = when (weeks) {
    0 -> "Net toegevoegd"
    1 -> "1 week open"
    else -> "$weeks weken open"
}

/** Formats a score the Dutch way, with a decimal comma: 2.8 → "2,8". */
internal fun Double.oneDecimal(): String {
    val tenths = (this * 10).roundToInt()
    return "${tenths / 10},${abs(tenths % 10)}"
}
