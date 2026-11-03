package com.example.voetbalai.data.remote

import com.example.voetbalai.BuildConfig
import com.example.voetbalai.domain.model.Attendance
import com.example.voetbalai.domain.model.ClubPlayer
import com.example.voetbalai.domain.model.Day
import com.example.voetbalai.domain.model.Player
import com.example.voetbalai.domain.model.Position
import com.example.voetbalai.domain.model.Team
import com.example.voetbalai.domain.stats.DrillResult
import com.example.voetbalai.domain.stats.MatchAppearance
import com.example.voetbalai.domain.stats.PlayerCard
import com.example.voetbalai.domain.stats.PlayerStat
import com.example.voetbalai.domain.stats.StatType
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

// UITLEG: Dit is de echte verbinding met de Supabase-database (via internet, of lokaal op mijn computer).
// - De DTO-klassen (TeamDto, PlayerDto, ...) zijn kopieën van een rij uit de database, precies zoals de
//   tabel heet: training_days, session_minutes. @SerialName koppelt de database-naam aan mijn Kotlin-naam.
// - De app heeft geen inlogscherm. Daarom log ik bij de eerste aanvraag anoniem in (signInAnonymously).
//   Supabase onthoudt die sessie op de telefoon, dus de volgende keer ben ik dezelfde gebruiker.
//   Dit is nodig omdat de beveiliging (RLS) alleen een ingelogde gebruiker zijn eigen team laat zien.
// - Opslaan gaat via de database-functie save_team, zodat team en spelers samen worden opgeslagen.

@Serializable
private data class TeamDto(
    val id: Long,
    val name: String,
    @SerialName("training_days") val trainingDays: List<String>,
    @SerialName("session_minutes") val sessionMinutes: Int,
    @SerialName("invite_code") val inviteCode: String
) {
    // Onbekende dagen sla ik over, zodat een vreemde waarde in de database de app niet laat crashen.
    fun toDomain() = Team(
        id = id,
        name = name,
        trainingDays = trainingDays.mapNotNull { runCatching { Day.valueOf(it) }.getOrNull() }.toSet(),
        sessionMinutes = sessionMinutes,
        inviteCode = inviteCode
    )
}

@Serializable
private data class PlayerDto(
    val id: Long,
    val name: String,
    val position: String,
    val attendance: String
) {
    fun toDomain() = Player(id, name, Position.valueOf(position), Attendance.valueOf(attendance))
}

@Serializable
private data class NewPlayerDto(
    @SerialName("team_id") val teamId: Long,
    val name: String,
    val position: String
)

@Serializable
private data class ClubPlayerDto(val name: String, val position: String) {
    fun toDomain() = ClubPlayer(name, Position.valueOf(position))
}

// DTO's voor de stats. De namen komen overeen met de views en tabellen in de database.
@Serializable
private data class PlayerCardDto(
    @SerialName("player_id") val playerId: Long,
    val name: String,
    val position: String,
    val overall: Int? = null
) {
    fun toDomain() = PlayerCard(playerId, name, Position.valueOf(position), overall)
}

@Serializable
private data class PlayerStatDto(val stat: String, val rating: Int)

@Serializable
private data class DrillDto(
    val stat: String,
    val drill: String,
    val attempts: Int,
    val successes: Int,
    @SerialName("recorded_on") val recordedOn: String
)

@Serializable
private data class NewDrillDto(
    @SerialName("player_id") val playerId: Long,
    val stat: String,
    val drill: String,
    val attempts: Int,
    val successes: Int
)

// Een wedstrijdregel van een speler, met de wedstrijd zelf erbij ("matches"), zoals PostgREST die meestuurt.
@Serializable
private data class MatchPlayerDto(val minutes: Int, val goals: Int, val assists: Int, val matches: MatchDto)

@Serializable
private data class MatchDto(
    @SerialName("played_on") val playedOn: String,
    val opponent: String,
    val home: Boolean,
    @SerialName("goals_for") val goalsFor: Int,
    @SerialName("goals_against") val goalsAgainst: Int
)

class SupabaseRemote(private val client: SupabaseClient) : TeamRemoteDataSource, StatsRemoteDataSource {

    // Zorgt dat er een ingelogde (anonieme) gebruiker is voordat ik iets vraag.
    // Meerdere schermen vragen tegelijk iets bij het opstarten. De Mutex laat ze na elkaar langs de deur gaan,
    // zodat er maar één keer ingelogd wordt (anders maak ik per aanvraag een nieuwe anonieme gebruiker).
    private val signInLock = Mutex()

    private suspend fun ensureSignedIn() = signInLock.withLock {
        if (client.auth.currentSessionOrNull() == null) client.auth.signInAnonymously()
    }

    override suspend fun fetchTeam(): Team? {
        ensureSignedIn()
        // Het nieuwste team (hoogste id). Ik vraag er maximaal één.
        return client.from("teams").select {
            order("id", Order.DESCENDING)
            limit(1)
        }.decodeList<TeamDto>().firstOrNull()?.toDomain()
    }

    override suspend fun fetchPlayers(teamId: Long): List<Player> {
        ensureSignedIn()
        return client.from("players").select {
            filter { eq("team_id", teamId) }
            order("id", Order.ASCENDING)
        }.decodeList<PlayerDto>().map { it.toDomain() }
    }

    override suspend fun saveTeam(team: Team, players: List<Player>) {
        ensureSignedIn()
        // De parameters van de database-functie save_team. De dagen houd ik in vaste volgorde (MON..FRI).
        val params = buildJsonObject {
            put("p_name", team.name)
            put("p_training_days", JsonArray(Day.entries.filter { it in team.trainingDays }.map { JsonPrimitive(it.name) }))
            put("p_session_minutes", team.sessionMinutes)
            put("p_invite_code", team.inviteCode)
            put("p_players", buildJsonArray {
                players.forEach { p ->
                    add(buildJsonObject {
                        put("name", p.name)
                        put("position", p.position.name)
                        put("attendance", p.attendance.name)
                    })
                }
            })
        }
        client.postgrest.rpc("save_team", params)
    }

    override suspend fun updateAttendance(playerId: Long, attendance: Attendance) {
        ensureSignedIn()
        client.from("players").update({ set("attendance", attendance.name) }) {
            filter { eq("id", playerId) }
        }
    }

    override suspend fun insertPlayer(teamId: Long, name: String, position: Position) {
        ensureSignedIn()
        client.from("players").insert(NewPlayerDto(teamId, name, position.name))
    }

    override suspend fun deleteAllTeams() {
        ensureSignedIn()
        // Een delete heeft altijd een filter nodig. "id groter dan 0" is elk team (de beveiliging laat alleen
        // mijn eigen teams toe).
        client.from("teams").delete { filter { gt("id", 0) } }
    }

    override suspend fun createDemoTeam() {
        ensureSignedIn()
        client.postgrest.rpc("create_demo_team")
    }

    override suspend fun fetchClubPlayers(): List<ClubPlayer> {
        ensureSignedIn()
        return client.from("club_players").select {
            order("id", Order.ASCENDING)
        }.decodeList<ClubPlayerDto>().map { it.toDomain() }
    }

    // ===== Stats =====

    override suspend fun fetchCards(): List<PlayerCard> {
        ensureSignedIn()
        return client.from("player_cards").select().decodeList<PlayerCardDto>().map { it.toDomain() }
    }

    override suspend fun fetchCard(playerId: Long): PlayerCard? {
        ensureSignedIn()
        return client.from("player_cards").select { filter { eq("player_id", playerId) } }
            .decodeList<PlayerCardDto>().firstOrNull()?.toDomain()
    }

    // Een onbekende stat in de database slaan we over, zodat de app niet crasht als er later een stat bijkomt.
    override suspend fun fetchStats(playerId: Long): List<PlayerStat> {
        ensureSignedIn()
        return client.from("player_stats").select { filter { eq("player_id", playerId) } }
            .decodeList<PlayerStatDto>()
            .mapNotNull { dto -> runCatching { StatType.valueOf(dto.stat) }.getOrNull()?.let { PlayerStat(it, dto.rating) } }
    }

    override suspend fun fetchDrills(playerId: Long): List<DrillResult> {
        ensureSignedIn()
        return client.from("drill_results").select {
            filter { eq("player_id", playerId) }
            order("recorded_on", Order.DESCENDING)
            limit(10)
        }.decodeList<DrillDto>().mapNotNull { dto ->
            runCatching { StatType.valueOf(dto.stat) }.getOrNull()
                ?.let { DrillResult(it, dto.drill, dto.attempts, dto.successes, dto.recordedOn) }
        }
    }

    override suspend fun fetchMatches(playerId: Long): List<MatchAppearance> {
        ensureSignedIn()
        return client.from("match_players").select(
            Columns.raw("minutes, goals, assists, matches(played_on, opponent, home, goals_for, goals_against)")
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
                assists = it.assists
            )
        }
    }

    override suspend fun insertDrill(playerId: Long, stat: StatType, drill: String, attempts: Int, successes: Int) {
        ensureSignedIn()
        client.from("drill_results").insert(NewDrillDto(playerId, stat.name, drill, attempts, successes))
    }

    companion object {
        // Maakt de verbinding met het adres en de sleutel uit local.properties (zie app/build.gradle.kts).
        fun create(): SupabaseRemote = SupabaseRemote(
            createSupabaseClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_KEY) {
                install(Auth)
                install(Postgrest)
            }
        )
    }
}
