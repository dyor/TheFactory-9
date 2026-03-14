package org.example.project.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun VideoPlayer(
    modifier: Modifier = Modifier, 
    url: String,
    seekRequest: Long? = null,
    onSeekHandled: () -> Unit = {},
    onTimeUpdate: (Long) -> Unit = {}
)
