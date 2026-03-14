package org.example.project.domain

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import java.io.File
import java.nio.ByteBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class VideoTrimmer actual constructor() {
    actual suspend fun trimVideo(
        inputPath: String,
        outputPath: String,
        segmentsToKeep: List<VideoTrimSegment>
    ): Boolean = withContext(Dispatchers.Default) {
        try {
            val file = File(inputPath)
            if (!file.exists()) return@withContext false

            val extractor = MediaExtractor()
            extractor.setDataSource(inputPath)

            var videoTrackIndex = -1
            var audioTrackIndex = -1

            val muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val trackMap = mutableMapOf<Int, Int>()

            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
                if (mime.startsWith("video/")) {
                    videoTrackIndex = i
                    val outTrackIndex = muxer.addTrack(format)
                    trackMap[i] = outTrackIndex
                } else if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    val outTrackIndex = muxer.addTrack(format)
                    trackMap[i] = outTrackIndex
                }
            }

            muxer.start()

            val maxChunkSize = 1024 * 1024 * 2 // 2MB
            val buffer = ByteBuffer.allocate(maxChunkSize)
            val bufferInfo = MediaCodec.BufferInfo()

            var presentationTimeOffsetUs = 0L

            for (segment in segmentsToKeep) {
                val startUs = segment.startMs * 1000
                val endUs = segment.endMs * 1000
                
                var segmentDurationUs = 0L
                var firstSampleTimeUs = -1L

                // Process tracks one by one or concurrently if needed. 
                // For simplicity, we seek and read until end of segment.
                // Note: Native Android muxing requires monotonic timestamps per track,
                // so we must read audio and video interleaved, just like the extractor provides them.
                
                extractor.seekTo(startUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
                
                while (true) {
                    val trackIndex = extractor.sampleTrackIndex
                    if (trackIndex < 0) break

                    val sampleTimeUs = extractor.sampleTime
                    if (sampleTimeUs > endUs) break

                    if (sampleTimeUs >= startUs) {
                        if (firstSampleTimeUs == -1L) {
                            firstSampleTimeUs = sampleTimeUs
                        }

                        val size = extractor.readSampleData(buffer, 0)
                        if (size > 0) {
                            val outTrackIndex = trackMap[trackIndex]
                            if (outTrackIndex != null) {
                                bufferInfo.offset = 0
                                bufferInfo.size = size
                                bufferInfo.flags = extractor.sampleFlags
                                
                                // Calculate the new presentation time
                                val currentSampleOffsetUs = sampleTimeUs - firstSampleTimeUs
                                bufferInfo.presentationTimeUs = presentationTimeOffsetUs + currentSampleOffsetUs
                                
                                muxer.writeSampleData(outTrackIndex, buffer, bufferInfo)
                                
                                // Track the max duration of this segment to offset the next one
                                if (currentSampleOffsetUs > segmentDurationUs) {
                                    segmentDurationUs = currentSampleOffsetUs
                                }
                            }
                        }
                    }
                    extractor.advance()
                }
                
                // Add the duration of this segment to the overall offset for the next segment
                presentationTimeOffsetUs += segmentDurationUs + 10000 // Add small 10ms gap between segments
            }

            muxer.stop()
            muxer.release()
            extractor.release()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("VideoTrimmer", "Error trimming video", e)
            false
        }
    }
}
