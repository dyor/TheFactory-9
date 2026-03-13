package org.example.project.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed class ScriptStage {
    @Serializable
    data object WRITERS_ROOM : ScriptStage()
    @Serializable
    data object RECORDING_STUDIO : ScriptStage()
    @Serializable
    data object EDITING_STUDIO : ScriptStage()
    @Serializable
    data object PUBLISHING_STUDIO : ScriptStage()
    @Serializable
    data object ARCHIVES : ScriptStage()
}
