package com.example.voetbalai

import com.example.voetbalai.domain.model.Attendance
import com.example.voetbalai.domain.model.ClubPlayer
import com.example.voetbalai.domain.model.Day
import com.example.voetbalai.domain.model.Player
import com.example.voetbalai.domain.model.Position
import com.example.voetbalai.domain.model.Team
import com.example.voetbalai.domain.model.TeamWithPlayers
import com.example.voetbalai.domain.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeTeamRepository(
    team: Team? = Team(1, "JO17-1", setOf(Day.TUE, Day.THU), 90, "k7Q2"),
    players: List<Player> = listOf(
        Player(1, "Daan Verhoeven", Position.KEEPER),
        Player(2, "Sem Vossen", Position.DEFENCE),
        Player(3, "Jesse Daniëls", Position.MIDFIELD),
        Player(4, "Lars de Wit", Position.ATTACK),
        Player(5, "Milan Brouwer", Position.DEFENCE),
        Player(6, "Noah Peeters", Position.MIDFIELD)
    )
) : TeamRepository {
    private val teamState = MutableStateFlow(team)
    private val playerState = MutableStateFlow(players)
    private var nextId = 100L
    var onboarded = false
    var savedTeam: Team? = null
    var savedPlayers: List<Player> = emptyList()

    override val team = teamState
    override val players = playerState.map { it }

    override suspend fun refresh() {}

    override suspend fun currentTeam() = teamState.value?.let { TeamWithPlayers(it, playerState.value) }

    override suspend fun saveTeam(team: Team, players: List<Player>) {
        savedTeam = team
        savedPlayers = players
        teamState.value = team
        playerState.value = players
    }

    override suspend fun setAttendance(playerId: Long, attendance: Attendance) {
        playerState.value = playerState.value.map { if (it.id == playerId) it.copy(attendance = attendance) else it }
    }

    override suspend fun addPlayer(name: String, position: Position) {
        playerState.value = playerState.value + Player(nextId++, name.trim(), position)
    }

    override suspend fun resetDatabase() {
        teamState.value = null
        playerState.value = emptyList()
        onboarded = false
    }

    override suspend fun createDemoTeam() {
        teamState.value = Team(1, "JO17-1", setOf(Day.TUE, Day.THU), 90, "k7Q2")
        playerState.value = fullSquad
    }

    override suspend fun clubList() = listOf(
        ClubPlayer("Finn Smeets", Position.DEFENCE),
        ClubPlayer("Luuk Jacobs", Position.MIDFIELD),
        ClubPlayer("Noah Peeters", Position.MIDFIELD)
    )

    override fun isOnboarded() = onboarded
    override fun markOnboarded() { onboarded = true }
}

/** Een geldige selectie volgens [com.example.voetbalai.domain.TeamRules]: 16 spelers. */
val fullSquad: List<Player> = listOf(
    Position.KEEPER to 2, Position.DEFENCE to 5, Position.MIDFIELD to 5, Position.ATTACK to 4
).flatMap { (position, aantal) -> (1..aantal).map { position to it } }
    .mapIndexed { i, (position, nr) -> Player(i + 1L, "${position.name.lowercase().replaceFirstChar { it.uppercase() }} $nr", position) }
