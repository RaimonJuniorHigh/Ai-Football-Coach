package com.aifootballcoach.trainer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.aifootballcoach.trainer.theme.CoachColors

/** Segmented progress bar at the top of the onboarding steps. */
@Composable
fun StepProgress(current: Int, total: Int, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        repeat(total) { index ->
            Box(
                Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (index < current) CoachColors.Pitch else CoachColors.Track),
            )
        }
    }
}

/**
 * Layout shared by the numbered onboarding steps: progress bar, "Step x of y",
 * a big title, scrollable content and a pinned primary button.
 */
@Composable
fun StepScaffold(
    step: Int,
    totalSteps: Int,
    title: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    buttonEnabled: Boolean = true,
    buttonColor: Color = CoachColors.Ink,
    buttonIcon: ImageVector? = null,
    /** Shows a round back (or close, on step 1) button left of the progress bar. */
    onBack: (() -> Unit)? = null,
    backIcon: ImageVector = CoachIcons.ChevronLeft,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(CoachColors.Paper)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .padding(horizontal = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
            if (onBack != null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(CoachColors.Card)
                        .clickable(onClick = onBack),
                ) {
                    Icon(backIcon, contentDescription = "Terug", tint = CoachColors.Ink, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(12.dp))
            }
            StepProgress(current = step, total = totalSteps, modifier = Modifier.weight(1f))
        }
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(28.dp))
            Text(
                "Stap $step van $totalSteps",
                style = MaterialTheme.typography.labelSmall,
                color = CoachColors.Muted,
            )
            Spacer(Modifier.height(6.dp))
            Text(title, style = MaterialTheme.typography.headlineLarge, color = CoachColors.Ink)
            Spacer(Modifier.height(24.dp))
            content()
            Spacer(Modifier.height(24.dp))
        }
        PillButton(
            text = buttonText,
            onClick = onButtonClick,
            enabled = buttonEnabled,
            containerColor = buttonColor,
            icon = buttonIcon,
            modifier = Modifier.padding(bottom = 16.dp),
        )
    }
}

/** Small grey label above a form field, e.g. "Training days". */
@Composable
fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = CoachColors.Muted,
        modifier = modifier.padding(bottom = 8.dp),
    )
}

/** Uppercase, letter-spaced section header, e.g. "NEEDS YOU". */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.12.em,
        ),
        color = CoachColors.Muted,
        modifier = modifier.padding(start = 4.dp, bottom = 8.dp),
    )
}
