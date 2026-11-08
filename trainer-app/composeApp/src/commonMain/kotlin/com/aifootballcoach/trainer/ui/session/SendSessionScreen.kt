package com.aifootballcoach.trainer.ui.session

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aifootballcoach.trainer.theme.CoachColors
import com.aifootballcoach.trainer.ui.components.CoachIcons
import com.aifootballcoach.trainer.ui.components.PillButton

/** Design screen 21 — "Approve & send to players". */
@Composable
fun SendSessionScreen(
    totalBlocks: Int,
    acceptedBlocks: Int,
    minutes: Int,
    editedBlocks: Int,
    skippedBlocks: Int,
    players: Int,
    homeExercises: Boolean,
    onHomeExercisesChange: (Boolean) -> Unit,
    onSend: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(CoachColors.Paper)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(CoachColors.Card)
                .clickable(onClick = onBack),
        ) {
            Icon(CoachIcons.ChevronLeft, contentDescription = "Terug", tint = CoachColors.Ink, modifier = Modifier.size(16.dp))
        }

        Spacer(Modifier.height(20.dp))
        Text("Alle $totalBlocks blokken bekeken", style = MaterialTheme.typography.labelSmall, color = CoachColors.Muted)
        Text("Klaar om te versturen?", style = MaterialTheme.typography.headlineLarge, color = CoachColors.Ink)

        Spacer(Modifier.height(18.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CoachColors.Card),
        ) {
            SummaryRow("Training", "$minutes min · ${blocks(acceptedBlocks)}")
            HorizontalDivider(color = CoachColors.Paper)
            SummaryRow("Jij hebt aangepast", blocks(editedBlocks))
            HorizontalDivider(color = CoachColors.Paper)
            SummaryRow("Overgeslagen", blocks(skippedBlocks))
        }

        Spacer(Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CoachColors.Card)
                .clickable { onHomeExercisesChange(!homeExercises) }
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text("Thuisoefeningen", style = MaterialTheme.typography.titleMedium, color = CoachColors.Ink)
                Text(
                    "Spelers met een zwakke stat krijgen 2–4 eigen oefeningen in hun web-app.",
                    style = MaterialTheme.typography.labelSmall,
                    color = CoachColors.Muted,
                )
            }
            Spacer(Modifier.width(12.dp))
            Switch(
                checked = homeExercises,
                onCheckedChange = onHomeExercisesChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = CoachColors.Pitch,
                    checkedThumbColor = CoachColors.Lime,
                    uncheckedTrackColor = CoachColors.Chip,
                    uncheckedThumbColor = Color.White,
                    uncheckedBorderColor = CoachColors.Chip,
                ),
            )
        }

        Spacer(Modifier.weight(1f))
        PillButton(
            text = "Versturen naar $players spelers",
            onClick = onSend,
            enabled = acceptedBlocks > 0,
            containerColor = CoachColors.Pitch,
            modifier = Modifier.padding(bottom = 12.dp),
        )
    }
}

private fun blocks(count: Int) = if (count == 1) "1 blok" else "$count blokken"

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = CoachColors.Ink, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = CoachColors.Ink)
    }
}
