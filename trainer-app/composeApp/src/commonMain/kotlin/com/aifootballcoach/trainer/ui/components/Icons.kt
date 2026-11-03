package com.aifootballcoach.trainer.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Small line icons used in the design (24×24 grid, 1.8 stroke).
 * Drawn here so the app doesn't need the large material-icons-extended artifact.
 */
object CoachIcons {
    val Home: ImageVector by lazy {
        lineIcon("Home") {
            moveTo(4f, 10.5f); lineTo(12f, 4f); lineTo(20f, 10.5f)
            lineTo(20f, 20f); lineTo(14.5f, 20f); lineTo(14.5f, 14f)
            lineTo(9.5f, 14f); lineTo(9.5f, 20f); lineTo(4f, 20f); close()
        }
    }

    val People: ImageVector by lazy {
        lineIcon("People") {
            circle(9f, 8f, 3.5f)
            moveTo(2.5f, 20f); curveTo(2.5f, 15.5f, 15.5f, 15.5f, 15.5f, 20f)
            moveTo(15.5f, 4.8f); curveTo(18.5f, 5.5f, 18.5f, 10.5f, 15.5f, 11.2f)
            moveTo(18f, 14.5f); curveTo(20.5f, 15.3f, 21.5f, 17.2f, 21.5f, 20f)
        }
    }

    val Sparkle: ImageVector by lazy {
        lineIcon("Sparkle") {
            moveTo(12f, 3f); curveTo(12.8f, 8.5f, 15.5f, 11.2f, 21f, 12f)
            curveTo(15.5f, 12.8f, 12.8f, 15.5f, 12f, 21f)
            curveTo(11.2f, 15.5f, 8.5f, 12.8f, 3f, 12f)
            curveTo(8.5f, 11.2f, 11.2f, 8.5f, 12f, 3f); close()
        }
    }

    val Calendar: ImageVector by lazy {
        lineIcon("Calendar") {
            moveTo(5f, 6f); lineTo(19f, 6f); lineTo(19f, 20f); lineTo(5f, 20f); close()
            moveTo(5f, 10f); lineTo(19f, 10f)
            moveTo(9f, 3.5f); lineTo(9f, 7.5f)
            moveTo(15f, 3.5f); lineTo(15f, 7.5f)
        }
    }

    val ChevronRight: ImageVector by lazy {
        lineIcon("ChevronRight") { moveTo(9.5f, 6f); lineTo(15.5f, 12f); lineTo(9.5f, 18f) }
    }

    val ChevronLeft: ImageVector by lazy {
        lineIcon("ChevronLeft") { moveTo(14.5f, 6f); lineTo(8.5f, 12f); lineTo(14.5f, 18f) }
    }

    val Close: ImageVector by lazy {
        lineIcon("Close") { moveTo(6.5f, 6.5f); lineTo(17.5f, 17.5f); moveTo(17.5f, 6.5f); lineTo(6.5f, 17.5f) }
    }

    val Check: ImageVector by lazy {
        lineIcon("Check") { moveTo(5f, 12.5f); lineTo(10f, 17.5f); lineTo(19f, 7f) }
    }

    val Shield: ImageVector by lazy {
        lineIcon("Shield") {
            moveTo(12f, 3f); lineTo(19f, 6f); lineTo(19f, 11f)
            curveTo(19f, 15.5f, 16f, 19f, 12f, 21f)
            curveTo(8f, 19f, 5f, 15.5f, 5f, 11f)
            lineTo(5f, 6f); close()
        }
    }

    val Plus: ImageVector by lazy {
        lineIcon("Plus") { moveTo(12f, 5f); lineTo(12f, 19f); moveTo(5f, 12f); lineTo(19f, 12f) }
    }
}

private fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
    moveTo(cx - r, cy)
    arcTo(r, r, 0f, isMoreThanHalf = true, isPositiveArc = true, x1 = cx + r, y1 = cy)
    arcTo(r, r, 0f, isMoreThanHalf = true, isPositiveArc = true, x1 = cx - r, y1 = cy)
    close()
}

private fun lineIcon(name: String, pathBuilder: PathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.8f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathBuilder = pathBuilder,
    ).build()
