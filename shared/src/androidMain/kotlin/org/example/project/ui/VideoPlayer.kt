package org.example.project.ui

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.io.File

@Composable
actual fun VideoPlayer(
    modifier: Modifier, 
    url: String,
    seekRequest: Long?,
    isPlaying: Boolean,
    onSeekHandled: () -> Unit,
    onTimeUpdate: (Long) -> Unit,
    onCompletion: () -> Unit
) {
    val context = LocalContext.current
    val videoView = remember {
        VideoView(context).apply {
            val mediaController = MediaController(context)
            mediaController.setAnchorView(this)
            setMediaController(mediaController)
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                gravity = android.view.Gravity.CENTER
            }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { 
            FrameLayout(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                addView(videoView)
            }
        },
        update = { 
            // We intentionally do not call setVideoURI here to prevent recomposition from restarting the video
        }
    )

    LaunchedEffect(url) {
        val uri = if (url.startsWith("/")) Uri.fromFile(File(url)) else Uri.parse(url)
        videoView.setVideoURI(uri)
        videoView.requestFocus()
        videoView.setOnPreparedListener { mp ->
            if (isPlaying) mp.start()
        }
        videoView.setOnCompletionListener {
            onCompletion()
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying && !videoView.isPlaying) {
            videoView.start()
        } else if (!isPlaying && videoView.isPlaying) {
            videoView.pause()
        }
    }

    LaunchedEffect(seekRequest) {
        if (seekRequest != null) {
            videoView.seekTo(seekRequest.toInt())
            delay(200) // Debounce so stale time updates are ignored
            onSeekHandled()
        }
    }

    LaunchedEffect(videoView) {
        while (isActive) {
            if (videoView.isPlaying) {
                val pos = videoView.currentPosition.toLong()
                if (pos >= 0) {
                    onTimeUpdate(pos)
                }
            }
            delay(50)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            videoView.stopPlayback()
        }
    }
}
