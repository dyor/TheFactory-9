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
import org.example.project.domain.resolveVideoPath

import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import kotlinx.coroutines.delay

import kotlinx.cinterop.readValue

@OptIn(ExperimentalForeignApi::class)
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
    val player = remember(url) {
        val resolvedUrl = resolveVideoPath(url)
        val nsUrl = if (resolvedUrl.startsWith("/")) {
            NSURL.fileURLWithPath(resolvedUrl)
        } else {
            NSURL.URLWithString(resolvedUrl)
        }
        val p = if (nsUrl != null) AVPlayer(uRL = nsUrl) else AVPlayer()
        p
    }

    DisposableEffect(player) {
        val observer = NSNotificationCenter.defaultCenter.addObserverForName(
            AVPlayerItemDidPlayToEndTimeNotification,
            null,
            NSOperationQueue.mainQueue,
            { _ -> onCompletion() }
        )
        onDispose {
            NSNotificationCenter.defaultCenter.removeObserver(observer)
        }
    }

    UIKitViewController(
        modifier = modifier,
        factory = {
            val controller = AVPlayerViewController()
            controller.player = player
            if (isPlaying) player.play()
            controller
        },
        update = { _ ->
            if (isPlaying) player.play() else player.pause()
        }
    )

    LaunchedEffect(isPlaying) {
        if (isPlaying) player.play() else player.pause()
    }

    LaunchedEffect(seekRequest) {
        if (seekRequest != null) {
            player.seekToTime(CMTimeMake(seekRequest, 1000), platform.CoreMedia.kCMTimeZero.readValue(), platform.CoreMedia.kCMTimeZero.readValue())
            delay(200)
            onSeekHandled()
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            player.play()
        } else {
            player.pause()
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
