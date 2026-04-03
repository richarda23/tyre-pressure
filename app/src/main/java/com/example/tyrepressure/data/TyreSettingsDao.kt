package com.example.tyrepressure.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Data Access Object (DAO) for tyre target pressure settings.
 *
 * See [TyrePressureDao] for a general explanation of DAOs and threading.
 */
@Dao
interface TyreSettingsDao {

    /**
     * Get the settings for a specific tyre as LiveData.
     *
     * The home screen observes this so the colour-coding updates automatically
     * if the user changes the target pressure in Settings.
     *
     * @param position The tyre position name, e.g. "FRONT_LEFT"
     */
    @Query("SELECT * FROM tyre_settings WHERE tyrePosition = :position")
    fun getSettings(position: String): LiveData<TyreSettings>

    /**
     * Insert or update the settings for a tyre.
     *
     * OnConflictStrategy.REPLACE: if a row with this tyrePosition already
     * exists, replace it entirely. This ensures there is always exactly one
     * settings row per tyre.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(settings: TyreSettings)

    /**
     * Get all four tyre settings rows at once.
     * Used by the Settings screen ViewModel to pre-fill the form.
     */
    @Query("SELECT * FROM tyre_settings")
    suspend fun getAllSettings(): List<TyreSettings>
}
