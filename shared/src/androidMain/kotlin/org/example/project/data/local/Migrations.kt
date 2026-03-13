package org.example.project.data.local

// Manual Migrations using SupportSQLiteDatabase are incompatible with BundledSQLiteDriver in KMP.
// If you want to use manual migrations in KMP Room with SQLiteDriver, 
// you must implement `Migration` using `androidx.sqlite.SQLiteConnection` 
// when the proper KMP SQLite APIs are fully stabilized and accessible in your source sets.
// For now, we rely on `.fallbackToDestructiveMigration(dropAllTables = true)` 
// in our RoomDatabase.Builder to clear data on schema change during development.
