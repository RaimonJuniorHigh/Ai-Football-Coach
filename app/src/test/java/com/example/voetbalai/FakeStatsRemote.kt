package com.example.voetbalai

import com.example.voetbalai.data.remote.StatsRemoteDataSource
import com.example.voetbalai.domain.model.Position
import com.example.voetbalai.domain.stats.DrillResult
import com.example.voetbalai.domain.stats.MatchAppearance
import com.example.voetbalai.domain.stats.PlayerCard
import com.example.voetbalai.domain.stats.PlayerStat
import com.example.voetbalai.domain.stats.StatType

/** Nepversie van de stats in de online database; houdt alles in het geheugen. */
class FakeStatsRemote : StatsRemoteDataSource {
    var cards = listOf(
        PlayerCard(1, "Lars de Wit", Position.ATTACK, 41),
        PlayerCard(2, "Rick Bos", Position.ATTACK, 61),
        PlayerCard(3, "Daan Verhoeven", Position.KEEPER, 73),
        PlayerCard(4, "Nieuwe Speler", Position.ATTACK, null)
    )
    var stats = mapOf(1L to listOf(PlayerStat(StatType.PACE, 7), PlayerStat(StatType.FINISHING, 10)))
    var drills = mutableListOf<Pair<Long, DrillResult>>()
    var matches = mapOf(
        1L to listOf(
            MatchAppearance("2026-10-13", "SV Sint-Joris", true, 5, 1, 90, 1, 0),
            MatchAppearance("2026-10-27", "Rapid Boys", true, 2, 2, 45, 0, 1)
        )
    )
    var failWith: Exception? = null

    private fun check() { failWith?.let { throw it } }

    override suspend fun fetchCards(): List<PlayerCard> { check(); return cards }

    override suspend fun fetchCard(playerId: Long): PlayerCard? { check(); return cards.firstOrNull { it.playerId == playerId } }

    override suspend fun fetchStats(playerId: Long): List<PlayerStat> { check(); return stats[playerId].orEmpty() }

    override suspend fun fetchDrills(playerId: Long): List<DrillResult> {
        check()
        return drills.filter { it.first == playerId }.map { it.second }
    }

    override suspend fun fetchMatches(playerId: Long): List<MatchAppearance> { check(); return matches[playerId].orEmpty() }

    override suspend fun insertDrill(playerId: Long, stat: StatType, drill: String, attempts: Int, successes: Int) {
        check()
        drills.add(playerId to DrillResult(stat, drill, attempts, successes, "2026-11-03"))
    }
}
