package com.aifootballcoach.trainer

import com.aifootballcoach.trainer.data.Attention
import com.aifootballcoach.trainer.data.Availability
import com.aifootballcoach.trainer.data.DrillResult
import com.aifootballcoach.trainer.data.FocusSuggestion
import com.aifootballcoach.trainer.data.MatchAppearance
import com.aifootballcoach.trainer.data.PlannedSession
import com.aifootballcoach.trainer.data.Player
import com.aifootballcoach.trainer.data.PlayerProfile
import com.aifootballcoach.trainer.data.PlayerStat
import com.aifootballcoach.trainer.data.Position
import com.aifootballcoach.trainer.data.SessionBrief
import com.aifootballcoach.trainer.data.SessionDraft
import com.aifootballcoach.trainer.data.SessionPlanner
import com.aifootballcoach.trainer.data.SquadInsights
import com.aifootballcoach.trainer.data.StatType
import com.aifootballcoach.trainer.data.Team
import com.aifootballcoach.trainer.data.TeamRepository
import com.aifootballcoach.trainer.data.Weekday

/** Nepversie voor de tests, met een kleine vaste selectie. Zo kan ik de schermen tekenen zonder database. */
class FakeTeamRepository : TeamRepository {
    private val team = Team("JO17-1", setOf(Weekday.Tue, Weekday.Thu), 90)

    val squad = listOf(
        Player("1", "Daan Verhoeven", Position.Keeper),
        Player("2", "Sem Vossen", Position.Defence),
        Player("3", "Jesse Daniëls", Position.Midfield),
        Player("4", "Thijs Peeters", Position.Defence, Availability.Out),
        Player("5", "Lars de Wit", Position.Attack),
    )

    private val stats: Map<String, List<PlayerStat>> = mapOf(
        "2" to listOf(PlayerStat(StatType.DEFENDING, 3), PlayerStat(StatType.PASSING, 9), PlayerStat(StatType.PACE, 15)),
        "3" to listOf(PlayerStat(StatType.PASSING, 12), PlayerStat(StatType.STAMINA, 17)),
        "5" to listOf(PlayerStat(StatType.FINISHING, 18), PlayerStat(StatType.PACE, 7)),
    )

    private val overalls = mapOf("2" to 48, "3" to 66, "5" to 84)

    override suspend fun refresh() {}
    override fun loadTeam() = team
    override suspend fun saveTeam(team: Team) {}
    override fun teamLink() = "svbudel.app/join/k7Q2"
    override fun players() = squad
    override suspend fun addPlayer(name: String, position: Position) = Player("99", name, position)
    override suspend fun removePlayer(id: String) {}
    override fun overall(playerId: String) = overalls[playerId]
    override fun needsYou(): List<Attention> = SquadInsights.needsYou(squad, stats)

    override suspend fun playerProfile(playerId: String) = PlayerProfile(
        overall = overalls[playerId],
        stats = stats[playerId].orEmpty(),
        drills = listOf(
            DrillResult(StatType.DEFENDING, "Duels", attempts = 20, successes = 3, recordedOn = "2026-11-03"),
            DrillResult(StatType.PASSING, "Passes", attempts = 50, successes = 24, recordedOn = "2026-10-29"),
        ),
        matches = listOf(
            MatchAppearance("2026-11-01", "SV Budel", home = true, goalsFor = 2, goalsAgainst = 1, minutes = 90, goals = 1, assists = 0),
        ),
    )

    override suspend fun recordDrill(playerId: String, stat: StatType, attempts: Int, successes: Int) {}
    override fun nextSession() = PlannedSession("Donderdag", "Do 5 nov", minutes = 90)
    override fun focusSuggestions(): List<FocusSuggestion> = SquadInsights.focusSuggestions(stats)
    override fun nextSessionDraft(): SessionDraft? = null
    override fun draftSession(brief: SessionBrief) = SessionDraft(
        "Donderdag",
        "Do 5 nov",
        brief.focus.joinToString(", "),
        90,
        blocks = SessionPlanner.blocksFor(brief, available = 4, reasons = focusSuggestions().associate { it.title to it.reason }),
    )
    override fun sendDraft(draft: SessionDraft, homeExercises: Boolean) = Unit
}
