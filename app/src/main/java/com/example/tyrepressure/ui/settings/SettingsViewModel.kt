package com.example.tyrepressure.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tyrepressure.data.TyreDatabase
import com.example.tyrepressure.data.TyrePosition
import com.example.tyrepressure.data.TyreRepository
import com.example.tyrepressure.data.TyreSettings
import kotlinx.coroutines.launch

/**
 * ViewModel for the settings screen.
 *
 * Loads the current target pressure for all four tyres and saves updated
 * values when the user taps Save.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TyreRepository

    // Current settings, keyed by TyrePosition.
    // The Fragment uses this to pre-fill the form fields on first load.
    private val _settings = MutableLiveData<Map<TyrePosition, Float>>()
    val settings: LiveData<Map<TyrePosition, Float>> = _settings

    // The Fragment navigates back when this becomes true.
    private val _saveComplete = MutableLiveData(false)
    val saveComplete: LiveData<Boolean> = _saveComplete

    init {
        val db = TyreDatabase.getDatabase(application)
        repository = TyreRepository(db.tyrePressureDao(), db.tyreSettingsDao())
        loadSettings()
    }

    /**
     * Fetch all tyre settings from the database and post them to [settings].
     */
    private fun loadSettings() {
        viewModelScope.launch {
            val allSettings = repository.getAllSettings()
            // Convert the list to a map keyed by TyrePosition for easy lookup.
            val map = allSettings.associate { s ->
                TyrePosition.valueOf(s.tyrePosition) to s.targetPressure
            }
            _settings.postValue(map)
        }
    }

    /**
     * Save new target pressures for all four tyres.
     *
     * @param targets Map of TyrePosition → new target pressure in PSI.
     */
    fun saveSettings(targets: Map<TyrePosition, Float>) {
        viewModelScope.launch {
            targets.forEach { (position, pressure) ->
                repository.upsertSettings(
                    TyreSettings(tyrePosition = position.name, targetPressure = pressure)
                )
            }
            _saveComplete.postValue(true)
        }
    }
}
