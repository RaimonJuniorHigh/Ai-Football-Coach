package com.aifootballcoach.trainer

import androidx.compose.ui.window.ComposeUIViewController

// TODO: read the database address and public key (like Android does from local.properties) once the Xcode project exists.
fun MainViewController() = ComposeUIViewController {
    SetupProblemScreen("De database-instellingen voor iOS zijn nog niet ingesteld.")
}
