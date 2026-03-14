package org.example.project.domain

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberVideoPublisher(): VideoPublisher {
    val context = LocalContext.current
    return remember(context) { VideoPublisher(context) }
}
