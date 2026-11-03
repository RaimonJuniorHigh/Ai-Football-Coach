package com.aifootballcoach.trainer.ui.brief

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aifootballcoach.trainer.data.FocusSuggestion
import com.aifootballcoach.trainer.theme.CoachColors
import com.aifootballcoach.trainer.ui.components.CardTextField
import com.aifootballcoach.trainer.ui.components.CoachIcons
import com.aifootballcoach.trainer.ui.components.FieldLabel
import com.aifootballcoach.trainer.ui.components.StepScaffold

/**
 * Brief step 2 — what the session should work on. Not in the design; built in the
 * same style. The themes come from the assistant and are marked as its suggestion,
 * so the trainer can see where they come from and pick or add their own.
 */
@Composable
fun BriefFocusScreen(
    suggestions: List<FocusSuggestion>,
    selectedIds: Set<String>,
    onToggle: (FocusSuggestion) -> Unit,
    ownTheme: String,
    onOwnThemeChange: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    StepScaffold(
        step = 2,
        totalSteps = 3,
        title = "Waar werken we aan?",
        buttonText = "Volgende",
        onButtonClick = onNext,
        buttonEnabled = selectedIds.isNotEmpty() || ownTheme.isNotBlank(),
        onBack = onBack,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .background(CoachColors.Lime)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Icon(CoachIcons.Sparkle, contentDescription = null, tint = CoachColors.Ink, modifier = Modifier.size(11.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                "OP BASIS VAN DE STATS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                color = CoachColors.Ink,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "Op basis van de stats van je selectie. Kies er één of meer.",
            style = MaterialTheme.typography.labelSmall,
            color = CoachColors.Muted,
        )

        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            suggestions.forEach { suggestion ->
                SuggestionCard(suggestion, selected = suggestion.id in selectedIds) { onToggle(suggestion) }
            }
        }

        Spacer(Modifier.height(18.dp))
        FieldLabel("Of zelf een thema")
        CardTextField(
            value = ownTheme,
            onValueChange = onOwnThemeChange,
            placeholder = "bijv. Afronden na een voorzet",
        )
    }
}

@Composable
private fun SuggestionCard(suggestion: FocusSuggestion, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CoachColors.Card)
            .border(1.5.dp, if (selected) CoachColors.Pitch else Color.Transparent, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(suggestion.title, style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp), color = CoachColors.Ink)
            Text(suggestion.reason, style = MaterialTheme.typography.labelSmall, color = CoachColors.Muted)
        }
        Spacer(Modifier.width(10.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (selected) CoachColors.Pitch else CoachColors.Chip),
        ) {
            if (selected) {
                Icon(CoachIcons.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}
