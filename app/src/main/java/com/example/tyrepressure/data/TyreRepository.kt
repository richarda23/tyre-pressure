package com.example.tyrepressure.data

import androidx.lifecycle.LiveData

/**
 * Repository: a single source of truth for all tyre data.
 *
 * The Repository sits between the ViewModels (UI logic) and the DAOs (database).
 * Its purpose is to hide which data source is being used. Right now everything
 * comes from Room, but if you later added a CSV export or a network sync, you'd
 * add that logic here without touching any ViewModel.
 *
 * It also keeps ViewModels clean — they call repository.insertReading(...) without
 * needing to know anything about Room, threads, or DAOs.
 *
 * @param pressureDao  DAO for reading and writing pressure measurements.
 * @param settingsDao  DAO for reading and writing target pressure settings.
 */
class TyreRepository(
    private val pressureDao: TyrePressureDao,
    private val settingsDao: TyreSettingsDao
) {

    // -------------------------------------------------------------------------
    // Pressure readings
    // -------------------------------------------------------------------------

    /**
     * Save a new tyre pressure reading to the database.
     * Must be called from a coroutine (the ViewModel handles this).
     */
    suspend fun insertReading(reading: TyrePressureReading) {
        pressureDao.insertReading(reading)
    }

    /**
     * Get all readings for a tyre as an auto-updating LiveData list.
     * Readings are ordered oldest-first for left-to-right chart rendering.
     */
    fun getReadingsForTyre(position: TyrePosition): LiveData<List<TyrePressureReading>> {
        return pressureDao.getReadingsForTyre(position.name)
    }

    /**
     * Get the most recent reading for a tyre.
     * Returns null if no readings have been recorded yet.
     */
    suspend fun getLatestReadingForTyre(position: TyrePosition): TyrePressureReading? {
        return pressureDao.getLatestReadingForTyre(position.name)
    }

    // -------------------------------------------------------------------------
    // Settings (target pressures)
    // -------------------------------------------------------------------------

    /**
     * Get LiveData for a tyre's settings.
     * Observers are notified automatically if the user changes the target pressure.
     */
    fun getSettingsForTyre(position: TyrePosition): LiveData<TyreSettings> {
        return settingsDao.getSettings(position.name)
    }

    /**
     * Save (insert or update) target pressure settings for a tyre.
     */
    suspend fun upsertSettings(settings: TyreSettings) {
        settingsDao.upsertSettings(settings)
    }

    /**
     * Get all four tyre settings rows at once.
     * Used by the Settings screen to pre-fill the form fields.
     */
    suspend fun getAllSettings(): List<TyreSettings> {
        return settingsDao.getAllSettings()
    }
}
