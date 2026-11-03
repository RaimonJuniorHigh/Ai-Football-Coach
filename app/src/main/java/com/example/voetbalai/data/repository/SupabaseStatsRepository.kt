package com.example.voetbalai.data.repository

import com.example.voetbalai.data.remote.StatsRemoteDataSource
import com.example.voetbalai.domain.repository.PlayerStatsRepository
import com.example.voetbalai.domain.stats.PlayerCard
import com.example.voetbalai.domain.stats.PlayerProfile
import com.example.voetbalai.domain.stats.StatType

// UITLEG: De echte uitvoering van PlayerStatsRepository. Ik haal de gegevens bij de database op en zet ze in
// de modellen van de app. De cijfers (1-20 en 1-99) rekent de database uit; ik sorteer ze hier alleen.
class SupabaseStatsRepository(private val remote: StatsRemoteDataSource) : PlayerStatsRepository {

    // Per positie (keeper, verdediger, middenvelder, aanvaller) en daarbinnen het hoogste cijfer eerst.
    // Spelers zonder cijfer komen onderaan hun positie.
    override suspend fun playerCards(): List<PlayerCard> =
        remote.fetchCards().sortedWith(
            compareBy<PlayerCard> { it.position.ordinal }
                .thenByDescending { it.overall ?: -1 }
                .thenBy { it.name }
        )

    // Het profiel is een samenvoeging van vier aanvragen: kaartje, stats, oefeningen en wedstrijden.
    // Oefeningen en wedstrijden toon ik nieuwste eerst, en hoogstens de laatste tien.
    override suspend fun playerProfile(playerId: Long): PlayerProfile? {
        val card = remote.fetchCard(playerId) ?: return null
        return PlayerProfile(
            card = card,
            stats = remote.fetchStats(playerId).sortedBy { it.stat.ordinal },
            drills = remote.fetchDrills(playerId).sortedByDescending { it.recordedOn }.take(10),
            matches = remote.fetchMatches(playerId).sortedByDescending { it.playedOn }.take(10)
        )
    }

    // Bij een nieuw resultaat geef ik de uitleg van de oefening mee, zodat later te zien is wat er getraind is.
    override suspend fun recordDrill(playerId: Long, stat: StatType, attempts: Int, successes: Int) {
        require(attempts > 0) { "Aantal pogingen moet groter zijn dan 0" }
        require(successes in 0..attempts) { "Aantal gelukt moet tussen 0 en het aantal pogingen liggen" }
        remote.insertDrill(playerId, stat, stat.label, attempts, successes)
    }
}
