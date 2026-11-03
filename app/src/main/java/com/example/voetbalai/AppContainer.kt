package com.example.voetbalai

import android.content.Context
import com.example.voetbalai.data.prefs.PrefsOnboardingStore
import com.example.voetbalai.data.remote.SupabaseRemote
import com.example.voetbalai.data.repository.SupabaseStatsRepository
import com.example.voetbalai.data.repository.SupabaseTeamRepository
import com.example.voetbalai.domain.chat.ChatApi
import com.example.voetbalai.domain.chat.FakeChatApi
import com.example.voetbalai.domain.repository.PlayerStatsRepository
import com.example.voetbalai.domain.repository.TeamRepository

// UITLEG: AppContainer is de "lijmlaag". Hier kies ik welke onderdelen de app echt gebruikt.
// De ViewModels vragen alleen om een TeamRepository of ChatApi (interfaces),
// en hier bepaal ik welke echte versie erachter zit. Wil ik later een echte AI, dan verander ik
// alleen de regel met chatApi en blijft de rest van de app hetzelfde.

/** Plek waar de implementaties worden gekozen; vervang hier bijvoorbeeld de nep-chat door een echte AI. */
class AppContainer(context: Context) {
    // Hier kies ik de echte opslag: de Supabase-database (adres en sleutel staan in local.properties).
    // Team en stats praten met dezelfde verbinding en dus dezelfde (anonieme) gebruiker.
    private val remote = SupabaseRemote.create()
    val teamRepository: TeamRepository = SupabaseTeamRepository(remote, PrefsOnboardingStore(context))

    // De spelersstatistieken (cijfers 1-20 en kaartjes 1-99) komen ook uit de database.
    val statsRepository: PlayerStatsRepository = SupabaseStatsRepository(remote)

    // Hier kies ik de chat-API. Er is nog geen echte, dus gebruik ik tijdelijk het nepantwoord.
    // Zodra de API er is, zet ik hier mijn echte klasse neer.
    val chatApi: ChatApi = FakeChatApi()
}
