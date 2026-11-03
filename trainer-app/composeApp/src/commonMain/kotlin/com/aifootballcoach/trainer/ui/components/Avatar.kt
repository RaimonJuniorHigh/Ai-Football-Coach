package com.aifootballcoach.trainer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aifootballcoach.trainer.theme.CoachColors

/** Grey circle with initials, used for players and the trainer. */
@Composable
fun InitialsAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size).clip(CircleShape).background(CoachColors.Chip),
    ) {
        Text(
            initials,
            style = textStyle,
            color = CoachColors.Muted,
        )
    }
}
