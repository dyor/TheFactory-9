package org.example.project.data.local

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

actual fun getInMemoryDatabase(): AppDatabase {
    val db = Room.inMemoryDatabaseBuilder(
        factory = { AppDatabaseConstructor.initialize() } // Use factory for in-memory on iOS
    ).setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
    return db
}
