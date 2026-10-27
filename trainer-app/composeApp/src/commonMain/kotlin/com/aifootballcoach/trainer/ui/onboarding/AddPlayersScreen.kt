package com.aifootballcoach.trainer.ui.onboarding

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.aifootballcoach.trainer.data.Player
import com.aifootballcoach.trainer.data.Position
import com.aifootballcoach.trainer.theme.CoachColors
import com.aifootballcoach.trainer.ui.components.FieldLabel
import com.aifootballcoach.trainer.ui.components.InitialsAvatar
import com.aifootballcoach.trainer.ui.components.StepScaffold

/**
 * Design screen 13 — "Onboarding · add players" (step 2 of 3).
 * Type a name, then tap a position chip to add the player to the squad.
 */
@Composable
fun AddPlayersScreen(
    players: List<Player>,
    onAddPlayer: (name: String, position: Position) -> Unit,
    onImportFromClubList: () -> Unit,
    onNext: () -> Unit,
) {
    var name by remember { mutableStateOf("") }

    StepScaffold(
        step = 2,
        totalSteps = 3,
        title = "Who's in the squad?",
        buttonText = "Next",
        onButtonClick = onNext,
        buttonEnabled = players.isNotEmpty(),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CoachColors.Card)
                .padding(14.dp),
        ) {
            Box {
                if (name.isEmpty()) {
                    Text("Player name", style = MaterialTheme.typography.bodyMedium, color = CoachColors.Muted)
                }
                BasicTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = CoachColors.Ink),
                    cursorBrush = SolidColor(CoachColors.Pitch),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Position.entries.forEach { position ->
                    PositionChip(
                        position = position,
                        enabled = name.isNotBlank(),
                        onClick = {
                            onAddPlayer(name, position)
                            name = ""
                        },
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        FieldLabel("Added · ${players.size}")
        if (players.isNotEmpty()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CoachColors.Card),
            ) {
                players.forEachIndexed { index, player ->
                    if (index > 0) HorizontalDivider(color = CoachColors.Paper)
                    PlayerRow(player)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "Import from club list instead",
            style = MaterialTheme.typography.labelMedium,
            color = CoachColors.Pitch,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onImportFromClubList)
                .padding(8.dp),
        )
    }
}

@Composable
private fun PositionChip(position: Position, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(CoachColors.Chip)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Text(
            position.label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = CoachColors.Ink,
        )
    }
}

@Composable
private fun PlayerRow(player: Player) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        InitialsAvatar(player.initials)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(player.name, style = MaterialTheme.typography.titleMedium, color = CoachColors.Ink)
            Text(player.role, style = MaterialTheme.typography.labelSmall, color = CoachColors.Muted)
        }
    }
}
