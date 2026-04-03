package com.example.tyrepressure.ui.chart

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.tyrepressure.data.TyreDatabase
import com.example.tyrepressure.data.TyrePosition
import com.example.tyrepressure.data.TyrePressureReading
import com.example.tyrepressure.data.TyreRepository
import com.example.tyrepressure.util.DateUtils
import com.github.mikephil.charting.data.Entry

/**
 * Which axis mode the chart is currently using.
 */
enum class ChartMode {
    /** X-axis shows calendar dates derived from each reading's timestamp. */
    BY_DATE,
    /** X-axis shows odometer readings in miles. Readings without mileage are excluded. */
    BY_MILEAGE
}

/**
 * ViewModel for the deflation chart screen.
 *
 * Transforms raw [TyrePressureReading] lists from the database into
 * [Entry] objects that MPAndroidChart can plot directly.
 *
 * Uses [MediatorLiveData] to react to two independent sources:
 *   1. New readings arriving from Room (via [rawReadings])
 *   2. The user switching chart mode or tyre selection
 *
 * Whenever either source changes, [chartEntries] is recalculated.
 */
class ChartViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TyreRepository

    // Which tyre the user currently wants to see. Defaults to Front Left.
    val selectedTyre = MutableLiveData(TyrePosition.FRONT_LEFT)

    // Whether the X-axis shows dates or mileage.
    val chartMode = MutableLiveData(ChartMode.BY_DATE)

    // The raw readings from Room for the currently selected tyre.
    // MediatorLiveData lets us swap the underlying source when the tyre changes.
    private val rawReadings = MediatorLiveData<List<TyrePressureReading>>()
    private var currentReadingsSource: LiveData<List<TyrePressureReading>>? = null

    /**
     * Processed chart entries ready for MPAndroidChart.
     *
     * Recalculated whenever [rawReadings] or [chartMode] changes.
     */
    val chartEntries: LiveData<List<Entry>> = MediatorLiveData<List<Entry>>().also { mediator ->
        mediator.addSource(rawReadings) { readings ->
            mediator.value = toChartEntries(readings, chartMode.value ?: ChartMode.BY_DATE)
        }
        mediator.addSource(chartMode) { mode ->
            mediator.value = toChartEntries(rawReadings.value.orEmpty(), mode)
        }
    }

    /**
     * Date strings for the X-axis labels in BY_DATE mode.
     *
     * MPAndroidChart uses float indices (0f, 1f, 2f…) for the X-axis internally.
     * [IndexAxisValueFormatter] maps those indices to these label strings.
     */
    val dateLabels = MutableLiveData<List<String>>(emptyList())

    init {
        val db = TyreDatabase.getDatabase(application)
        repository = TyreRepository(db.tyrePressureDao(), db.tyreSettingsDao())

        // When the selected tyre changes, swap the data source.
        // observeForever (no LifecycleOwner) is needed here because we're inside
        // a ViewModel, not a Fragment. We remove the observer in onCleared().
        selectedTyre.observeForever { position ->
            // Remove the old data source from rawReadings before adding the new one.
            currentReadingsSource?.let { rawReadings.removeSource(it) }
            val newSource = repository.getReadingsForTyre(position)
            currentReadingsSource = newSource
            rawReadings.addSource(newSource) { rawReadings.value = it }
        }
    }

    /**
     * Convert a list of raw readings into MPAndroidChart [Entry] objects.
     *
     * BY_DATE:    X = integer index (0, 1, 2…), Y = pressure PSI.
     *             Date strings are stored separately in [dateLabels].
     * BY_MILEAGE: X = mileage value, Y = pressure PSI.
     *             Readings with null mileage are excluded.
     */
    private fun toChartEntries(
        readings: List<TyrePressureReading>,
        mode: ChartMode
    ): List<Entry> {
        return when (mode) {
            ChartMode.BY_DATE -> {
                // Build the date label list to hand to the X-axis formatter.
                dateLabels.postValue(
                    readings.map { DateUtils.formatTimestamp(it.timestamp) }
                )
                // mapIndexed gives us both the index and the reading in one pass.
                readings.mapIndexed { index, reading ->
                    Entry(index.toFloat(), reading.measuredPressure)
                }
            }

            ChartMode.BY_MILEAGE -> {
                dateLabels.postValue(emptyList())
                // Filter out readings where the user did not enter a mileage value.
                readings
                    .filter { it.mileage != null }
                    .map { reading ->
                        Entry(reading.mileage!!.toFloat(), reading.measuredPressure)
                    }
            }
        }
    }

    /** Switch to a different tyre. [rawReadings] will update automatically. */
    fun selectTyre(position: TyrePosition) {
        selectedTyre.value = position
    }

    /** Switch between date and mileage axis modes. */
    fun setChartMode(mode: ChartMode) {
        chartMode.value = mode
    }

    override fun onCleared() {
        super.onCleared()
        // Remove the observeForever observer to prevent a memory leak.
        currentReadingsSource?.let { rawReadings.removeSource(it) }
    }
}
