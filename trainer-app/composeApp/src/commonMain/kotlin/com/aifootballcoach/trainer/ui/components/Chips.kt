package com.aifootballcoach.trainer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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

/** Rounded toggle chip, green when selected (e.g. training days). */
@Composable
fun ToggleChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = CoachColors.Pitch,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(if (selected) selectedColor else CoachColors.Chip)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else CoachColors.Ink,
        )
    }
}

/** iOS-style segmented control, e.g. "60 min | 75 min | 90 min". */
@Composable
fun <T> SegmentedControl(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(CoachColors.Chip)
            .padding(4.dp),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(if (isSelected) CoachColors.Card else Color.Transparent)
                    .clickable { onSelect(option) },
            ) {
                Text(
                    label(option),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    ),
                    color = CoachColors.Ink,
                )
            }
        }
    }
}
