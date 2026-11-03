package com.aifootballcoach.trainer.ui.player

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aifootballcoach.trainer.data.DevelopmentPoint
import com.aifootballcoach.trainer.data.Player
import com.aifootballcoach.trainer.theme.CoachColors
import com.aifootballcoach.trainer.ui.components.CardTextField
import com.aifootballcoach.trainer.ui.components.CoachBottomSheet
import com.aifootballcoach.trainer.ui.components.PillButton
import com.aifootballcoach.trainer.ui.components.RatingPicker

/** Design screen 16 — "Log an observation (after training)". */
@Composable
fun LogObservationSheet(
    player: Player,
    point: DevelopmentPoint,
    hasNextPlayer: Boolean,
    onSave: (rating: Int, note: String) -> Unit,
    onDismiss: () -> Unit,
    initialRating: Int? = null,
) {
    // Keyed on the point so moving on to the next player starts with a clean sheet.
    var rating by remember(point.id) { mutableStateOf(initialRating) }
    var note by remember(point.id) { mutableStateOf("") }

    CoachBottomSheet(onDismiss = onDismiss) {
        Text(
            "${player.firstName} · ${point.title} · vanavond",
            style = MaterialTheme.typography.labelSmall,
            color = CoachColors.Muted,
        )
        Text(
            "Hoe ging het?",
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 26.sp, lineHeight = 30.sp),
            color = CoachColors.Ink,
        )

        Spacer(Modifier.height(14.dp))
        RatingPicker(selected = rating, onSelect = { rating = it }, large = true)
        Row(Modifier.fillMaxWidth().padding(top = 6.dp, start = 4.dp, end = 4.dp)) {
            Text("Lastig", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = CoachColors.Muted)
            Spacer(Modifier.weight(1f))
            Text("Ging top", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = CoachColors.Muted)
        }

        Spacer(Modifier.height(14.dp))
        CardTextField(
            value = note,
            onValueChange = { note = it },
            placeholder = "Notitie toevoegen (optioneel)",
        )

        Spacer(Modifier.height(16.dp))
        PillButton(
            text = if (hasNextPlayer) "Opslaan · volgende speler" else "Opslaan",
            onClick = { rating?.let { onSave(it, note) } },
            enabled = rating != null,
            containerColor = CoachColors.Pitch,
            height = 52.dp,
        )
    }
}
