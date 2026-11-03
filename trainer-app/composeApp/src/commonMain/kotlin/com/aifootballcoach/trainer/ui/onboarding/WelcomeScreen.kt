package com.aifootballcoach.trainer.ui.onboarding

import androidx.compose.foundation.background
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
import com.aifootballcoach.trainer.theme.CoachColors
import com.aifootballcoach.trainer.ui.components.PillButton

private val steps = listOf("Geef je team een naam", "Voeg je spelers toe", "Nodig ze uit met één link")

/** Design screen 11 — "Onboarding · welcome". */
@Composable
fun WelcomeScreen(trainerName: String, onStart: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(CoachColors.Pitch)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(48.dp))
        Text(
            "Welkom, $trainerName",
            style = MaterialTheme.typography.labelSmall,
            color = CoachColors.OnPitchMuted,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Zet je team op in drie stappen.",
            style = MaterialTheme.typography.displayMedium,
            color = Color.White,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Duurt zo'n vijf minuten. Je kunt alles later nog aanpassen.",
            style = MaterialTheme.typography.bodyMedium,
            color = CoachColors.OnPitchMuted,
        )

        Spacer(Modifier.weight(1f))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            steps.forEachIndexed { index, label ->
                StepRow(number = index + 1, label = label, active = index == 0)
            }
        }
        Spacer(Modifier.height(20.dp))
        PillButton(
            text = "Beginnen",
            onClick = onStart,
            containerColor = CoachColors.Lime,
            contentColor = CoachColors.Ink,
            modifier = Modifier.padding(bottom = 16.dp),
        )
    }
}

@Composable
private fun StepRow(number: Int, label: String, active: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (active) CoachColors.Lime else Color.White.copy(alpha = 0.15f)),
        ) {
            Text(
                number.toString(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (active) CoachColors.Ink else Color.White,
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.titleMedium.copy(fontSize = MaterialTheme.typography.bodyMedium.fontSize), color = Color.White)
    }
}
