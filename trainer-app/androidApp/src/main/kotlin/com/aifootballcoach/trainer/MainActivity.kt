package com.aifootballcoach.trainer

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.aifootballcoach.trainer.data.SupabaseRemote
import com.aifootballcoach.trainer.data.SupabaseTeamRepository

class MainActivity : ComponentActivity() {
    // The database address and public key come from local.properties (supabase.url and supabase.key).
    private val repository by lazy {
        SupabaseTeamRepository(SupabaseRemote.create(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_KEY))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            if (BuildConfig.SUPABASE_URL.isEmpty() || BuildConfig.SUPABASE_KEY.isEmpty()) {
                SetupProblemScreen("Zet supabase.url en supabase.key in local.properties en bouw de app opnieuw.")
                return@setContent
            }
            App(repository = repository, onDarkBackground = { dark ->
                val style = if (dark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            })
        }
    }
}
