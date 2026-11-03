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

data class Player(
    val id: String,
    val name: String,
    val position: Position,
    val availability: Availability = Availability.In,
) {
    val firstName: String get() = name.substringBefore(' ')

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

/** A session draft the trainer made with the brief, waiting for review. */
data class SessionDraft(
    val day: String,
    val dateLabel: String,
    val theme: String,
    val minutes: Int,
)

/** A "Needs you" item on the Today screen. */
data class Attention(
    val player: Player,
    val detail: String,
)

/** The next training on the calendar, as shown in step 1 of the brief. */
data class PlannedSession(
    val day: String,
    val dateLabel: String,
    val minutes: Int,
)

/** A focus theme suggested from the squad's stats, with why. */
data class FocusSuggestion(
    val id: String,
    val title: String,
    val reason: String,
)

enum class PitchSpace(val label: String) {
    Half("Half veld"),
    Full("Heel veld"),
}

enum class Intensity(val label: String) {
    Light("Licht"),
    Medium("Gemiddeld"),
    High("Zwaar"),
}

/** Everything the trainer fills in before a session draft is made. */
data class SessionBrief(
    val session: PlannedSession,
    val absentPlayerIds: Set<String>,
    val focus: List<String>,
    val space: PitchSpace,
    val intensity: Intensity,
    val notes: String,
)
