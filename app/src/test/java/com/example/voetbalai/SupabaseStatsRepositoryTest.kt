package com.example.voetbalai

import com.example.voetbalai.data.repository.SupabaseStatsRepository
import com.example.voetbalai.domain.model.Position
import com.example.voetbalai.domain.stats.StatType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SupabaseStatsRepositoryTest {
    private val remote = FakeStatsRemote()
    private val repository = SupabaseStatsRepository(remote)

    @Test fun cardsAreSortedByPositionThenOverallHighToLow() = runTest {
        val cards = repository.playerCards()
        assertEquals(listOf("Daan Verhoeven", "Rick Bos", "Lars de Wit", "Nieuwe Speler"), cards.map { it.name })
        assertEquals(Position.KEEPER, cards.first().position)
    }

    @Test fun playerWithoutTrainingHasNoOverall() = runTest {
        assertNull(repository.playerCards().first { it.name == "Nieuwe Speler" }.overall)
    }

    @Test fun profileCombinesCardStatsAndMatches() = runTest {
        val profile = repository.playerProfile(1)!!
        assertEquals("Lars de Wit", profile.card.name)
        assertEquals(listOf(StatType.FINISHING, StatType.PACE), profile.stats.map { it.stat })
        assertEquals(listOf("2026-10-27", "2026-10-13"), profile.matches.map { it.playedOn })
    }

    @Test fun unknownPlayerHasNoProfile() = runTest {
        assertNull(repository.playerProfile(999))
    }

    @Test fun recordDrillStoresTheResult() = runTest {
        repository.recordDrill(1, StatType.FINISHING, attempts = 100, successes = 100)
        val drill = repository.playerProfile(1)!!.drills.single()
        assertEquals(100, drill.successes)
        assertEquals(StatType.FINISHING, drill.stat)
    }

    @Test fun recordDrillRejectsImpossibleNumbers() = runTest {
        assertTrue(runCatching { repository.recordDrill(1, StatType.FINISHING, 0, 0) }.isFailure)
        assertTrue(runCatching { repository.recordDrill(1, StatType.FINISHING, 10, 11) }.isFailure)
        assertTrue(runCatching { repository.recordDrill(1, StatType.FINISHING, 10, -1) }.isFailure)
    }

    @Test fun databaseErrorsReachTheCaller() = runTest {
        remote.failWith = IllegalStateException("geen internet")
        assertEquals("geen internet", runCatching { repository.playerCards() }.exceptionOrNull()?.message)
    }
}
