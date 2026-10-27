package com.aifootballcoach.trainer.ui.squad

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aifootballcoach.trainer.data.Availability
import com.aifootballcoach.trainer.data.Player
import com.aifootballcoach.trainer.data.PlayerFlag
import com.aifootballcoach.trainer.data.Position
import com.aifootballcoach.trainer.theme.CoachColors
import com.aifootballcoach.trainer.ui.components.InitialsAvatar
import com.aifootballcoach.trainer.ui.components.ToggleChip

/** Squad filter; `null` position means "All". */
private val filters: List<Position?> = listOf(null, Position.Defence, Position.Midfield, Position.Attack)

private val topShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
private val bottomShape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)

/** Design screen 03 — "Squad". */
@Composable
fun SquadScreen(
    teamName: String,
    players: List<Player>,
    onOpenPlayer: (Player) -> Unit,
    contentPadding: PaddingValues,
) {
    var filter by remember { mutableStateOf<Position?>(null) }
    val visible = players.filter { filter == null || it.position == filter }

    LazyColumn(
        contentPadding = contentPadding,
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
    ) {
        item {
            Spacer(Modifier.height(12.dp))
            Text("${players.size} players", style = MaterialTheme.typography.labelSmall, color = CoachColors.Muted)
            Text(teamName, style = MaterialTheme.typography.headlineLarge, color = CoachColors.Ink)
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(filters) { position ->
                    ToggleChip(
                        text = position?.label ?: "All",
                        selected = filter == position,
                        onClick = { filter = position },
                        selectedColor = CoachColors.Ink,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        itemsIndexed(visible, key = { _, player -> player.id }) { index, player ->
            val shape = when {
                visible.size == 1 -> RoundedCornerShape(16.dp)
                index == 0 -> topShape
                index == visible.lastIndex -> bottomShape
                else -> RoundedCornerShape(0.dp)
            }
            Column(Modifier.clip(shape).background(CoachColors.Card)) {
                if (index > 0) HorizontalDivider(color = CoachColors.Paper)
                SquadRow(player) { onOpenPlayer(player) }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun SquadRow(player: Player, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        InitialsAvatar(player.initials)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(player.name, style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp), color = CoachColors.Ink)
            Text(player.role, style = MaterialTheme.typography.labelSmall, color = CoachColors.Muted)
        }
        StatusTag(player)
    }
}

@Composable
private fun StatusTag(player: Player) {
    val (label, background, content) = when {
        player.flag == PlayerFlag.Stalled -> Triple("Stalled", CoachColors.AlertSoft, CoachColors.Alert)
        player.flag == PlayerFlag.Review -> Triple("Review", CoachColors.PitchSoft, CoachColors.Pitch)
        player.availability == Availability.Out -> Triple("Out", CoachColors.Chip, CoachColors.Muted)
        else -> Triple("In", CoachColors.Chip, CoachColors.Muted)
    }
    Box(
        Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(background)
            .padding(horizontal = 9.dp, vertical = 3.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
            color = content,
        )
    }
}
