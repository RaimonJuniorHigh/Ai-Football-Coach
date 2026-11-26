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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aifootballcoach.trainer.data.Intensity
import com.aifootballcoach.trainer.data.SessionBlock
import com.aifootballcoach.trainer.theme.CoachColors
import com.aifootballcoach.trainer.ui.components.CardTextField
import com.aifootballcoach.trainer.ui.components.FieldLabel

private const val MinuteStep = 5
private const val MinMinutes = 5
private const val MaxMinutes = 60

/** Design screen 20 — "Edit a proposed block". */
@Composable
fun EditBlockScreen(
    block: SessionBlock,
    number: Int,
    onSave: (SessionBlock) -> Unit,
    onCancel: () -> Unit,
) {
    var drill by remember(block.id) { mutableStateOf(block.drill) }
    var minutes by remember(block.id) { mutableIntStateOf(block.minutes) }
    var intensity by remember(block.id) { mutableStateOf(block.intensity) }
    var howItWorks by remember(block.id) { mutableStateOf(block.howItWorks) }

    Column(
        Modifier
            .fillMaxSize()
            .background(CoachColors.Paper)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .padding(horizontal = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 14.dp)) {
            TextButton("Annuleren", CoachColors.Muted, onClick = onCancel)
            Text(
                "Blok $number",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CoachColors.Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                "Opslaan",
                CoachColors.Pitch,
                enabled = drill.isNotBlank(),
                onClick = {
                    onSave(block.copy(drill = drill.trim(), minutes = minutes, intensity = intensity, howItWorks = howItWorks.trim()))
                },
            )
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            CardTextField(
                value = drill,
                onValueChange = { drill = it },
                label = "Oefening",
                placeholder = "Naam van de oefening",
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
            )

            Spacer(Modifier.height(10.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CoachColors.Card),
            ) {
                SettingRow("Duur") {
                    StepButton("−", enabled = minutes > MinMinutes) { minutes -= MinuteStep }
                    Text(
                        "$minutes min",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
                        color = CoachColors.Ink,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(64.dp),
                    )
                    StepButton("+", enabled = minutes < MaxMinutes) { minutes += MinuteStep }
                }
                HorizontalDivider(color = CoachColors.Paper)
                SettingRow("Intensiteit", onClick = { intensity = Intensity.entries[(intensity.ordinal + 1) % Intensity.entries.size] }) {
                    Text(intensity.label, style = MaterialTheme.typography.bodyMedium, color = CoachColors.Muted)
                }
                HorizontalDivider(color = CoachColors.Paper)
                SettingRow("Spelers") {
                    Text(block.players.toString(), style = MaterialTheme.typography.bodyMedium, color = CoachColors.Muted)
                }
            }

            Spacer(Modifier.height(16.dp))
            FieldLabel("Hoe het werkt")
            CardTextField(
                value = howItWorks,
                onValueChange = { howItWorks = it },
                placeholder = "Leg kort uit hoe de oefening gaat",
            )

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp)) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(CoachColors.Pitch))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Jouw aanpassingen gaan mee in de training die je verstuurt.",
                    style = MaterialTheme.typography.labelSmall,
                    color = CoachColors.Muted,
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TextButton(text: String, color: Color, enabled: Boolean = true, onClick: () -> Unit) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
        color = if (enabled) color else color.copy(alpha = 0.4f),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp),
    )
}

@Composable
private fun SettingRow(label: String, onClick: (() -> Unit)? = null, trailing: @Composable () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = CoachColors.Ink, modifier = Modifier.weight(1f))
        trailing()
    }
}

@Composable
private fun StepButton(symbol: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(CoachColors.Chip)
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Text(
            symbol,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) CoachColors.Ink else CoachColors.Muted,
        )
    }
}
