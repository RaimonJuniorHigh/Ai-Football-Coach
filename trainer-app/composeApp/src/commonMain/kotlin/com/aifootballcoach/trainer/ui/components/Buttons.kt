package com.aifootballcoach.trainer.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aifootballcoach.trainer.theme.CoachColors

/** Full-width pill button used at the bottom of every flow screen. */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = CoachColors.Ink,
    contentColor: Color = Color.White,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(percent = 50),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.3f),
            disabledContentColor = contentColor.copy(alpha = 0.7f),
        ),
        modifier = modifier.fillMaxWidth().height(56.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
