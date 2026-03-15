package org.example.project.domain

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.CValue
import kotlinx.cinterop.readValue // Added for .readValue()
import platform.AVFoundation.*
import platform.CoreMedia.*
import platform.Foundation.NSURL
import platform.Foundation.NSFileManager
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

import platform.CoreMedia.CMTimeGetSeconds

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@OptIn(ExperimentalForeignApi::class)
actual class VideoTrimmer actual constructor() {
    actual suspend fun trimVideo(
        inputPath: String,
        outputPath: String,
        segmentsToKeep: List<VideoTrimSegment>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val resolvedInput = resolveVideoPath(inputPath)
            val resolvedOutput = resolveVideoPath(outputPath)

            val inputUrl = if (resolvedInput.startsWith("/")) NSURL.fileURLWithPath(resolvedInput) else NSURL.URLWithString(resolvedInput)
            val outputUrl = if (resolvedOutput.startsWith("/")) NSURL.fileURLWithPath(resolvedOutput) else NSURL.URLWithString(resolvedOutput)
            
            if (inputUrl == null || outputUrl == null) {
                return@withContext false
            }

            val fileManager = NSFileManager.defaultManager
            if (fileManager.fileExistsAtPath(outputUrl.path!!)) {
                fileManager.removeItemAtURL(outputUrl, null)
            }

            // Using standard Kotlin constructors ensures the correct KMP typing
            val asset = AVURLAsset(uRL = inputUrl, options = null)
            val composition = AVMutableComposition()

            // Added .readValue() to convert the struct
            var insertTime: CValue<CMTime> = kCMTimeZero.readValue()

            // Using the suspend functions for newer iOS SDKs
            val videoTracks = suspendCoroutine { cont ->
                asset.loadTracksWithMediaType(AVMediaTypeVideo) { tracks, _ ->
                    cont.resume(tracks as? List<AVAssetTrack> ?: emptyList())
                }
            }
            val audioTracks = suspendCoroutine { cont ->
                asset.loadTracksWithMediaType(AVMediaTypeAudio) { tracks, _ ->
                    cont.resume(tracks as? List<AVAssetTrack> ?: emptyList())
                }
            }
            
            val compVideoTrack = if (videoTracks.isNotEmpty()) composition.addMutableTrackWithMediaType(AVMediaTypeVideo, kCMPersistentTrackID_Invalid) else null
            val compAudioTrack = if (audioTracks.isNotEmpty()) composition.addMutableTrackWithMediaType(AVMediaTypeAudio, kCMPersistentTrackID_Invalid) else null

            // Copy transform from original track to preserve rotation/orientation
            if (compVideoTrack != null && videoTracks.isNotEmpty()) {
                compVideoTrack.preferredTransform = videoTracks.first().preferredTransform
            }

            for (segment in segmentsToKeep) {
                val start = CMTimeMake(segment.startMs, 1000)
                val duration = CMTimeMake(segment.endMs - segment.startMs, 1000)
                val timeRange = CMTimeRangeMake(start, duration)

                if (compVideoTrack != null && videoTracks.isNotEmpty()) {
                    val track = videoTracks.first()
                    // Removed the `error = null` parameter
                    compVideoTrack.insertTimeRange(timeRange, ofTrack = track, atTime = insertTime, error = null)
                }
                if (compAudioTrack != null && audioTracks.isNotEmpty()) {
                    val track = audioTracks.first()
                    // Removed the `error = null` parameter
                    compAudioTrack.insertTimeRange(timeRange, ofTrack = track, atTime = insertTime, error = null)
                }

                insertTime = CMTimeAdd(insertTime, duration)
            }

            val exportSession = AVAssetExportSession(composition, AVAssetExportPresetPassthrough)
            if (exportSession == null) return@withContext false

            exportSession.outputURL = outputUrl
            exportSession.outputFileType = AVFileTypeMPEG4

            // Wrap ONLY the final callback in suspendCoroutine
            suspendCoroutine { continuation ->
                exportSession.exportAsynchronouslyWithCompletionHandler {
                    if (exportSession.status == AVAssetExportSessionStatusCompleted) {
                        continuation.resume(true)
                    } else {
                        println("Export failed: ${exportSession.error?.localizedDescription}")
                        continuation.resume(false)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    actual suspend fun getVideoDurationMs(videoPath: String): Long = withContext(Dispatchers.IO) {
        try {
            val resolvedPath = resolveVideoPath(videoPath)
            val url = if (resolvedPath.startsWith("/")) NSURL.fileURLWithPath(resolvedPath) else NSURL.URLWithString(resolvedPath)
            if (url == null) return@withContext 0L

            val asset = AVURLAsset(uRL = url, options = null)
            val duration = asset.duration
            val seconds = CMTimeGetSeconds(duration)
            if (seconds.isNaN()) 0L else (seconds * 1000).toLong()
        } catch (e: Exception) {
            0L
        }
    }
}