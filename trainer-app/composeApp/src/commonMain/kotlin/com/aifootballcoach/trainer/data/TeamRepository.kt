package com.aifootballcoach.trainer.data

/**
 * Alles wat de trainer-app weet over het team, de spelers en hun stats.
 * [SupabaseTeamRepository] is de echte uitvoering en leest uit de gedeelde Supabase-database.
 * Lezen gaat direct (uit het geheugen). Met [refresh] haal ik de nieuwste gegevens uit de database.
 */
interface TeamRepository {
    /** Haalt team, spelers en stats uit de database. */
    suspend fun refresh()

    fun loadTeam(): Team
    suspend fun saveTeam(team: Team)
    fun teamLink(): String

    fun players(): List<Player>
    suspend fun addPlayer(name: String, position: Position): Player
    suspend fun removePlayer(id: String)

    /** Het totaalcijfer (1–99) van de speler, of null zolang er nog geen oefeningen zijn. */
    fun overall(playerId: String): Int?

    /** Spelers met een heel lage stat, voor het lijstje "Vraagt aandacht" op het Vandaag-scherm. */
    fun needsYou(): List<Attention>

    suspend fun playerProfile(playerId: String): PlayerProfile

    /** Slaat een oefening op ([successes] van [attempts]). De database rekent de stats opnieuw uit. */
    suspend fun recordDrill(playerId: String, stat: StatType, attempts: Int, successes: Int)

    fun nextSession(): PlannedSession
    fun focusSuggestions(): List<FocusSuggestion>
    fun nextSessionDraft(): SessionDraft?

    /** Bewaart wat de trainer heeft ingevuld als concept voor de volgende training. Er zit nog geen assistent achter. */
    fun draftSession(brief: SessionBrief): SessionDraft
}
