package com.aifootballcoach.trainer.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aifootballcoach.trainer.data.Team
import com.aifootballcoach.trainer.data.Weekday
import com.aifootballcoach.trainer.theme.CoachColors
import com.aifootballcoach.trainer.ui.components.FieldLabel
import com.aifootballcoach.trainer.ui.components.SegmentedControl
import com.aifootballcoach.trainer.ui.components.StepScaffold
import com.aifootballcoach.trainer.ui.components.ToggleChip

private val sessionLengths = listOf(60, 75, 90)

/** Design screen 12 — "Onboarding · your team" (step 1 of 3). */
@Composable
fun TeamSetupScreen(
    team: Team,
    onTeamChange: (Team) -> Unit,
    onNext: () -> Unit,
) {
    StepScaffold(
        step = 1,
        totalSteps = 3,
        title = "Welk team train je?",
        buttonText = "Volgende",
        onButtonClick = onNext,
        buttonEnabled = team.name.isNotBlank() && team.trainingDays.isNotEmpty(),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CoachColors.Card)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text("Teamnaam", style = MaterialTheme.typography.labelSmall, color = CoachColors.Muted)
            Spacer(Modifier.height(2.dp))
            BasicTextField(
                value = team.name,
                onValueChange = { onTeamChange(team.copy(name = it)) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = CoachColors.Ink,
                ),
                cursorBrush = SolidColor(CoachColors.Pitch),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(20.dp))
        FieldLabel("Trainingsdagen")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Weekday.entries.forEach { day ->
                val selected = day in team.trainingDays
                ToggleChip(
                    text = day.short,
                    selected = selected,
                    onClick = {
                        val days = if (selected) team.trainingDays - day else team.trainingDays + day
                        onTeamChange(team.copy(trainingDays = days))
                    },
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        FieldLabel("Gebruikelijke trainingsduur")
        SegmentedControl(
            options = sessionLengths,
            selected = team.sessionMinutes,
            label = { "$it min" },
            onSelect = { onTeamChange(team.copy(sessionMinutes = it)) },
        )
    }
}
