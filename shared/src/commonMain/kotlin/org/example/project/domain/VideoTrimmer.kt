package org.example.project.domain

/**
 * A segment of video to be kept in the final output.
 * @param startMs The start time in milliseconds.
 * @param endMs The end time in milliseconds.
 */
data class VideoTrimSegment(val startMs: Long, val endMs: Long)

expect class VideoTrimmer() {
    /**
     * Trims the input video by keeping only the specified segments.
     * @param inputPath The absolute path to the input video.
     * @param outputPath The absolute path to save the trimmed video.
     * @param segmentsToKeep A list of segments to keep. The segments will be concatenated in order.
     * @return true if successful, false otherwise.
     */
    suspend fun trimVideo(inputPath: String, outputPath: String, segmentsToKeep: List<VideoTrimSegment>): Boolean

    /**
     * Gets the total duration of the video at the given path in milliseconds.
     * @param videoPath The absolute path to the video.
     * @return Duration in milliseconds, or 0 if it cannot be determined.
     */
    suspend fun getVideoDurationMs(videoPath: String): Long
}
