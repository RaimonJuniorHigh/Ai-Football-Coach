package com.example.voetbalai

import com.example.voetbalai.data.prefs.OnboardingStore
import com.example.voetbalai.data.remote.TeamRemoteDataSource
import com.example.voetbalai.domain.model.Attendance
import com.example.voetbalai.domain.model.ClubPlayer
import com.example.voetbalai.domain.model.Day
import com.example.voetbalai.domain.model.Player
import com.example.voetbalai.domain.model.Position
import com.example.voetbalai.domain.model.Team

/** Nepversie van de online database: houdt alles in het geheugen, zodat de repository zonder internet te testen is. */
class FakeTeamRemote : TeamRemoteDataSource {
    private var team: Team? = null
    private var players: List<Player> = emptyList()
    private var nextTeamId = 1L
    private var nextPlayerId = 1L
    var failWith: Exception? = null
    var calls = 0

    private fun check() {
        calls++
        failWith?.let { throw it }
    }

    override suspend fun fetchTeam(): Team? { check(); return team }

    override suspend fun fetchPlayers(teamId: Long): List<Player> {
        check()
        return if (team?.id == teamId) players else emptyList()
    }

    // Net als de echte functie save_team: eerst het oude team weg, dan het nieuwe met nieuwe id's.
    override suspend fun saveTeam(team: Team, players: List<Player>) {
        check()
        this.team = team.copy(id = nextTeamId++)
        this.players = players.map { it.copy(id = nextPlayerId++) }
    }

    override suspend fun updateAttendance(playerId: Long, attendance: Attendance) {
        check()
        players = players.map { if (it.id == playerId) it.copy(attendance = attendance) else it }
    }

    override suspend fun insertPlayer(teamId: Long, name: String, position: Position) {
        check()
        players = players + Player(nextPlayerId++, name, position)
    }

    override suspend fun deleteAllTeams() {
        check()
        team = null
        players = emptyList()
    }

    override suspend fun createDemoTeam() {
        saveTeam(Team(0, "JO17-1", setOf(Day.TUE, Day.THU), 90, "demo"), fullSquad)
    }

    override suspend fun fetchClubPlayers(): List<ClubPlayer> {
        check()
        return listOf(ClubPlayer("Milan Brouwer", Position.KEEPER), ClubPlayer("Noah Peeters", Position.DEFENCE))
    }
}

class FakeOnboardingStore : OnboardingStore {
    private var value = false
    override fun isOnboarded() = value
    override fun setOnboarded(value: Boolean) { this.value = value }
}
