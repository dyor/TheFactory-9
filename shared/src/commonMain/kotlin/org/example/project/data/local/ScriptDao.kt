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
}
