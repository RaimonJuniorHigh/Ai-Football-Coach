package com.aifootballcoach.trainer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.aifootballcoach.trainer.theme.CoachColors

enum class Tab(val label: String, val icon: ImageVector) {
    Today("Vandaag", CoachIcons.Home),
    Squad("Selectie", CoachIcons.People),
    Assistant("Assistent", CoachIcons.Sparkle),
    Sessions("Trainingen", CoachIcons.Calendar),
}

/** Floating dark tab bar; the selected tab expands into a lime pill with its label. */
@Composable
fun CoachBottomBar(selected: Tab, onSelect: (Tab) -> Unit, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(CoachColors.Ink)
            .padding(horizontal = 10.dp),
    ) {
        Tab.entries.forEach { tab ->
            val isSelected = tab == selected
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(if (isSelected) CoachColors.Lime else Color.Transparent)
                    .clickable { onSelect(tab) }
                    .padding(horizontal = if (isSelected) 16.dp else 12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        tab.icon,
                        contentDescription = tab.label,
                        tint = if (isSelected) CoachColors.Ink else Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(18.dp),
                    )
                    if (isSelected) {
                        Spacer(Modifier.width(6.dp))
                        Text(tab.label, style = MaterialTheme.typography.labelMedium, color = CoachColors.Ink)
                    }
                }
            }
        }
    }
}
