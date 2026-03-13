package org.example.project

import androidx.compose.ui.window.ComposeUIViewController
import org.example.project.data.local.getDatabase

fun MainViewController() = ComposeUIViewController { App(database = getDatabase()) }