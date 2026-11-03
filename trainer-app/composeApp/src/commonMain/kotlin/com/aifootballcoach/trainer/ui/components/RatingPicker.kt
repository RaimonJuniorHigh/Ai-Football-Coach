package com.aifootballcoach.trainer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.sp
import com.aifootballcoach.trainer.theme.CoachColors

/**
 * Row of five tiles for a 1–5 score. [large] is the bigger post-training variant,
 * where the picked number turns lime.
 */
@Composable
fun RatingPicker(
    selected: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    large: Boolean = false,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(if (large) 8.dp else 7.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        (1..5).forEach { value ->
            val isSelected = value == selected
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(if (large) 56.dp else 42.dp)
                    .clip(RoundedCornerShape(if (large) 16.dp else 14.dp))
                    .background(if (isSelected) CoachColors.Pitch else CoachColors.Card)
                    .clickable { onSelect(value) },
            ) {
                Text(
                    value.toString(),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = if (large) 20.sp else 15.sp,
                        fontWeight = if (large) FontWeight.ExtraBold else FontWeight.SemiBold,
                    ),
                    color = when {
                        isSelected && large -> CoachColors.Lime
                        isSelected -> Color.White
                        else -> CoachColors.Ink
                    },
                )
            }
        }
    }
}
