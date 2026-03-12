package org.example.project.data.local

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.example.project.domain.model.Script
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ScriptDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var scriptDao: ScriptDao

    @BeforeTest
    fun setup() {
        // In common tests, we instantiate an in-memory database to ensure isolation.
        // Room 2.7.0+ supports an in-memory builder for commonMain via the new driver pattern.
        database = androidx.room.Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(androidx.sqlite.driver.bundled.BundledSQLiteDriver())
            .build()
        scriptDao = database.scriptDao()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetScript_shouldReturnSameScript() = runTest {
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

    @Test
    fun updateScript_shouldReflectChanges() = runTest {
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

    @Test
    fun deleteScript_shouldRemoveFromDatabase() = runTest {
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

    @Test
    fun getAllScripts_shouldReturnOrderedList() = runTest {
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
