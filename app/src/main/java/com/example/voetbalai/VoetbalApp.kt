package com.example.voetbalai

import android.app.Application

// UITLEG: Dit is de Application-klasse. Android maakt hem één keer aan als de app start
// (hij staat in AndroidManifest.xml bij android:name=".VoetbalApp"). Hier maak ik de
// AppContainer aan, zodat de hele app dezelfde repository en chat-API gebruikt.
class VoetbalApp : Application() {
    // "by lazy" betekent dat ik hem pas aanmaak als hij voor het eerst nodig is.
    val container by lazy { AppContainer(this) }
}
