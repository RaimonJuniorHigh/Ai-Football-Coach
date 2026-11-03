package com.aifootballcoach.trainer.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Een team zoals het in de database staat, met het id en de uitnodigingscode die het model van de app niet heeft. */
class SavedTeam(val id: Long, val team: Team, val inviteCode: String)

// Rijen precies zoals de tabellen in supabase/migrations ze noemen (training_days, session_minutes, ...).
@Serializable
private data class TeamDto(
    val id: Long,
    val name: String,
    @SerialName("training_days") val trainingDays: List<String>,
    @SerialName("session_minutes") val sessionMinutes: Int,
    @SerialName("invite_code") val inviteCode: String,
) {
    // Een dag die de app niet kent sla ik over, zodat één vreemde waarde de app niet laat crashen.
    fun toSaved() = SavedTeam(
        id = id,
        team = Team(
            name = name,
            trainingDays = trainingDays.mapNotNull { day -> Weekday.entries.firstOrNull { it.name.uppercase() == day } }.toSet(),
            sessionMinutes = sessionMinutes,
        ),
        inviteCode = inviteCode,
    )
}

@Serializable
private data class PlayerDto(val id: Long, val name: String, val position: String, val attendance: String) {
    fun toPlayer() = Player(
        id = id.toString(),
        name = name,
        position = Position.entries.first { it.name.uppercase() == position },
        // De database kent ook INJURED, maar de app heeft alleen In en Uit. Alles behalve PRESENT telt dus als Uit.
        availability = if (attendance == "PRESENT") Availability.In else Availability.Out,
    )
}

@Serializable
private data class NewPlayerDto(@SerialName("team_id") val teamId: Long, val name: String, val position: String)

// Rijen van de views player_cards en player_stats en van de tabellen drill_results en match_players.
@Serializable
private data class CardDto(@SerialName("player_id") val playerId: Long, val overall: Int? = null)

@Serializable
private data class StatDto(@SerialName("player_id") val playerId: Long, val stat: String, val rating: Int)

@Serializable
private data class DrillDto(
    val stat: String,
    val drill: String,
    val attempts: Int,
    val successes: Int,
    @SerialName("recorded_on") val recordedOn: String,
)

@Serializable
private data class NewDrillDto(
    @SerialName("player_id") val playerId: Long,
    val stat: String,
    val drill: String,
    val attempts: Int,
    val successes: Int,
)

// Een wedstrijdregel van een speler, met de wedstrijd zelf ("matches") erbij zoals PostgREST die meestuurt.
@Serializable
private data class MatchPlayerDto(val minutes: Int, val goals: Int, val assists: Int, val matches: MatchDto)

@Serializable
private data class MatchDto(
    @SerialName("played_on") val playedOn: String,
    val opponent: String,
    val home: Boolean,
    @SerialName("goals_for") val goalsFor: Int,
    @SerialName("goals_against") val goalsAgainst: Int,
)

// Een stat die de app niet kent sla ik over, zodat een nieuwe stat in de database de app niet laat crashen.
private fun statTypeOrNull(code: String): StatType? = StatType.entries.firstOrNull { it.name == code }

/**
 * De echte verbinding met de Supabase-database (online, of de lokale van `supabase start`).
 * Er is nog geen inlogscherm. Daarom log ik bij de eerste vraag anoniem in. Supabase onthoudt die sessie,
 * en door de beveiliging (row level security) ziet deze gebruiker alleen zijn eigen team.
 */
class SupabaseRemote(private val client: SupabaseClient) {

    // Er kunnen meerdere vragen tegelijk starten. Het slot zorgt dat er maar één inlogt.
    private val signInLock = Mutex()

    private suspend fun ensureSignedIn() = signInLock.withLock {
        // De opgeslagen sessie wordt bij het starten op de achtergrond geladen. Ik wacht daarop, anders log ik
        // bij elke start in als een nieuwe anonieme gebruiker en is het team weg.
        client.auth.awaitInitialization()
        if (client.auth.currentSessionOrNull() == null) client.auth.signInAnonymously()
    }

    suspend fun fetchTeam(): SavedTeam? {
        ensureSignedIn()
        return client.from("teams").select {
            order("id", Order.DESCENDING)
            limit(1)
        }.decodeList<TeamDto>().firstOrNull()?.toSaved()
    }

    suspend fun fetchPlayers(teamId: Long): List<Player> {
        ensureSignedIn()
        return client.from("players").select {
            filter { eq("team_id", teamId) }
            order("id", Order.ASCENDING)
        }.decodeList<PlayerDto>().map { it.toPlayer() }
    }

    /** Slaat het team op via de database-functie `save_team`. Bestaat het team al, dan blijven de spelers staan. */
    suspend fun saveTeam(team: Team, inviteCode: String, players: List<Player>) {
        ensureSignedIn()
        val params = buildJsonObject {
            put("p_name", team.name)
            put("p_training_days", JsonArray(Weekday.entries.filter { it in team.trainingDays }.map { JsonPrimitive(it.name.uppercase()) }))
            put("p_session_minutes", team.sessionMinutes)
            put("p_invite_code", inviteCode)
            put("p_players", buildJsonArray {
                players.forEach { p ->
                    add(buildJsonObject {
                        put("name", p.name)
                        put("position", p.position.name.uppercase())
                        put("attendance", if (p.availability == Availability.In) "PRESENT" else "ABSENT")
                    })
                }
            })
        }
        client.postgrest.rpc("save_team", params)
    }

    suspend fun insertPlayer(teamId: Long, name: String, position: Position) {
        ensureSignedIn()
        client.from("players").insert(NewPlayerDto(teamId, name, position.name.uppercase()))
    }

    suspend fun deletePlayer(playerId: Long) {
        ensureSignedIn()
        client.from("players").delete { filter { eq("id", playerId) } }
    }

    /** Het totaalcijfer (1–99) per speler. Alleen spelers met oefeningen staan erin. */
    suspend fun fetchOveralls(): Map<Long, Int> {
        ensureSignedIn()
        return client.from("player_cards").select().decodeList<CardDto>()
            .mapNotNull { card -> card.overall?.let { card.playerId to it } }.toMap()
    }

    /** De stats van alle spelers van het team, per speler. */
    suspend fun fetchSquadStats(): Map<Long, List<PlayerStat>> {
        ensureSignedIn()
        return client.from("player_stats").select().decodeList<StatDto>()
            .mapNotNull { row -> statTypeOrNull(row.stat)?.let { row.playerId to PlayerStat(it, row.rating) } }
            .groupBy({ it.first }, { it.second })
    }

    suspend fun fetchDrills(playerId: Long): List<DrillResult> {
        ensureSignedIn()
        return client.from("drill_results").select {
            filter { eq("player_id", playerId) }
            order("recorded_on", Order.DESCENDING)
            limit(10)
        }.decodeList<DrillDto>().mapNotNull { dto ->
            statTypeOrNull(dto.stat)?.let { DrillResult(it, dto.drill, dto.attempts, dto.successes, dto.recordedOn) }
        }
    }

    suspend fun fetchMatches(playerId: Long): List<MatchAppearance> {
        ensureSignedIn()
        return client.from("match_players").select(
            Columns.raw("minutes, goals, assists, matches(played_on, opponent, home, goals_for, goals_against)"),
        ) {
            filter { eq("player_id", playerId) }
        }.decodeList<MatchPlayerDto>().map {
            MatchAppearance(
                playedOn = it.matches.playedOn,
                opponent = it.matches.opponent,
                home = it.matches.home,
                goalsFor = it.matches.goalsFor,
                goalsAgainst = it.matches.goalsAgainst,
                minutes = it.minutes,
                goals = it.goals,
                assists = it.assists,
            )
        }
    }

    suspend fun insertDrill(playerId: Long, stat: StatType, drill: String, attempts: Int, successes: Int) {
        ensureSignedIn()
        client.from("drill_results").insert(NewDrillDto(playerId, stat.name, drill, attempts, successes))
    }

    companion object {
        /** [url] is het adres van het project en [key] de publieke (publishable) sleutel. Nooit de geheime sleutel gebruiken. */
        fun create(url: String, key: String) = SupabaseRemote(
            createSupabaseClient(url, key) {
                install(Auth)
                install(Postgrest)
            },
        )
    }
}
