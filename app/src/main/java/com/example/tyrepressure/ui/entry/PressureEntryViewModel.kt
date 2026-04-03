package com.example.tyrepressure.ui.entry

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tyrepressure.data.TyreDatabase
import com.example.tyrepressure.data.TyrePosition
import com.example.tyrepressure.data.TyrePressureReading
import com.example.tyrepressure.data.TyreRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for the pressure entry screen.
 *
 * Handles inserting a new [TyrePressureReading] into the database, and exposes
 * a LiveData flag that the Fragment watches to know when the save is complete.
 */
class PressureEntryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TyreRepository

    // The Fragment observes this. When it becomes true, the Fragment navigates back.
    private val _saveComplete = MutableLiveData(false)
    val saveComplete: LiveData<Boolean> = _saveComplete

    init {
        val db = TyreDatabase.getDatabase(application)
        repository = TyreRepository(db.tyrePressureDao(), db.tyreSettingsDao())
    }

    /**
     * Build a [TyrePressureReading] from the user's input and save it.
     *
     * The timestamp is recorded automatically as the current time.
     *
     * viewModelScope.launch runs the database insert on a background thread.
     * The coroutine is automatically cancelled if the ViewModel is cleared (i.e.
     * the user navigates away before the save completes).
     *
     * @param position         Which tyre was measured.
     * @param measuredPressure PSI reading before inflation. Required.
     * @param inflatedPressure PSI reading after topping up. Null if not inflated.
     * @param mileage          Odometer reading in miles. Null if not entered.
     */
    fun saveReading(
        position: TyrePosition,
        measuredPressure: Float,
        inflatedPressure: Float?,
        mileage: Int?
    ) {
        viewModelScope.launch {
            val reading = TyrePressureReading(
                tyrePosition      = position.name,
                measuredPressure  = measuredPressure,
                inflatedPressure  = inflatedPressure,
                mileage           = mileage,
                // System.currentTimeMillis() returns the current time in milliseconds
                // since the Unix epoch (1 January 1970). Room stores it as a Long.
                timestamp         = System.currentTimeMillis()
            )
            repository.insertReading(reading)
            // postValue posts to the main thread from a background coroutine.
            // setValue would crash if called from a background thread.
            _saveComplete.postValue(true)
        }
    }
}
