package org.example.project.data.local

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.example.project.domain.model.Script

import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

expect fun getInMemoryDatabase(): AppDatabase

open class ScriptDaoTest {





    open fun insertAndGetScript_shouldReturnSameScript(database: AppDatabase, scriptDao: ScriptDao) = runTest {
        val script = Script(
            title = "Test Video",
            content = "This is a test script.",
            createdAt = 1600000000L,
            isRecorded = false
        )
        
        val id = scriptDao.insertScript(script)
        
        val retrievedScript = scriptDao.getScriptById(id)
        assertNotNull(retrievedScript)
        assertEquals("Test Video", retrievedScript.title)
        assertEquals("This is a test script.", retrievedScript.content)
    }

    open fun updateScript_shouldReflectChanges(database: AppDatabase, scriptDao: ScriptDao) = runTest {
        val script = Script(
            title = "Original Title",
            content = "Original Content",
            createdAt = 1600000000L
        )
        val id = scriptDao.insertScript(script)
        
        val updatedScript = scriptDao.getScriptById(id)!!.copy(title = "Updated Title", isRecorded = true)
        scriptDao.updateScript(updatedScript)
        
        val newlyRetrievedScript = scriptDao.getScriptById(id)
        assertEquals("Updated Title", newlyRetrievedScript?.title)
        assertEquals(true, newlyRetrievedScript?.isRecorded)
    }

    open fun deleteScript_shouldRemoveFromDatabase(database: AppDatabase, scriptDao: ScriptDao) = runTest {
        val script = Script(
            title = "To be deleted",
            content = "Content",
            createdAt = 1600000000L
        )
        val id = scriptDao.insertScript(script)
        val scriptToDelete = scriptDao.getScriptById(id)!!
        
        scriptDao.deleteScript(scriptToDelete)
        
        val deletedScript = scriptDao.getScriptById(id)
        assertNull(deletedScript)
    }

    open fun getAllScripts_shouldReturnOrderedList(database: AppDatabase, scriptDao: ScriptDao) = runTest {
        val script1 = Script(title = "First", content = "", createdAt = 1000L)
        val script2 = Script(title = "Second", content = "", createdAt = 2000L)
        
        scriptDao.insertScript(script1)
        scriptDao.insertScript(script2)
        
        // Due to "ORDER BY createdAt DESC", script2 should be first
        val allScripts = scriptDao.getAllScripts().first()
        
        assertEquals(2, allScripts.size)
        assertEquals("Second", allScripts[0].title)
        assertEquals("First", allScripts[1].title)
    }
}
