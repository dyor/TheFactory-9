package org.example.project.data.local

import kotlin.test.Test
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class IosScriptDaoTest : ScriptDaoTest() {

    private lateinit var database: AppDatabase
    private lateinit var scriptDao: ScriptDao

    @BeforeTest
    fun setup() {
        database = getInMemoryDatabase()
        scriptDao = database.scriptDao()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetScript_shouldReturnSameScript() = super.insertAndGetScript_shouldReturnSameScript(database, scriptDao)

    @Test
    fun updateScript_shouldReflectChanges() = super.updateScript_shouldReflectChanges(database, scriptDao)

    @Test
    fun deleteScript_shouldRemoveFromDatabase() = super.deleteScript_shouldRemoveFromDatabase(database, scriptDao)

    @Test
    fun getAllScripts_shouldReturnOrderedList() = super.getAllScripts_shouldReturnOrderedList(database, scriptDao)
}
