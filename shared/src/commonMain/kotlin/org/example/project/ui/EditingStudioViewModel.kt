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
    val markedForRemoval: Boolean = false
)

data class EditingStudioUiState(
    val segments: List<VideoSegment> = emptyList(),
    val isSaved: Boolean = false,
    val isSaving: Boolean = false,
    val videoPath: String? = null
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
                }
            }
        }

        // Initialize 60 one-second segments
        val initialSegments = (1..60).map { VideoSegment(second = it) }
        _uiState.value = _uiState.value.copy(segments = initialSegments)
    }

    fun clearSeekRequest() {
        _seekRequest.value = null
    }

    fun onTimeUpdate(currentPositionMs: Long) {
        if (_seekRequest.value != null) return // Already handling a seek
        
        val currentSecond = (currentPositionMs / 1000).toInt() + 1
        val segments = _uiState.value.segments
        val segment = segments.find { it.second == currentSecond }
        
        if (segment?.markedForRemoval == true) {
            val nextUnskipped = segments.find { it.second > currentSecond && !it.markedForRemoval }
            if (nextUnskipped != null) {
                // Seek to the start of the next unskipped second, with a tiny 50ms buffer to ensure we are inside it
                val nextStartMs = (nextUnskipped.second - 1) * 1000L + 50L
                _seekRequest.value = nextStartMs
            }
        }
    }

    fun toggleRemovalMarker(second: Int) {
        val oldSegment = _uiState.value.segments.find { it.second == second } ?: return
        val isNowMarked = !oldSegment.markedForRemoval

        val updatedSegments = _uiState.value.segments.map {
            if (it.second == second) it.copy(markedForRemoval = isNowMarked) else it
        }
        _uiState.value = _uiState.value.copy(segments = updatedSegments, isSaved = false)

        if (isNowMarked) {
            // We just skipped it. Advance to start of next unskipped.
            val nextUnskipped = updatedSegments.find { it.second > second && !it.markedForRemoval }
            if (nextUnskipped != null) {
                val nextStartMs = (nextUnskipped.second - 1) * 1000L + 50L
                _seekRequest.value = nextStartMs
            }
        } else {
            // We just unskipped it. Advance to start of this second.
            val thisStartMs = (second - 1) * 1000L
            _seekRequest.value = thisStartMs
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
                if (!segment.markedForRemoval) {
                    val start = (segment.second - 1) * 1000L
                    val end = segment.second * 1000L
                    
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
        val resetSegments = _uiState.value.segments.map { it.copy(markedForRemoval = false) }
        _uiState.value = _uiState.value.copy(
            segments = resetSegments,
            isSaved = false
        )
    }
}
