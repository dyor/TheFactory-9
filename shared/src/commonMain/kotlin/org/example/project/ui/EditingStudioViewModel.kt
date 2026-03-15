package org.example.project.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.data.local.ScriptDao
import org.example.project.domain.VideoTrimmer
import org.example.project.domain.VideoTrimSegment

import kotlinx.coroutines.flow.firstOrNull

// A representation of a 1-second video segment
data class VideoSegment(
    val second: Int, // 1 to 60
    val skippedTenths: Set<Int> = emptySet()
) {
    val isFullySkipped get() = skippedTenths.size == 10
    val isPartiallySkipped get() = skippedTenths.isNotEmpty() && skippedTenths.size < 10
    val isNotSkipped get() = skippedTenths.isEmpty()
}

data class EditingStudioUiState(
    val segments: List<VideoSegment> = emptyList(),
    val isSaved: Boolean = false,
    val isSaving: Boolean = false,
    val videoPath: String? = null,
    val currentPlaybackSecond: Int = 1,
    val currentPlaybackTenth: Int = 0,
    val isPreviewMode: Boolean = false,
    val selectedSegmentForDetail: Int? = null,
    val isPlaying: Boolean = true
)

class EditingStudioViewModel(
    private val scriptDao: ScriptDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditingStudioUiState())
    val uiState: StateFlow<EditingStudioUiState> = _uiState.asStateFlow()
    
    private val videoTrimmer = VideoTrimmer()
    
    private val _seekRequest = MutableStateFlow<Long?>(null)
    val seekRequest: StateFlow<Long?> = _seekRequest.asStateFlow()

    init {
        viewModelScope.launch {
            scriptDao.getActiveScript().collect { script ->
                if (script != null) {
                    _uiState.value = _uiState.value.copy(
                        videoPath = script.videoPath
                    )
                    if (script.videoPath != null && _uiState.value.segments.isEmpty()) {
                        val durationMs = videoTrimmer.getVideoDurationMs(script.videoPath)
                        if (durationMs > 0) {
                            val numSeconds = kotlin.math.ceil(durationMs / 1000.0).toInt()
                            val initialSegments = (1..numSeconds).map { VideoSegment(second = it) }
                            _uiState.value = _uiState.value.copy(segments = initialSegments)
                        } else {
                            // Fallback
                            val initialSegments = (1..60).map { VideoSegment(second = it) }
                            _uiState.value = _uiState.value.copy(segments = initialSegments)
                        }
                    }
                }
            }
        }
    }

    fun clearSeekRequest() {
        _seekRequest.value = null
    }

    fun onTimeUpdate(currentPositionMs: Long) {
        if (_seekRequest.value != null) return // Already handling a seek
        if (!_uiState.value.isPlaying) return // Do not auto-close or jump if paused by the modal
        
        val currentSecond = (currentPositionMs / 1000).toInt() + 1
        val currentTenth = ((currentPositionMs % 1000) / 100).toInt()
        
        _uiState.value = _uiState.value.copy(
            currentPlaybackSecond = currentSecond,
            currentPlaybackTenth = currentTenth
        )

        // Close the modal if we naturally play into a new second
        if (_uiState.value.selectedSegmentForDetail != null && _uiState.value.selectedSegmentForDetail != currentSecond) {
            closeSegmentDetail()
        }

        if (_uiState.value.isPreviewMode) {
            val maxMs = _uiState.value.segments.maxOfOrNull { it.second }?.times(1000L) ?: return
            if (currentPositionMs >= maxMs - 100) {
                // Video finished playing
                _uiState.value = _uiState.value.copy(isPreviewMode = false, isPlaying = false)
                return
            }

            val segments = _uiState.value.segments
            val segment = segments.find { it.second == currentSecond }
            if (segment?.skippedTenths?.contains(currentTenth) == true) {
                val nextStartMs = findNextUnskippedMs(currentPositionMs)
                if (nextStartMs != null) {
                    _seekRequest.value = nextStartMs
                } else {
                    // Reached the end of unskipped content
                    _uiState.value = _uiState.value.copy(isPreviewMode = false, isPlaying = false)
                }
            }
        }
    }

    fun onVideoCompletion() {
        if (_uiState.value.isPreviewMode) {
            _uiState.value = _uiState.value.copy(isPreviewMode = false, isPlaying = false)
        }
    }

    private fun findNextUnskippedMs(fromMs: Long): Long? {
        var checkMs = fromMs - (fromMs % 100) + 100 // start checking next tenth
        val maxMs = _uiState.value.segments.maxOfOrNull { it.second }?.times(1000L) ?: return null
        while (checkMs < maxMs) {
            val sec = (checkMs / 1000).toInt() + 1
            val ten = ((checkMs % 1000) / 100).toInt()
            val seg = _uiState.value.segments.find { it.second == sec }
            if (seg == null || !seg.skippedTenths.contains(ten)) {
                return checkMs
            }
            checkMs += 100
        }
        return null
    }

    fun openSegmentDetail(second: Int) {
        if (_uiState.value.selectedSegmentForDetail == second) {
            closeSegmentDetail()
            return
        }

        _uiState.value = _uiState.value.copy(
            selectedSegmentForDetail = second,
            isPlaying = false, // Pause when opening modal
            currentPlaybackSecond = second, // Snap purple border
            currentPlaybackTenth = 0
        )
        // Advance video to the start of the selected second
        _seekRequest.value = (second - 1) * 1000L
    }

    fun closeSegmentDetail() {
        val sec = _uiState.value.selectedSegmentForDetail
        _uiState.value = _uiState.value.copy(selectedSegmentForDetail = null, isPlaying = true)
        
        if (sec != null) {
             _seekRequest.value = (sec - 1) * 1000L
        }
    }

    fun toggleTenthSkipped(second: Int, tenth: Int) {
        val segments = _uiState.value.segments.toMutableList()
        val index = segments.indexOfFirst { it.second == second }
        if (index != -1) {
            val segment = segments[index]
            val newTenths = segment.skippedTenths.toMutableSet()
            val isSkipping = !newTenths.contains(tenth)
            if (isSkipping) {
                newTenths.add(tenth)
            } else {
                newTenths.remove(tenth)
            }
            segments[index] = segment.copy(skippedTenths = newTenths)
            
            // Advance video precisely to the start of that tenth
            val seekMs = (second - 1) * 1000L + (tenth * 100L)
            
            _uiState.value = _uiState.value.copy(
                segments = segments, 
                isSaved = false,
                currentPlaybackSecond = second,
                currentPlaybackTenth = tenth
            )
            
            _seekRequest.value = seekMs
        }
    }

    fun skipAllTenths(second: Int) {
        updateTenths(second, (0..9).toSet())
    }

    fun showAllTenths(second: Int) {
        updateTenths(second, emptySet())
    }

    private fun updateTenths(second: Int, tenths: Set<Int>) {
        val segments = _uiState.value.segments.toMutableList()
        val index = segments.indexOfFirst { it.second == second }
        if (index != -1) {
            segments[index] = segments[index].copy(skippedTenths = tenths)
            _uiState.value = _uiState.value.copy(segments = segments, isSaved = false)
            _seekRequest.value = (second - 1) * 1000L
        }
    }

    fun togglePreviewMode() {
        val isNowPreview = !_uiState.value.isPreviewMode
        
        if (isNowPreview) {
            val firstUnskippedSec = _uiState.value.segments.find { !it.isFullySkipped }?.second ?: 1
            val firstUnskippedTen = (0..9).find { !_uiState.value.segments.find { s -> s.second == firstUnskippedSec }!!.skippedTenths.contains(it) } ?: 0
            val seekMs = (firstUnskippedSec - 1) * 1000L + (firstUnskippedTen * 100L)
            
            _uiState.value = _uiState.value.copy(isPreviewMode = true, isPlaying = true)
            _seekRequest.value = seekMs
        } else {
             _uiState.value = _uiState.value.copy(isPreviewMode = false, isPlaying = false)
        }
    }

    fun saveModifications() {
        val currentVideoPath = _uiState.value.videoPath ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            
            val segmentsToKeep = mutableListOf<VideoTrimSegment>()
            var currentStartMs = -1L
            var currentEndMs = -1L
            
            // Build continuous ranges to keep
            _uiState.value.segments.forEach { segment ->
                for (tenth in 0..9) {
                    val isSkipped = segment.skippedTenths.contains(tenth)
                    if (!isSkipped) {
                        val start = (segment.second - 1) * 1000L + tenth * 100L
                        val end = start + 100L
                        
                        if (currentStartMs == -1L) {
                            currentStartMs = start
                            currentEndMs = end
                        } else if (start == currentEndMs) {
                            currentEndMs = end // extend segment
                        } else {
                            segmentsToKeep.add(VideoTrimSegment(currentStartMs, currentEndMs))
                            currentStartMs = start
                            currentEndMs = end
                        }
                    }
                }
            }
            if (currentStartMs != -1L) {
                segmentsToKeep.add(VideoTrimSegment(currentStartMs, currentEndMs))
            }
            
            val extensionIndex = currentVideoPath.lastIndexOf('.')
            val outputPath = if (extensionIndex > 0) {
                currentVideoPath.substring(0, extensionIndex) + "_trimmed" + currentVideoPath.substring(extensionIndex)
            } else {
                currentVideoPath + "_trimmed.mp4"
            }
            
            val success = videoTrimmer.trimVideo(currentVideoPath, outputPath, segmentsToKeep)
            
            if (success) {
                // Update DB with the new path
                val activeScript = scriptDao.getActiveScript().firstOrNull()
                if (activeScript != null) {
                    scriptDao.updateScriptVideoPath(activeScript.id, outputPath)
                }
                
                _uiState.value = _uiState.value.copy(
                    isSaved = true, 
                    isSaving = false,
                    videoPath = outputPath
                )
            } else {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }

    fun restoreOriginal() {
        // Restore to the original list without any removal markers
        val resetSegments = _uiState.value.segments.map { it.copy(skippedTenths = emptySet()) }
        _uiState.value = _uiState.value.copy(
            segments = resetSegments,
            isSaved = false
        )
    }
}
