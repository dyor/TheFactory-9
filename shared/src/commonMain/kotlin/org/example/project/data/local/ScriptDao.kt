package org.example.project.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.example.project.domain.model.Script

@Dao
interface ScriptDao {
    @Insert
    suspend fun insertScript(script: Script): Long

    @Update
    suspend fun updateScript(script: Script)

    @Delete
    suspend fun deleteScript(script: Script)

    @Query("SELECT * FROM scripts ORDER BY createdAt DESC")
    fun getAllScripts(): Flow<List<Script>>

    @Query("SELECT * FROM scripts WHERE id = :id")
    suspend fun getScriptById(id: Long): Script?

    @Query("UPDATE scripts SET isActive = 0 WHERE isActive = 1")
    suspend fun clearActiveScript()

    @Query("UPDATE scripts SET isActive = 1 WHERE id = :scriptId")
    suspend fun setActiveScript(scriptId: Long)

    @Query("UPDATE scripts SET currentStage = :stage WHERE id = :scriptId")
    suspend fun updateScriptStage(scriptId: Long, stage: org.example.project.domain.model.ScriptStage)

    @Query("UPDATE scripts SET videoPath = :path WHERE id = :scriptId")
    suspend fun updateScriptVideoPath(scriptId: Long, path: String)

    @Query("SELECT * FROM scripts WHERE isActive = 1 LIMIT 1")
    fun getActiveScript(): Flow<Script?>
}
