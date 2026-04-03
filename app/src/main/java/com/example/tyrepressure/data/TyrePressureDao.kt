package com.example.tyrepressure.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * Data Access Object (DAO) for tyre pressure readings.
 *
 * A DAO defines the database operations the app can perform. The @Dao annotation
 * tells Room to generate a concrete implementation of this interface at compile
 * time — you never write SQL boilerplate yourself.
 *
 * --- Threads and coroutines ---
 * Room forbids database access on the main (UI) thread because a slow query
 * would freeze the interface. Functions marked 'suspend' must be called from a
 * coroutine, which runs on a background thread automatically.
 *
 * Functions returning LiveData are the exception: they can be called from the
 * main thread. Room moves the query to a background thread internally and
 * delivers results back via the LiveData object. The UI observes the LiveData
 * and updates automatically whenever the underlying data changes.
 */
@Dao
interface TyrePressureDao {

    /**
     * Insert a new pressure reading into the database.
     * Called when the user taps Save on the entry screen.
     */
    @Insert
    suspend fun insertReading(reading: TyrePressureReading)

    /**
     * Get all readings for a specific tyre, ordered oldest-first.
     *
     * Returns LiveData — Room re-queries and emits a new list automatically
     * whenever any row in the table changes. Observers (Fragments) receive
     * updates without polling.
     *
     * @param position The tyre position name, e.g. "FRONT_LEFT"
     */
    @Query("SELECT * FROM tyre_pressure_readings WHERE tyrePosition = :position ORDER BY timestamp ASC")
    fun getReadingsForTyre(position: String): LiveData<List<TyrePressureReading>>

    /**
     * Get only the most recent reading for a tyre.
     * Used by the home screen to show the latest pressure on each button.
     *
     * Returns null if no readings exist yet for this tyre.
     */
    @Query("SELECT * FROM tyre_pressure_readings WHERE tyrePosition = :position ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestReadingForTyre(position: String): TyrePressureReading?

    /**
     * Delete all readings for a specific tyre.
     * Not currently wired to a UI button but provided for future use.
     */
    @Query("DELETE FROM tyre_pressure_readings WHERE tyrePosition = :position")
    suspend fun deleteReadingsForTyre(position: String)
}
