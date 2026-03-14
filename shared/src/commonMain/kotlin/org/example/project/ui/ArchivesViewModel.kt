package org.example.project.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.example.project.data.local.ScriptDao
import org.example.project.domain.model.Script

class ArchivesViewModel(
    private val scriptDao: ScriptDao
) : ViewModel() {
    
    val allScripts: StateFlow<List<Script>> = scriptDao.getAllScripts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun makeScriptActive(scriptId: Long) {
        viewModelScope.launch {
            scriptDao.clearActiveScript()
            scriptDao.setActiveScript(scriptId)
        }
    }

    fun archiveScript(scriptId: Long) {
        viewModelScope.launch {
            scriptDao.clearActiveScript() // Which removes the isActive flag
        }
    }
}
