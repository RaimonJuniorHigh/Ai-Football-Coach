package com.aifootballcoach.trainer

import com.aifootballcoach.trainer.ui.components.friendlyError
import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorMessagesTest {
    private class UnresolvedAddressException : Exception("api.example.com")

    @Test
    fun networkErrorSaysThereIsNoConnection() {
        assertEquals(
            "Geen verbinding met de server. Controleer je internet en probeer het opnieuw.",
            friendlyError(UnresolvedAddressException(), "laden"),
        )
    }

    @Test
    fun networkErrorIsFoundInTheCause() {
        val error = IllegalStateException("wrapped", UnresolvedAddressException())
        assertEquals(
            "Geen verbinding met de server. Controleer je internet en probeer het opnieuw.",
            friendlyError(error, "opslaan"),
        )
    }

    @Test
    fun otherErrorsNameWhatFailedWithoutTechnicalText() {
        assertEquals(
            "Het opslaan is niet gelukt. Probeer het zo nog eens.",
            friendlyError(IllegalArgumentException("duplicate key value violates unique constraint"), "opslaan"),
        )
    }
}
