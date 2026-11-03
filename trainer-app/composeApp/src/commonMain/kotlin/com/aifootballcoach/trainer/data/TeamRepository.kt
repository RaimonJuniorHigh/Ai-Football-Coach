package com.aifootballcoach.trainer.data

import kotlin.math.round

/**
 * Source of team and player data for the trainer app.
 * Backed by [MockTeamRepository] until the shared backend/API exists.
 */
interface TeamRepository {
    fun trainer(): Trainer
    fun loadTeam(): Team
    fun saveTeam(team: Team)
    fun teamLink(): String
    fun players(): List<Player>
    fun addPlayer(name: String, position: Position): Player
    fun removePlayer(id: String)
    fun nextSessionDraft(): SessionDraft?
    fun needsYou(): List<Attention>
    fun playerStats(playerId: String): PlayerStats
    fun developmentPoints(playerId: String): List<DevelopmentPoint>
    fun addDevelopmentPoint(playerId: String, title: String, category: PointCategory, score: Int): DevelopmentPoint
    /** Saves the trainer's 1–5 rating after a session and updates the point's score. */
    fun logObservation(pointId: String, rating: Int, note: String)
}

class MockTeamRepository : TeamRepository {
    private var team = Team(
        name = "JO17-1",
        trainingDays = setOf(Weekday.Tue, Weekday.Thu),
        sessionMinutes = 90,
    )

    private val players = mutableListOf(
        Player("p1", "Daan Verhoeven", Position.Keeper),
        Player("p2", "Sem Vossen", Position.Defence, "Rechtsback", flag = PlayerFlag.Stalled, age = 16, shirtNumber = 2),
        Player("p3", "Jesse Daniëls", Position.Midfield, flag = PlayerFlag.Review),
        Player("p4", "Thijs Peeters", Position.Defence, "Centrale verdediger", Availability.Out),
        Player("p5", "Ruben Geurts", Position.Defence, "Centrale verdediger"),
        Player("p6", "Bram Maas", Position.Midfield),
        Player("p7", "Mees Koolen", Position.Attack, "Linksbuiten", flag = PlayerFlag.Stalled),
        Player("p8", "Luuk Bekkers", Position.Attack, "Spits"),
        Player("p9", "Noah Hendrikx", Position.Defence, "Linksback", Availability.Out),
        Player("p10", "Finn van Dijk", Position.Midfield, flag = PlayerFlag.Stalled),
        Player("p11", "Lars Jansen", Position.Attack, "Rechtsbuiten"),
        Player("p12", "Milan Smeets", Position.Midfield, availability = Availability.Out),
        Player("p13", "Stijn Willems", Position.Defence, "Centrale verdediger", flag = PlayerFlag.Stalled),
        Player("p14", "Tim Raaijmakers", Position.Keeper),
        Player("p15", "Jens Coolen", Position.Midfield, flag = PlayerFlag.Stalled),
        Player("p16", "Bas Vermeulen", Position.Attack, "Spits", Availability.Out),
        Player("p17", "Gijs Hermans", Position.Defence, "Rechtsback"),
        Player("p18", "Teun Martens", Position.Midfield, flag = PlayerFlag.Stalled),
    )
    private var nextId = players.size + 1

    private val points = mutableListOf(
        DevelopmentPoint("d1", "p2", "1-tegen-1 verdedigen", PointCategory.Defending, 2.8, change = 0.0, weeksOpen = 5),
        DevelopmentPoint("d2", "p2", "Lange diagonaal, zwakke voet", PointCategory.Passing, 3.4, change = 0.6, weeksOpen = 3),
        DevelopmentPoint("d3", "p2", "Timing van de overlap", PointCategory.Speed, 1.9, change = null, weeksOpen = 1),
        DevelopmentPoint("d4", "p7", "Eerste aanname onder druk", PointCategory.BallControl, 2.5, change = 0.0, weeksOpen = 4),
        DevelopmentPoint("d5", "p10", "Scanning voor de aanname", PointCategory.BallControl, 2.2, change = 0.0, weeksOpen = 6),
        DevelopmentPoint("d6", "p3", "Steekpass tussen de linies", PointCategory.Passing, 3.1, change = 0.3, weeksOpen = 2),
    )
    private var nextPointId = points.size + 1

    override fun trainer() = Trainer(firstName = "Tom", lastName = "Wijnen")

    override fun loadTeam(): Team = team

    override fun saveTeam(team: Team) {
        this.team = team
    }

    override fun teamLink() = "svbudel.app/join/k7Q2"

    override fun players(): List<Player> = players.toList()

    override fun addPlayer(name: String, position: Position): Player =
        Player("p${nextId++}", name.trim(), position).also { players += it }

    override fun removePlayer(id: String) {
        players.removeAll { it.id == id }
    }

    override fun nextSessionDraft() = SessionDraft(
        day = "Donderdag",
        time = "19:00",
        theme = "Druk zetten & omschakelen",
        minutes = 90,
    )

    override fun needsYou(): List<Attention> =
        players.filter { it.id == "p2" }.map { Attention(it, "1-tegen-1 verdedigen — 5 weken stil") }

    override fun playerStats(playerId: String): PlayerStats {
        val player = players.first { it.id == playerId }
        return when {
            playerId == "p2" -> PlayerStats(sessions = 11, attendancePercent = 92)
            player.availability == Availability.Out -> PlayerStats(sessions = 8, attendancePercent = 67)
            else -> PlayerStats(sessions = 11, attendancePercent = 88)
        }
    }

    override fun developmentPoints(playerId: String) = points.filter { it.playerId == playerId }

    override fun addDevelopmentPoint(playerId: String, title: String, category: PointCategory, score: Int) =
        DevelopmentPoint("d${nextPointId++}", playerId, title.trim(), category, score.toDouble(), change = null, weeksOpen = 0)
            .also { points += it }

    override fun logObservation(pointId: String, rating: Int, note: String) {
        val index = points.indexOfFirst { it.id == pointId }
        if (index < 0) return
        val point = points[index]
        // Mock scoring: new readings count for a third, so one session doesn't swing the score.
        val score = round((point.score * 2 + rating) / 3 * 10) / 10
        points[index] = point.copy(score = score, change = score - point.score)
    }
}
