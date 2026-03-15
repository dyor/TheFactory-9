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
                    extractor.selectTrack(i)
                } else if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    val outTrackIndex = muxer.addTrack(format)
                    trackMap[i] = outTrackIndex
                    extractor.selectTrack(i)
                }
            }

            // Copy rotation metadata
            try {
                val retriever = android.media.MediaMetadataRetriever()
                retriever.setDataSource(inputPath)
                val rotation = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull()
                if (rotation != null) {
                    muxer.setOrientationHint(rotation)
                }
                retriever.release()
            } catch (e: Exception) {
                Log.e("VideoTrimmer", "Failed to set orientation hint", e)
            }

            muxer.start()

            val maxChunkSize = 1024 * 1024 * 2 // 2MB
            val buffer = ByteBuffer.allocate(maxChunkSize)
            val bufferInfo = MediaCodec.BufferInfo()

            var presentationTimeOffsetUs = 0L
            val lastWrittenTimeUs = mutableMapOf<Int, Long>()

            for (segment in segmentsToKeep) {
                val startUs = segment.startMs * 1000
                val endUs = segment.endMs * 1000
                
                var segmentStartUs = -1L
                var maxSegmentDurationUs = 0L

                // Seek extractor to the nearest keyframe before startUs
                extractor.seekTo(startUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
                
                while (true) {
                    val trackIndex = extractor.sampleTrackIndex
                    if (trackIndex < 0) break

                    val sampleTimeUs = extractor.sampleTime
                    
                    // End early if we are cleanly past the segment end
                    if (sampleTimeUs > endUs) break 

                    // Always capture from the first frame returned by seek (which is a keyframe) to avoid corruption
                    if (segmentStartUs == -1L) {
                        segmentStartUs = sampleTimeUs
                    }

                    val size = extractor.readSampleData(buffer, 0)
                    if (size > 0) {
                        val outTrackIndex = trackMap[trackIndex]
                        if (outTrackIndex != null) {
                            val currentSampleOffsetUs = sampleTimeUs - segmentStartUs
                            val pts = presentationTimeOffsetUs + currentSampleOffsetUs
                            
                            val lastPts = lastWrittenTimeUs[outTrackIndex] ?: -1L
                            
                            // Ensure strict monotonic timestamps to prevent Muxer crashes
                            if (pts > lastPts) {
                                bufferInfo.offset = 0
                                bufferInfo.size = size
                                bufferInfo.flags = extractor.sampleFlags
                                bufferInfo.presentationTimeUs = pts
                                
                                muxer.writeSampleData(outTrackIndex, buffer, bufferInfo)
                                lastWrittenTimeUs[outTrackIndex] = pts
                                
                                if (currentSampleOffsetUs > maxSegmentDurationUs) {
                                    maxSegmentDurationUs = currentSampleOffsetUs
                                }
                            }
                        }
                    }
                    extractor.advance()
                }
                
                presentationTimeOffsetUs += maxSegmentDurationUs + 10000 // Add small 10ms gap
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

    actual suspend fun getVideoDurationMs(videoPath: String): Long = withContext(Dispatchers.IO) {
        try {
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(videoPath)
            val durationStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            durationStr?.toLongOrNull() ?: 0L
        } catch(e: Exception) {
            0L
        }
    }
}
