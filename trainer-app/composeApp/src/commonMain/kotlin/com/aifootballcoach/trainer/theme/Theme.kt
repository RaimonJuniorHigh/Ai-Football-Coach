package com.aifootballcoach.trainer.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/** Colour tokens taken from the "Trainer App v2" Claude design. */
object CoachColors {
    val Pitch = Color(0xFF2D4C2E)        // brand green: welcome background, selected chips
    val Lime = Color(0xFFDFEB7C)         // accent: primary action on green
    val Ink = Color(0xFF111912)          // near-black text and primary buttons
    val Paper = Color(0xFFF2F0EB)        // app background
    val Card = Color(0xFFFFFFFF)
    val Chip = Color(0xFFE6E3DC)         // unselected chips, avatars
    val Track = Color(0xFFDAD7D0)        // inactive progress segments
    val Muted = Color(0xFF6B6A64)        // secondary text
    val OnPitchMuted = Color(0xFFC9D3C6) // secondary text on green
}

private val Display = TextStyle(
    fontWeight = FontWeight.ExtraBold,
    letterSpacing = (-0.03).em,
)

val CoachTypography = Typography(
    displayMedium = Display.copy(fontSize = 38.sp, lineHeight = 40.sp),
    headlineLarge = Display.copy(fontSize = 30.sp, lineHeight = 33.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontSize = 15.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
    labelSmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
)

@Composable
fun CoachTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = CoachColors.Pitch,
            onPrimary = Color.White,
            secondary = CoachColors.Lime,
            onSecondary = CoachColors.Ink,
            background = CoachColors.Paper,
            onBackground = CoachColors.Ink,
            surface = CoachColors.Card,
            onSurface = CoachColors.Ink,
            surfaceVariant = CoachColors.Chip,
            onSurfaceVariant = CoachColors.Muted,
        ),
        typography = CoachTypography,
        content = content,
    )
}
