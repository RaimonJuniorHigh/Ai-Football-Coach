package com.aifootballcoach.trainer.data

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
}

class MockTeamRepository : TeamRepository {
    private var team = Team(
        name = "JO17-1",
        trainingDays = setOf(Weekday.Tue, Weekday.Thu),
        sessionMinutes = 90,
    )

    private val players = mutableListOf(
        Player("p1", "Daan Verhoeven", Position.Keeper),
        Player("p2", "Sem Vossen", Position.Defence, "Rechtsback", flag = PlayerFlag.Stalled),
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
}
