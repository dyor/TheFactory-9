package org.example.project.domain

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberVideoPublisher(): VideoPublisher {
    return remember { VideoPublisher() }
}
