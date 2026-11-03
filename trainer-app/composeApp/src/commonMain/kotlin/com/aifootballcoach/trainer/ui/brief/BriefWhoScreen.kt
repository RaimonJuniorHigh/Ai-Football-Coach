package com.aifootballcoach.trainer.ui.brief

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aifootballcoach.trainer.data.PlannedSession
import com.aifootballcoach.trainer.data.Player
import com.aifootballcoach.trainer.theme.CoachColors
import com.aifootballcoach.trainer.ui.components.CoachIcons
import com.aifootballcoach.trainer.ui.components.StepScaffold

/** Design screen 17 — "Brief · step 1 — when & who". */
@Composable
fun BriefWhoScreen(
    session: PlannedSession,
    players: List<Player>,
    absentIds: Set<String>,
    onToggleAbsent: (Player) -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit,
) {
    StepScaffold(
        step = 1,
        totalSteps = 3,
        title = "Wanneer, en wie komt er?",
        buttonText = "Volgende",
        onButtonClick = onNext,
        buttonEnabled = absentIds.size < players.size,
        onBack = onClose,
        backIcon = CoachIcons.Close,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CoachColors.Card)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text("Training", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = CoachColors.Muted)
                Text(
                    session.dateLabel,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = CoachColors.Ink,
                )
            }
            Text("${session.minutes} min", style = MaterialTheme.typography.labelMedium, color = CoachColors.Pitch)
        }

        Spacer(Modifier.height(10.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CoachColors.Card)
                .padding(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Beschikbaar", style = MaterialTheme.typography.labelSmall, color = CoachColors.Muted, modifier = Modifier.weight(1f))
                Text(
                    "${players.size - absentIds.size} van ${players.size}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = CoachColors.Ink,
                )
            }
            Spacer(Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                players.forEach { player ->
                    AvailabilityDot(player, present = player.id !in absentIds) { onToggleAbsent(player) }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Tik op een speler om af te melden.",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = CoachColors.Muted,
            )
        }
    }
}

@Composable
private fun AvailabilityDot(player: Player, present: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(if (present) CoachColors.Pitch else CoachColors.Chip)
            .clickable(onClick = onClick),
    ) {
        Text(
            player.initials,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
            color = if (present) Color.White else CoachColors.Muted,
        )
    }
}
