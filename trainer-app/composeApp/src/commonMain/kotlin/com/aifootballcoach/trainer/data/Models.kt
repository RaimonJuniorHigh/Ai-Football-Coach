package com.aifootballcoach.trainer.data

enum class Position(val label: String) {
    Keeper("Keeper"),
    Defence("Defence"),
    Midfield("Midfield"),
    Attack("Attack"),
}

enum class Weekday(val short: String) {
    Mon("Mon"), Tue("Tue"), Wed("Wed"), Thu("Thu"), Fri("Fri"),
}

data class Player(
    val id: String,
    val name: String,
    val position: Position,
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
