package com.aifootballcoach.trainer.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import com.aifootballcoach.trainer.theme.CoachColors
import com.aifootballcoach.trainer.ui.components.CoachIcons
import com.aifootballcoach.trainer.ui.components.PillButton
import com.aifootballcoach.trainer.ui.components.StepScaffold

/** Design screen 14 — "Onboarding · invite players" (step 3 of 3). */
@Composable
fun InvitePlayersScreen(
    teamLink: String,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onDone: () -> Unit,
) {
    var copied by remember { mutableStateOf(false) }

    StepScaffold(
        step = 3,
        totalSteps = 3,
        title = "Send one link to the team.",
        buttonText = "Done — go to Today",
        onButtonClick = onDone,
    ) {
        Text(
            "Players open it in their browser — nothing to install. " +
                "Each one only ever sees their own exercises.",
            style = MaterialTheme.typography.bodyMedium,
            color = CoachColors.Muted,
            modifier = Modifier.padding(top = 0.dp),
        )
        Spacer(Modifier.height(20.dp))

        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CoachColors.Pitch)
                .padding(16.dp),
        ) {
            Text("Team link", style = MaterialTheme.typography.labelSmall, color = CoachColors.OnPitchMuted)
            Spacer(Modifier.height(2.dp))
            Text(
                teamLink,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                color = Color.White,
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PillButton(
                    text = "Share",
                    onClick = onShare,
                    containerColor = CoachColors.Lime,
                    contentColor = CoachColors.Ink,
                    height = 44.dp,
                    modifier = Modifier.weight(1f),
                )
                PillButton(
                    text = if (copied) "Copied" else "Copy",
                    onClick = {
                        onCopy()
                        copied = true
                    },
                    containerColor = Color.White.copy(alpha = 0.15f),
                    contentColor = Color.White,
                    height = 44.dp,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CoachColors.Card)
                .padding(16.dp),
        ) {
            Icon(CoachIcons.Shield, contentDescription = null, tint = CoachColors.Pitch, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                "Players under 16 are asked for a parent's email. They join once the parent agrees.",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                color = CoachColors.Ink,
            )
        }
    }
}
