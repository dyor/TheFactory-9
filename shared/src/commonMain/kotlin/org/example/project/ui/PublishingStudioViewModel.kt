package org.example.project.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.example.project.data.local.ScriptDao
import org.example.project.domain.VideoPublisher
import org.example.project.domain.model.ScriptStage

data class PublishingStudioUiState(
    val videoPath: String? = null,
    val isPublishing: Boolean = false,
    val publishStatus: String? = null
)

class PublishingStudioViewModel(
    private val scriptDao: ScriptDao,
    private val videoPublisher: VideoPublisher
) : ViewModel() {
    private val _uiState = MutableStateFlow(PublishingStudioUiState())
    val uiState: StateFlow<PublishingStudioUiState> = _uiState.asStateFlow()

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
    }

    fun publishToYouTube() {
        val path = _uiState.value.videoPath
        if (path == null) {
            _uiState.value = _uiState.value.copy(publishStatus = "No video found to publish.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPublishing = true, publishStatus = "Saving to Gallery...")
            
            val success = videoPublisher.saveToGallery(path)
            
            if (success) {
                _uiState.value = _uiState.value.copy(isPublishing = false, publishStatus = "Saved! Opening YouTube...")
                
                // Update database to reflect completion
                val activeScript = scriptDao.getActiveScript().firstOrNull()
                if (activeScript != null) {
                    scriptDao.updateScriptStage(activeScript.id, ScriptStage.PUBLISHING_STUDIO)
                }
                
                videoPublisher.openYouTube()
            } else {
                _uiState.value = _uiState.value.copy(isPublishing = false, publishStatus = "Failed to save video to gallery.")
            }
        }
    }
    
    fun finishPublishing() {
        viewModelScope.launch {
            val activeScript = scriptDao.getActiveScript().firstOrNull()
            if (activeScript != null) {
                 scriptDao.clearActiveScript()
            }
        }
    }
}
