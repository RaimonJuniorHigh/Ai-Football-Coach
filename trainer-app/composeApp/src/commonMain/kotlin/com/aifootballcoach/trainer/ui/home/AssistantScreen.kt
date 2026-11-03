package com.aifootballcoach.trainer.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.aifootballcoach.trainer.theme.CoachColors
import com.aifootballcoach.trainer.ui.components.CoachIcons
import com.aifootballcoach.trainer.ui.components.PillButton

/**
 * Assistant tab. Not designed yet; for now it's the way into the brief flow,
 * and explains what the assistant does before the trainer starts.
 */
@Composable
fun AssistantScreen(onNewSession: () -> Unit, contentPadding: PaddingValues) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(32.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(CoachColors.Lime),
        ) {
            Icon(CoachIcons.Sparkle, contentDescription = null, tint = CoachColors.Ink, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text("Laat de assistent een training opstellen.", style = MaterialTheme.typography.headlineLarge, color = CoachColors.Ink)
        Spacer(Modifier.height(8.dp))
        Text(
            "Jij geeft aan wie er komt en waar je aan wilt werken. De assistent maakt een voorstel, " +
                "en jij past het aan en keurt het goed. Er gaat niks naar spelers zonder jouw akkoord.",
            style = MaterialTheme.typography.bodyMedium,
            color = CoachColors.Muted,
        )
        Spacer(Modifier.weight(1f))
        PillButton(
            text = "Nieuwe training opstellen",
            onClick = onNewSession,
            containerColor = CoachColors.Pitch,
            icon = CoachIcons.Sparkle,
            modifier = Modifier.padding(bottom = 12.dp),
        )
    }
}
