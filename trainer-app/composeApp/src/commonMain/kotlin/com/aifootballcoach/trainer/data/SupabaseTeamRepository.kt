package com.aifootballcoach.trainer.data

import kotlin.random.Random

/**
 * De echte [TeamRepository]: team, spelers, stats, oefeningen en wedstrijden komen uit de Supabase-database.
 * Bij een refresh haal ik alles voor de schermen op en bewaar ik het in het geheugen, zodat de schermen
 * niet hoeven te wachten. De cijfers (stat 1–20, totaal 1–99) rekent de database uit en niet ik.
 */
class SupabaseTeamRepository(private val remote: SupabaseRemote) : TeamRepository {

    private var teamId: Long? = null
    private var inviteCode = ""
    private var team = EMPTY_TEAM
    private var players: List<Player> = emptyList()
    private var overalls: Map<String, Int> = emptyMap()
    private var squadStats: Map<String, List<PlayerStat>> = emptyMap()
    private var draft: SessionDraft? = null

    override suspend fun refresh() {
        val saved = remote.fetchTeam()
        if (saved == null) {
            teamId = null
            inviteCode = ""
            team = EMPTY_TEAM
            players = emptyList()
            overalls = emptyMap()
            squadStats = emptyMap()
            return
        }
        val freshPlayers = remote.fetchPlayers(saved.id)
        val freshOveralls = remote.fetchOveralls()
        val freshStats = remote.fetchSquadStats()
        teamId = saved.id
        inviteCode = saved.inviteCode
        team = saved.team
        players = freshPlayers
        overalls = freshOveralls.mapKeys { it.key.toString() }
        squadStats = freshStats.mapKeys { it.key.toString() }
    }

    override fun loadTeam(): Team = team

    override suspend fun saveTeam(team: Team) {
        // Is er al een uitnodigingscode, dan houd ik die. Zo blijft de link die je al gedeeld hebt werken.
        if (inviteCode.isEmpty()) inviteCode = newInviteCode()
        remote.saveTeam(team, inviteCode, players)
        refresh()
    }

    override fun teamLink() = "svbudel.app/join/$inviteCode"

    override fun players(): List<Player> = players

    override suspend fun addPlayer(name: String, position: Position): Player {
        val id = teamId ?: error("Save the team before adding players")
        remote.insertPlayer(id, name.trim(), position)
        refresh()
        return players.last() // gesorteerd op id, dus de nieuwste speler staat laatst
    }

    override suspend fun removePlayer(id: String) {
        remote.deletePlayer(id.toLong())
        refresh()
    }

    override fun overall(playerId: String): Int? = overalls[playerId]

    override fun needsYou(): List<Attention> = SquadInsights.needsYou(players, squadStats)

    override suspend fun playerProfile(playerId: String): PlayerProfile {
        val id = playerId.toLong()
        return PlayerProfile(
            overall = overalls[playerId],
            stats = squadStats[playerId].orEmpty().sortedBy { it.stat.ordinal },
            drills = remote.fetchDrills(id),
            matches = remote.fetchMatches(id).sortedByDescending { it.playedOn }.take(10),
        )
    }

    override suspend fun recordDrill(playerId: String, stat: StatType, attempts: Int, successes: Int) {
        require(attempts > 0) { "Het aantal pogingen moet groter zijn dan 0" }
        require(successes in 0..attempts) { "Het aantal gelukt moet tussen 0 en het aantal pogingen liggen" }
        remote.insertDrill(playerId.toLong(), stat, stat.label, attempts, successes)
        refresh() // de database heeft de stats en het totaalcijfer opnieuw uitgerekend
    }

    override fun nextSession(): PlannedSession = SquadInsights.nextSession(now().date, team)

    override fun focusSuggestions(): List<FocusSuggestion> = SquadInsights.focusSuggestions(squadStats)

    override fun nextSessionDraft(): SessionDraft? = draft

    override fun draftSession(brief: SessionBrief): SessionDraft =
        SessionDraft(
            day = brief.session.day,
            dateLabel = brief.session.dateLabel,
            theme = brief.focus.joinToString(", "),
            minutes = brief.session.minutes,
        ).also { draft = it }

    private fun newInviteCode(): String {
        val alphabet = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789"
        return (1..4).map { alphabet[Random.nextInt(alphabet.length)] }.joinToString("")
    }

    private companion object {
        val EMPTY_TEAM = Team(name = "", trainingDays = emptySet(), sessionMinutes = 90)
    }
}
