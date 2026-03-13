package org.example.project.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.example.project.domain.model.ScriptStage

@Entity(tableName = "scripts")
data class Script(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val createdAt: Long, // Epoch milliseconds
    val isRecorded: Boolean = false,
    val isActive: Boolean = false,
    @ColumnInfo(defaultValue = "WRITERS_ROOM")
    val currentStage: ScriptStage = ScriptStage.WRITERS_ROOM
)
