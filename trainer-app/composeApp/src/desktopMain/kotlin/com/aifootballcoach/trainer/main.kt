package com.aifootballcoach.trainer

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

/** Desktop preview of the trainer app, sized like a phone. Run with `./gradlew :composeApp:run`. */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "AI Football Coach — Trainer",
        state = rememberWindowState(width = 390.dp, height = 844.dp),
    ) {
        App()
    }
}
