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

/** A team as stored in the database, with the id and invite code the app model doesn't carry. */
class SavedTeam(val id: Long, val team: Team, val inviteCode: String)

// Rows exactly as the tables in supabase/migrations name them (training_days, session_minutes, ...).
@Serializable
private data class TeamDto(
    val id: Long,
    val name: String,
    @SerialName("training_days") val trainingDays: List<String>,
    @SerialName("session_minutes") val sessionMinutes: Int,
    @SerialName("invite_code") val inviteCode: String,
) {
    // Days the app doesn't know are skipped, so one odd value can't crash the app.
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
        // The database also knows INJURED; the app only has In / Out, so anything but PRESENT counts as Out.
        availability = if (attendance == "PRESENT") Availability.In else Availability.Out,
    )
}

@Serializable
private data class NewPlayerDto(@SerialName("team_id") val teamId: Long, val name: String, val position: String)

// Rows of the views player_cards and player_stats, and of the tables drill_results and match_players.
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

// A match line of a player, with the match itself ("matches") as PostgREST sends it along.
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

// A stat the app doesn't know is skipped, so a new stat in the database can't crash the app.
private fun statTypeOrNull(code: String): StatType? = StatType.entries.firstOrNull { it.name == code }

/**
 * The real connection to the Supabase database (online or the local one from `supabase start`).
 * There is no login screen yet, so the first request signs in anonymously; Supabase remembers that
 * session and row level security then lets this user see only their own team.
 */
class SupabaseRemote(private val client: SupabaseClient) {

    // Several requests can start at once; the lock makes sure only one of them signs in.
    private val signInLock = Mutex()

    private suspend fun ensureSignedIn() = signInLock.withLock {
        // The saved session is loaded in the background at start-up; wait for it, or every app start would
        // sign in as a new anonymous user and lose the team.
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

    /** Replaces this trainer's team and players in one go (the `save_team` database function). */
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

    /** Overall (1–99) per player id; only players with training results are in there. */
    suspend fun fetchOveralls(): Map<Long, Int> {
        ensureSignedIn()
        return client.from("player_cards").select().decodeList<CardDto>()
            .mapNotNull { card -> card.overall?.let { card.playerId to it } }.toMap()
    }

    /** Stats of every player of this trainer's team, per player id. */
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
        /** [url] and [key] are the project URL and the public (publishable) key; never the secret key. */
        fun create(url: String, key: String) = SupabaseRemote(
            createSupabaseClient(url, key) {
                install(Auth)
                install(Postgrest)
            },
        )
    }
}
