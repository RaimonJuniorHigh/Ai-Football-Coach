package com.example.voetbalai.domain.repository

import com.example.voetbalai.domain.stats.PlayerCard
import com.example.voetbalai.domain.stats.PlayerProfile
import com.example.voetbalai.domain.stats.StatType

// UITLEG: De belofte voor alles rond spelersstatistieken. Net als bij TeamRepository staat hier alleen WAT kan,
// en niet HOE. De echte uitvoering (met de Supabase-database) staat in data/repository/SupabaseStatsRepository.kt.

interface PlayerStatsRepository {
    /** De kaartjes (totaalcijfer 1-99) van alle spelers van het team, per positie en daarbinnen van hoog naar laag. */
    suspend fun playerCards(): List<PlayerCard>

    /** Het profiel van één speler (stats, oefeningen, wedstrijden), of null als de speler niet bestaat. */
    suspend fun playerProfile(playerId: Long): PlayerProfile?

    /** Legt een trainingsresultaat vast: [successes] van de [attempts] pogingen lukten. De stat wordt daarna vanzelf bijgewerkt. */
    suspend fun recordDrill(playerId: Long, stat: StatType, attempts: Int, successes: Int)
}
