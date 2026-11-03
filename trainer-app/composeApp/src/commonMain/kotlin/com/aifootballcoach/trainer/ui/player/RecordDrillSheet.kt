package com.aifootballcoach.trainer.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aifootballcoach.trainer.data.Player
import com.aifootballcoach.trainer.data.Position
import com.aifootballcoach.trainer.data.StatType
import com.aifootballcoach.trainer.theme.CoachColors
import com.aifootballcoach.trainer.ui.components.CardTextField
import com.aifootballcoach.trainer.ui.components.CoachBottomSheet
import com.aifootballcoach.trainer.ui.components.PillButton
import com.aifootballcoach.trainer.ui.components.ToggleChip

/**
 * Bottom sheet to note a training result: pick the stat that was trained, then how many attempts
 * there were and how many worked (for example 100 shots, 70 goals). The database turns that
 * into the player's stat from 1 to 20.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecordDrillSheet(
    player: Player,
    onSave: (stat: StatType, attempts: Int, successes: Int) -> Unit,
    onDismiss: () -> Unit,
    error: String? = null,
    initialAttempts: String = "",
    initialSuccesses: String = "",
) {
    var stat by remember { mutableStateOf(if (player.position == Position.Keeper) StatType.GOALKEEPING else StatType.FINISHING) }
    var attempts by remember { mutableStateOf(initialAttempts) }
    var successes by remember { mutableStateOf(initialSuccesses) }

    val attemptsNumber = attempts.toIntOrNull()
    val successesNumber = successes.toIntOrNull()
    val valid = attemptsNumber != null && attemptsNumber > 0 && successesNumber != null && successesNumber in 0..attemptsNumber

    CoachBottomSheet(onDismiss = onDismiss) {
        Text(player.name, style = MaterialTheme.typography.labelSmall, color = CoachColors.Muted)
        Text(
            "Oefening noteren",
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 26.sp, lineHeight = 30.sp),
            color = CoachColors.Ink,
        )

        Spacer(Modifier.height(14.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            StatType.entries.forEach { option ->
                ToggleChip(text = option.label, selected = option == stat, onClick = { stat = option })
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(stat.drill, style = MaterialTheme.typography.labelSmall, color = CoachColors.Muted)

        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CardTextField(
                value = attempts,
                onValueChange = { attempts = it.filter(Char::isDigit).take(4) },
                label = "Pogingen",
                placeholder = "100",
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            CardTextField(
                value = successes,
                onValueChange = { successes = it.filter(Char::isDigit).take(4) },
                label = "Gelukt",
                placeholder = "70",
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            )
        }

        if (attemptsNumber != null && successesNumber != null && successesNumber > attemptsNumber) {
            Spacer(Modifier.height(6.dp))
            Text("Er kunnen er niet meer lukken dan er pogingen waren.", style = MaterialTheme.typography.labelSmall, color = CoachColors.Alert)
        }
        if (error != null) {
            Spacer(Modifier.height(6.dp))
            Text(error, style = MaterialTheme.typography.labelSmall, color = CoachColors.Alert)
        }

        Spacer(Modifier.height(18.dp))
        PillButton(
            text = "Oefening opslaan",
            onClick = { if (valid) onSave(stat, attemptsNumber!!, successesNumber!!) },
            enabled = valid,
            containerColor = CoachColors.Pitch,
            height = 52.dp,
        )
    }
}
