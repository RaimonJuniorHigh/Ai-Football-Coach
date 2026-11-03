package com.aifootballcoach.trainer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aifootballcoach.trainer.theme.CoachColors

/** White rounded text field with an optional small label on top, as used in the sheets. */
@Composable
fun CardTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CoachColors.Card)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        if (label != null) {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = CoachColors.Muted)
            Spacer(Modifier.height(2.dp))
        }
        Box {
            if (value.isEmpty()) {
                Text(placeholder, style = textStyle, color = CoachColors.Muted)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = textStyle.copy(color = CoachColors.Ink),
                cursorBrush = SolidColor(CoachColors.Pitch),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
