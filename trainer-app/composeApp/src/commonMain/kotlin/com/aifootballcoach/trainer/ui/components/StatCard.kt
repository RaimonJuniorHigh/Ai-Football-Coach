package com.aifootballcoach.trainer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aifootballcoach.trainer.theme.CoachColors

/** White card with a big number and a small label, e.g. "14 / beschikbaar". */
@Composable
fun StatCard(value: String, label: String, modifier: Modifier = Modifier, valueColor: Color = CoachColors.Ink) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CoachColors.Card)
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Text(
            value,
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 22.sp, lineHeight = 26.sp),
            color = valueColor,
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = CoachColors.Muted)
    }
}
