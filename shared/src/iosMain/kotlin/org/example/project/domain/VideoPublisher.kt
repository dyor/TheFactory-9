package org.example.project.domain

import platform.Foundation.NSURL
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIApplication
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class VideoPublisher {
    actual suspend fun saveToGallery(videoPath: String): Boolean = withContext(Dispatchers.IO) {
        suspendCoroutine { continuation ->
            val resolvedPath = resolveVideoPath(videoPath)
            val url = if (resolvedPath.startsWith("/")) NSURL.fileURLWithPath(resolvedPath) else NSURL.URLWithString(resolvedPath)
            
            if (url == null) {
                continuation.resume(false)
                return@suspendCoroutine
            }

            PHPhotoLibrary.sharedPhotoLibrary().performChanges({
                platform.Photos.PHAssetChangeRequest.creationRequestForAssetFromVideoAtFileURL(url)
            }, completionHandler = { success, error ->
                if (error != null) {
                    error.localizedDescription?.let { println("Error saving video: $it") }
                }
                continuation.resume(success)
            })
        }
    }
    
    actual fun openYouTube() {
        val appUrl = NSURL.URLWithString("youtube://")
        val webUrl = NSURL.URLWithString("https://www.youtube.com")
        
        val app = UIApplication.sharedApplication
        if (appUrl != null && app.canOpenURL(appUrl)) {
            app.openURL(appUrl, emptyMap<Any?, Any>(), null)
        } else if (webUrl != null) {
            app.openURL(webUrl, emptyMap<Any?, Any>(), null)
        }
    }
}
