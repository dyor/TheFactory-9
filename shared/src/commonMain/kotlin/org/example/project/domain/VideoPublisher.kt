package org.example.project.domain

/**
 * Handles exporting a video to the native gallery and launching the YouTube app.
 */
expect class VideoPublisher {
    /**
     * Saves the video to the native Photos/Gallery app.
     * @param videoPath the absolute path to the video file.
     * @return true if successful.
     */
    suspend fun saveToGallery(videoPath: String): Boolean
    
    /**
     * Opens the YouTube app (or website if app is not installed).
     */
    fun openYouTube()
}
