package com.aifootballcoach.trainer.data

/**
 * Everything the trainer app knows about the team, its players and their stats.
 * [SupabaseTeamRepository] is the real implementation; it reads from the shared Supabase database.
 * Reads are instant (served from memory); call [refresh] to load the latest from the database.
 */
interface TeamRepository {
    /** Loads team, players and stats from the database. */
    suspend fun refresh()

    fun loadTeam(): Team
    suspend fun saveTeam(team: Team)
    fun teamLink(): String

    fun players(): List<Player>
    suspend fun addPlayer(name: String, position: Position): Player
    suspend fun removePlayer(id: String)

    /** The player's overall (1–99), or null while there are no training results yet. */
    fun overall(playerId: String): Int?

    /** Players with a very low stat, for the "needs attention" list on the Today screen. */
    fun needsYou(): List<Attention>

    suspend fun playerProfile(playerId: String): PlayerProfile

    /** Saves a training result ([successes] of [attempts]); the database recalculates the stats. */
    suspend fun recordDrill(playerId: String, stat: StatType, attempts: Int, successes: Int)

    fun nextSession(): PlannedSession
    fun focusSuggestions(): List<FocusSuggestion>
    fun nextSessionDraft(): SessionDraft?

    /** Keeps the trainer's brief as the draft for the next session. There is no assistant (AI) behind this yet. */
    fun draftSession(brief: SessionBrief): SessionDraft
}
