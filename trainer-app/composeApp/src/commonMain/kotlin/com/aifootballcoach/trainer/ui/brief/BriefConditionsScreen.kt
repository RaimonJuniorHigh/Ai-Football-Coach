package com.aifootballcoach.trainer.ui.brief

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aifootballcoach.trainer.data.Intensity
import com.aifootballcoach.trainer.data.PitchSpace
import com.aifootballcoach.trainer.theme.CoachColors
import com.aifootballcoach.trainer.ui.components.CardTextField
import com.aifootballcoach.trainer.ui.components.CoachIcons
import com.aifootballcoach.trainer.ui.components.FieldLabel
import com.aifootballcoach.trainer.ui.components.SegmentedControl
import com.aifootballcoach.trainer.ui.components.StepScaffold

/** Design screen 18 — "Brief · step 3 — conditions". */
@Composable
fun BriefConditionsScreen(
    space: PitchSpace,
    onSpaceChange: (PitchSpace) -> Unit,
    intensity: Intensity,
    onIntensityChange: (Intensity) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    onDraft: () -> Unit,
    onBack: () -> Unit,
) {
    StepScaffold(
        step = 3,
        totalSteps = 3,
        title = "Waar moeten we rekening mee houden?",
        buttonText = "Training laten opstellen",
        onButtonClick = onDraft,
        buttonColor = CoachColors.Pitch,
        buttonIcon = CoachIcons.Sparkle,
        onBack = onBack,
    ) {
        FieldLabel("Ruimte")
        SegmentedControl(
            options = PitchSpace.entries,
            selected = space,
            label = { it.label },
            onSelect = onSpaceChange,
        )

        Spacer(Modifier.height(16.dp))
        FieldLabel("Intensiteit")
        SegmentedControl(
            options = Intensity.entries,
            selected = intensity,
            label = { it.label },
            onSelect = onIntensityChange,
        )
        // TODO: comes from the match calendar once the backend has it.
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp, start = 4.dp)) {
            Icon(CoachIcons.Sparkle, contentDescription = null, tint = CoachColors.Pitch, modifier = Modifier.size(11.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                "Zaterdag is er een wedstrijd — daarom gemiddeld.",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = CoachColors.Muted,
            )
        }

        Spacer(Modifier.height(16.dp))
        FieldLabel("Verder nog iets?")
        CardTextField(
            value = notes,
            onValueChange = onNotesChange,
            placeholder = "bijv. Twee keepers. JO15 heeft de andere helft.",
        )
    }
}
