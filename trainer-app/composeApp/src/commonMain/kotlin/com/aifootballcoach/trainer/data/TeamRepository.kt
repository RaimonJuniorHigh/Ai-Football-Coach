package com.aifootballcoach.trainer.data

/**
 * Source of team and player data for the trainer app.
 * Backed by [MockTeamRepository] until the shared backend/API exists.
 */
interface TeamRepository {
    fun loadTeam(): Team
    fun saveTeam(team: Team)
    fun players(): List<Player>
    fun addPlayer(name: String, position: Position): Player
    fun removePlayer(id: String)
}

class MockTeamRepository : TeamRepository {
    private var team = Team(
        name = "JO17-1",
        trainingDays = setOf(Weekday.Tue, Weekday.Thu),
        sessionMinutes = 90,
    )

    private val players = mutableListOf(
        Player("p1", "Daan Verhoeven", Position.Keeper),
        Player("p2", "Sem Vossen", Position.Defence),
        Player("p3", "Jesse Daniëls", Position.Midfield),
    )
    private var nextId = players.size + 1

    override fun loadTeam(): Team = team

    override fun saveTeam(team: Team) {
        this.team = team
    }

    override fun players(): List<Player> = players.toList()

    override fun addPlayer(name: String, position: Position): Player =
        Player("p${nextId++}", name.trim(), position).also { players += it }

    override fun removePlayer(id: String) {
        players.removeAll { it.id == id }
    }
}
