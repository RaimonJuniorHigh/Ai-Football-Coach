package com.example.voetbalai

import com.example.voetbalai.data.repository.SupabaseTeamRepository
import com.example.voetbalai.domain.model.Attendance
import com.example.voetbalai.domain.model.Day
import com.example.voetbalai.domain.model.Player
import com.example.voetbalai.domain.model.Position
import com.example.voetbalai.domain.model.Team
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SupabaseTeamRepositoryTest {
    private val remote = FakeTeamRemote()
    private val onboarding = FakeOnboardingStore()
    private val repository = SupabaseTeamRepository(remote, onboarding)

    private val team = Team(0, "JO15-2", setOf(Day.MON, Day.WED), 60, "abcd")

    @Test fun initialStateIsEmptyUntilRefresh() = runTest {
        assertNull(repository.team.first())
        assertTrue(repository.players.first().isEmpty())
        repository.refresh()
        assertNull(repository.team.first())
    }

    @Test fun saveTeamStoresTeamAndPlayers() = runTest {
        repository.saveTeam(team, listOf(Player(-1, "Bram", Position.ATTACK)))
        val saved = repository.currentTeam()!!
        assertEquals("JO15-2", saved.team.name)
        assertEquals(setOf(Day.MON, Day.WED), saved.team.trainingDays)
        assertEquals(60, saved.team.sessionMinutes)
        assertEquals(listOf("Bram"), saved.players.map { it.name })
        assertEquals(saved.team, repository.team.first())
    }

    @Test fun saveTeamReplacesExistingTeamAndItsPlayers() = runTest {
        repository.saveTeam(team, listOf(Player(-1, "Bram", Position.ATTACK)))
        repository.saveTeam(team.copy(name = "JO13-1"), listOf(Player(-1, "Sem", Position.KEEPER)))
        assertEquals("JO13-1", repository.team.first()?.name)
        assertEquals(listOf("Sem"), repository.players.first().map { it.name })
    }

    @Test fun setAttendanceOnlyChangesThatPlayer() = runTest {
        repository.saveTeam(team, listOf(Player(-1, "Bram", Position.ATTACK), Player(-2, "Sem", Position.KEEPER)))
        val bram = repository.players.first().first { it.name == "Bram" }
        repository.setAttendance(bram.id, Attendance.INJURED)
        val players = repository.players.first()
        assertEquals(Attendance.INJURED, players.first { it.name == "Bram" }.attendance)
        assertEquals(Attendance.PRESENT, players.first { it.name == "Sem" }.attendance)
    }

    @Test fun addPlayerAddsToCurrentTeamAndTrimsName() = runTest {
        repository.saveTeam(team, emptyList())
        repository.addPlayer("  Milan ", Position.KEEPER)
        val player = repository.players.first().single()
        assertEquals("Milan", player.name)
        assertEquals(Position.KEEPER, player.position)
        assertEquals(Attendance.PRESENT, player.attendance)
    }

    @Test fun addPlayerWithoutTeamDoesNothing() = runTest {
        repository.addPlayer("Milan", Position.KEEPER)
        assertTrue(repository.players.first().isEmpty())
    }

    @Test fun resetDatabaseClearsEverythingAndOnboardingFlag() = runTest {
        repository.saveTeam(team, fullSquad)
        repository.markOnboarded()
        repository.resetDatabase()
        assertNull(repository.team.first())
        assertTrue(repository.players.first().isEmpty())
        assertFalse(repository.isOnboarded())
    }

    @Test fun createDemoTeamTwiceGivesNoDuplicatePlayers() = runTest {
        repository.createDemoTeam()
        repository.createDemoTeam()
        assertEquals(16, repository.players.first().size)
        assertEquals("JO17-1", repository.team.first()?.name)
    }

    @Test fun clubListComesFromTheDatabase() = runTest {
        assertEquals(listOf("Milan Brouwer", "Noah Peeters"), repository.clubList().map { it.name })
    }

    @Test fun markOnboardedIsRemembered() {
        assertFalse(repository.isOnboarded())
        repository.markOnboarded()
        assertTrue(repository.isOnboarded())
    }

    @Test fun databaseErrorsReachTheCaller() = runTest {
        remote.failWith = IllegalStateException("geen internet")
        val error = runCatching { repository.refresh() }.exceptionOrNull()
        assertEquals("geen internet", error?.message)
    }
}
