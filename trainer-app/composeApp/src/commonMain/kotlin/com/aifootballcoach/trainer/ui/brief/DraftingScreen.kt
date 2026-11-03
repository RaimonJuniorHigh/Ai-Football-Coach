package com.aifootballcoach.trainer.ui.brief

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aifootballcoach.trainer.theme.CoachColors
import com.aifootballcoach.trainer.ui.components.CoachIcons

/**
 * Design screen 19 — "Drafting", shown while the assistant builds the session.
 * [stepsDone] of [steps] are ticked off; the next one shows a spinner.
 */
@Composable
fun DraftingScreen(day: String, steps: List<String>, stepsDone: Int) {
    Column(
        Modifier
            .fillMaxSize()
            .background(CoachColors.Ink)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(48.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(CoachColors.Lime),
        ) {
            Icon(CoachIcons.Sparkle, contentDescription = null, tint = CoachColors.Ink, modifier = Modifier.size(22.dp))
        }

        Spacer(Modifier.height(28.dp))
        Text("$day wordt opgesteld…", style = MaterialTheme.typography.headlineLarge, color = Color.White)
        Spacer(Modifier.height(8.dp))
        Text(
            "Je bekijkt elk blok zodra het klaar is. Er gaat nog niks naar de spelers.",
            style = MaterialTheme.typography.bodyMedium,
            color = CoachColors.OnPitchMuted,
        )

        Spacer(Modifier.height(24.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            steps.take(stepsDone + 1).forEachIndexed { index, step ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(16.dp)) {
                        if (index < stepsDone) {
                            Icon(CoachIcons.Check, contentDescription = "Klaar", tint = CoachColors.Lime, modifier = Modifier.size(16.dp))
                        } else {
                            CircularProgressIndicator(color = CoachColors.Lime, strokeWidth = 2.dp, modifier = Modifier.size(13.dp))
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        step,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp),
                        color = if (index < stepsDone) Color.White else CoachColors.OnPitchMuted,
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))
        Text(
            "Namen blijven bij Sv Budel — de assistent ziet alleen posities en ontwikkelpunten.",
            style = MaterialTheme.typography.labelSmall,
            color = CoachColors.OnPitchMuted,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .padding(14.dp),
        )
    }
}
