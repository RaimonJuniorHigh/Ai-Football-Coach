package com.aifootballcoach.trainer.ui.components

/** Names of exceptions (or their causes) that mean the phone couldn't reach the server. */
private val networkErrorNames = listOf("IOException", "Timeout", "UnresolvedAddress", "Connect", "HttpRequestException")

/**
 * Turns an exception into a short Dutch sentence a trainer understands, instead of the
 * technical message. [action] is what failed, e.g. "laden" or "opslaan".
 */
fun friendlyError(error: Throwable, action: String): String =
    if (error.isNetworkError()) {
        "Geen verbinding met de server. Controleer je internet en probeer het opnieuw."
    } else {
        "Het $action is niet gelukt. Probeer het zo nog eens."
    }

private fun Throwable.isNetworkError(): Boolean =
    generateSequence(this) { it.cause }
        .take(5)
        .any { e -> networkErrorNames.any { name -> e::class.simpleName.orEmpty().contains(name) } }
