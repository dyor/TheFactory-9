package org.example.project.data.local

import androidx.room.TypeConverter
import org.example.project.domain.model.ScriptStage

class ScriptStageConverter {
    @TypeConverter
    fun fromScriptStage(stage: ScriptStage): String {
        return when (stage) {
            ScriptStage.WRITERS_ROOM -> "WRITERS_ROOM"
            ScriptStage.RECORDING_STUDIO -> "RECORDING_STUDIO"
            ScriptStage.EDITING_STUDIO -> "EDITING_STUDIO"
            ScriptStage.PUBLISHING_STUDIO -> "PUBLISHING_STUDIO"
            ScriptStage.ARCHIVES -> "ARCHIVES"
        }
    }

    @TypeConverter
    fun toScriptStage(stageString: String): ScriptStage {
        return when (stageString) {
            "WRITERS_ROOM" -> ScriptStage.WRITERS_ROOM
            "RECORDING_STUDIO" -> ScriptStage.RECORDING_STUDIO
            "EDITING_STUDIO" -> ScriptStage.EDITING_STUDIO
            "PUBLISHING_STUDIO" -> ScriptStage.PUBLISHING_STUDIO
            "ARCHIVES" -> ScriptStage.ARCHIVES
            else -> ScriptStage.WRITERS_ROOM // Default or handle error
        }
    }
}
