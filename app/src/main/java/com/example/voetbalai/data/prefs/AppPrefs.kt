package com.example.voetbalai.data.prefs

import android.content.Context

// UITLEG: Hier onthoudt de app één klein ja/nee-ding: is het team opzetten (onboarding) al klaar?
// Dat is te klein voor de database, dus gebruik ik SharedPreferences: een simpel bestandje met
// sleutel en waarde dat Android voor me bijhoudt.
// OnboardingStore is de interface (de belofte) en PrefsOnboardingStore de echte versie,
// zodat ik in tests een nepversie kan gebruiken.

/** Bewaart of de onboarding klaar is; losgekoppeld van Android zodat de repository te testen is. */
interface OnboardingStore {
    fun isOnboarded(): Boolean
    fun setOnboarded(value: Boolean)
}

class PrefsOnboardingStore(context: Context) : OnboardingStore {
    // Het bestandje "voetbalai" met daarin de sleutel "onboarded" (true of false).
    private val prefs = context.applicationContext.getSharedPreferences("voetbalai", Context.MODE_PRIVATE)

    override fun isOnboarded() = prefs.getBoolean("onboarded", false)

    override fun setOnboarded(value: Boolean) = prefs.edit().putBoolean("onboarded", value).apply()
}
