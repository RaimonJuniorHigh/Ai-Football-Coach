package com.example.voetbalai.data.remote

import com.example.voetbalai.domain.stats.DrillResult
import com.example.voetbalai.domain.stats.MatchAppearance
import com.example.voetbalai.domain.stats.PlayerCard
import com.example.voetbalai.domain.stats.PlayerStat
import com.example.voetbalai.domain.stats.StatType

// UITLEG: De lijst met dingen die de app over stats aan de online database kan vragen (alleen de beloftes).
// De echte uitvoering staat in SupabaseRemote.kt; in de tests gebruik ik een nepversie in het geheugen.
interface StatsRemoteDataSource {
    suspend fun fetchCards(): List<PlayerCard>

    suspend fun fetchCard(playerId: Long): PlayerCard?

    suspend fun fetchStats(playerId: Long): List<PlayerStat>

    suspend fun fetchDrills(playerId: Long): List<DrillResult>

    suspend fun fetchMatches(playerId: Long): List<MatchAppearance>

    suspend fun insertDrill(playerId: Long, stat: StatType, drill: String, attempts: Int, successes: Int)
}
