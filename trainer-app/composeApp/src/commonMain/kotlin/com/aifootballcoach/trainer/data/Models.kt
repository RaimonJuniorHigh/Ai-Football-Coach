package com.aifootballcoach.trainer.data

enum class Position(val label: String) {
    Keeper("Keeper"),
    Defence("Verdediging"),
    Midfield("Middenveld"),
    Attack("Aanval"),
}

enum class Weekday(val short: String) {
    Mon("Ma"), Tue("Di"), Wed("Wo"), Thu("Do"), Fri("Vr"),
}

/** Whether a player is available for the next session. */
enum class Availability { In, Out }

/** Something about the player the trainer should look at. */
enum class PlayerFlag { Stalled, Review }

data class Player(
    val id: String,
    val name: String,
    val position: Position,
    /** Specific role shown under the name, e.g. "Rechtsback". */
    val role: String = position.label,
    val availability: Availability = Availability.In,
    val flag: PlayerFlag? = null,
) {
    val initials: String
        get() = name.split(' ')
            .filter { it.isNotBlank() }
            .let { parts -> listOfNotNull(parts.firstOrNull(), parts.drop(1).lastOrNull()) }
            .joinToString("") { it.first().uppercase() }
}

data class Team(
    val name: String,
    val trainingDays: Set<Weekday>,
    val sessionMinutes: Int,
)

data class Trainer(
    val firstName: String,
    val lastName: String,
) {
    val initials: String get() = "${firstName.first()}${lastName.first()}"
}

/** An AI-drafted session waiting for the trainer's review. */
data class SessionDraft(
    val day: String,
    val time: String,
    val theme: String,
    val minutes: Int,
)

/** A "Needs you" item on the Today screen. */
data class Attention(
    val player: Player,
    val detail: String,
)
