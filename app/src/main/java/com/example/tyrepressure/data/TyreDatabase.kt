package com.example.tyrepressure.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The Room database for the app.
 *
 * @Database lists every @Entity class (table) the database contains, and
 * sets a version number. If you add a new column or table in the future,
 * you must increment the version number and provide a Migration object.
 *
 * This class uses the Singleton pattern — only one instance is ever created.
 * Opening multiple database connections simultaneously can corrupt data, so
 * we guard against that with a synchronized block and an @Volatile flag.
 */
@Database(
    entities = [TyrePressureReading::class, TyreSettings::class],
    version = 1,
    // exportSchema = true writes a JSON schema file to help track migrations.
    // Set to true and configure schemaLocation in build.gradle for production apps.
    exportSchema = false
)
abstract class TyreDatabase : RoomDatabase() {

    // Room generates concrete implementations of these at compile time.
    abstract fun tyrePressureDao(): TyrePressureDao
    abstract fun tyreSettingsDao(): TyreSettingsDao

    companion object {

        /**
         * @Volatile means the value of INSTANCE is always read from and
         * written to main memory, not a CPU cache. This ensures all threads
         * see the same value and prevents two threads from each creating their
         * own database instance.
         */
        @Volatile
        private var INSTANCE: TyreDatabase? = null

        /**
         * Returns the single database instance, creating it if needed.
         *
         * The synchronized(this) block ensures that only one thread can run
         * this code at a time. The second thread to arrive will wait, then
         * see that INSTANCE is already set and return it immediately.
         *
         * @param context The application context. We use the application
         *                context (not an Activity context) to prevent memory
         *                leaks — the database lives as long as the app, not
         *                as long as any single screen.
         */
        fun getDatabase(context: Context): TyreDatabase {
            // If INSTANCE is already set, return it immediately (fast path).
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TyreDatabase::class.java,
                    "tyre_database"          // Filename of the SQLite .db file on disk
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Callback that runs the first time the database file is created.
         * We use it to insert default target pressure settings (32 PSI per tyre).
         *
         * Note: onCreate is NOT called on every app launch — only when the
         * database file is first created (i.e. first install or after clearing
         * app data).
         */
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // By the time onCreate fires, INSTANCE has been set in getDatabase().
                INSTANCE?.let { database ->
                    // Dispatchers.IO runs the coroutine on a background I/O thread.
                    CoroutineScope(Dispatchers.IO).launch {
                        val dao = database.tyreSettingsDao()
                        // Insert a default 32 PSI target for each of the four tyres.
                        TyrePosition.values().forEach { position ->
                            dao.upsertSettings(
                                TyreSettings(
                                    tyrePosition = position.name,
                                    targetPressure = 32f
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
