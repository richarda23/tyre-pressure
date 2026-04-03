package com.example.tyrepressure.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tyrepressure.data.TyreDatabase
import com.example.tyrepressure.data.TyrePosition
import com.example.tyrepressure.data.TyrePressureReading
import com.example.tyrepressure.data.TyreRepository
import com.example.tyrepressure.data.TyreSettings
import kotlinx.coroutines.launch

/**
 * ViewModel for the home screen.
 *
 * --- Why extend AndroidViewModel instead of ViewModel? ---
 * AndroidViewModel receives the Application object in its constructor. We need
 * it to build the database singleton (TyreDatabase.getDatabase(application)).
 * Plain ViewModel has no access to Context.
 *
 * --- Why use a ViewModel at all? ---
 * When the user rotates their phone, Android destroys and recreates the Fragment.
 * Without a ViewModel the Fragment would re-query the database from scratch on
 * every rotation. The ViewModel survives rotations — it is only destroyed when
 * the user leaves the screen permanently (back press, etc.).
 *
 * Exposes:
 *   [latestReadings] — most recent pressure reading per tyre (updated on refresh)
 *   [settings]       — target pressure settings per tyre (live from the database)
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TyreRepository

    // Private mutable LiveData for latest readings. We expose read-only LiveData
    // to the Fragment so only this ViewModel can update the values.
    private val _latestReadings: Map<TyrePosition, MutableLiveData<TyrePressureReading?>> =
        TyrePosition.values().associateWith { MutableLiveData(null) }

    val latestReadings: Map<TyrePosition, LiveData<TyrePressureReading?>> = _latestReadings

    // Settings LiveData comes directly from Room via the repository.
    // Room will emit a new value automatically if the user changes their target pressure.
    val settings: Map<TyrePosition, LiveData<TyreSettings>>

    init {
        val db = TyreDatabase.getDatabase(application)
        repository = TyreRepository(db.tyrePressureDao(), db.tyreSettingsDao())

        settings = TyrePosition.values().associateWith { position ->
            repository.getSettingsForTyre(position)
        }

        // Load the latest reading for every tyre when the ViewModel is first created.
        loadLatestReadings()
    }

    /**
     * Fetch the most recent pressure reading for all four tyres from the database.
     *
     * viewModelScope.launch runs the code on a background thread. The result is
     * posted back to the main thread via postValue(). The Fragment's LiveData
     * observer then updates the UI.
     */
    private fun loadLatestReadings() {
        viewModelScope.launch {
            TyrePosition.values().forEach { position ->
                val latest = repository.getLatestReadingForTyre(position)
                _latestReadings[position]?.postValue(latest)
            }
        }
    }

    /**
     * Reload the latest readings from the database.
     *
     * Called from [HomeFragment.onResume] so the home screen reflects any
     * reading the user just recorded on the entry screen.
     */
    fun refresh() {
        loadLatestReadings()
    }
}
