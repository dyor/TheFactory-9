package org.example.project.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.CValue
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.play
import platform.AVFoundation.pause
import platform.AVFoundation.addPeriodicTimeObserverForInterval
import platform.AVFoundation.removeTimeObserver
import platform.AVFoundation.seekToTime
import platform.AVKit.AVPlayerViewController
import platform.CoreMedia.CMTime
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMake
import platform.Foundation.NSURL
import platform.UIKit.UIViewController

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoPlayer(
    modifier: Modifier, 
    url: String,
    seekRequest: Long?,
    onSeekHandled: () -> Unit,
    onTimeUpdate: (Long) -> Unit
) {
    val player = remember(url) {
        val nsUrl = if (url.startsWith("/")) {
            NSURL.fileURLWithPath(url)
        } else {
            NSURL.URLWithString(url)
        }
        if (nsUrl != null) AVPlayer(uRL = nsUrl) else AVPlayer()
    }

    UIKitViewController(
        modifier = modifier,
        factory = {
            val controller = AVPlayerViewController()
            controller.player = player
            // Play automatically when it appears
            player.play()
            controller
        },
        update = { _ ->
            // Update logic if needed
        }
    )

    LaunchedEffect(seekRequest) {
        if (seekRequest != null) {
            player.seekToTime(CMTimeMake(seekRequest, 1000))
            onSeekHandled()
        }
    }

    DisposableEffect(player) {
        val timeScale = 1000
        val observer = player.addPeriodicTimeObserverForInterval(
            interval = CMTimeMake(100, timeScale),
            queue = null
        ) { time ->
            val seconds = CMTimeGetSeconds(time)
            onTimeUpdate((seconds * 1000).toLong())
        }

        onDispose {
            player.removeTimeObserver(observer)
            player.pause()
        }
    }
}
