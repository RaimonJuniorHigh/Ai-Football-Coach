package com.example.voetbalai.viewmodel.app

import androidx.lifecycle.ViewModel
import com.example.voetbalai.domain.repository.TeamRepository

// UITLEG: Heel klein. Ik vraag hier aan de repository of het team opzetten al klaar is, zodat de app weet
// of hij bij het onboarding-scherm of direct bij "Vandaag" moet beginnen.

/** Beslist waar de app start; heeft geen scherm. */
class AppViewModel(repository: TeamRepository) : ViewModel() {
    /** True als de trainer de onboarding al heeft afgerond: dan start de app direct op Vandaag. */
    val onboardingDone: Boolean = repository.isOnboarded()
}
