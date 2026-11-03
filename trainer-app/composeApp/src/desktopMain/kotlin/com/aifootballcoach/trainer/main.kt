package com.aifootballcoach.trainer

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.aifootballcoach.trainer.data.SupabaseRemote
import com.aifootballcoach.trainer.data.SupabaseTeamRepository

/**
 * Desktop preview of the trainer app, sized like a phone. Run with `./gradlew :composeApp:run`.
 * The database address and public key come from the SUPABASE_URL and SUPABASE_KEY environment variables.
 */
fun main() = application {
    val url = System.getenv("SUPABASE_URL").orEmpty()
    val key = System.getenv("SUPABASE_KEY").orEmpty()
    Window(
        onCloseRequest = ::exitApplication,
        title = "AI Football Coach — Trainer",
        state = rememberWindowState(width = 390.dp, height = 844.dp),
    ) {
        if (url.isEmpty() || key.isEmpty()) {
            SetupProblemScreen("Zet de omgevingsvariabelen SUPABASE_URL en SUPABASE_KEY.")
        } else {
            App(SupabaseTeamRepository(SupabaseRemote.create(url, key)))
        }
    }
}
