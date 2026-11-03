package com.aifootballcoach.trainer.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aifootballcoach.trainer.data.Player
import com.aifootballcoach.trainer.data.PointCategory
import com.aifootballcoach.trainer.theme.CoachColors
import com.aifootballcoach.trainer.ui.components.CardTextField
import com.aifootballcoach.trainer.ui.components.CoachBottomSheet
import com.aifootballcoach.trainer.ui.components.FieldLabel
import com.aifootballcoach.trainer.ui.components.PillButton
import com.aifootballcoach.trainer.ui.components.RatingPicker
import com.aifootballcoach.trainer.ui.components.ToggleChip

/** Design screen 15 — "Add a development point" (bottom sheet over the player). */
@Composable
fun AddPointSheet(
    player: Player,
    onSave: (title: String, category: PointCategory, score: Int) -> Unit,
    onDismiss: () -> Unit,
    initialTitle: String = "",
) {
    var category by remember { mutableStateOf(PointCategory.Defending) }
    var title by remember { mutableStateOf(initialTitle) }
    var score by remember { mutableIntStateOf(3) }

    CoachBottomSheet(onDismiss = onDismiss) {
        Text(player.name, style = MaterialTheme.typography.labelSmall, color = CoachColors.Muted)
        Text(
            "Nieuw ontwikkelpunt",
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 26.sp, lineHeight = 30.sp),
            color = CoachColors.Ink,
        )

        Spacer(Modifier.height(14.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            PointCategory.entries.forEach { option ->
                ToggleChip(
                    text = option.label,
                    selected = option == category,
                    onClick = { category = option },
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        CardTextField(
            value = title,
            onValueChange = { title = it },
            label = "Waar werkt ${player.firstName} aan?",
            placeholder = "bijv. 1-tegen-1 — binnenkant dichtzetten",
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        )

        Spacer(Modifier.height(14.dp))
        FieldLabel("Waar staat ${player.firstName} nu?")
        RatingPicker(selected = score, onSelect = { score = it })

        Spacer(Modifier.height(18.dp))
        PillButton(
            text = "Punt opslaan",
            onClick = { onSave(title, category, score) },
            enabled = title.isNotBlank(),
            containerColor = CoachColors.Pitch,
            height = 52.dp,
        )
    }
}
