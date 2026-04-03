package com.example.tyrepressure.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single tyre pressure measurement recorded by the user.
 *
 * The @Entity annotation tells Room to create a database table called
 * "tyre_pressure_readings". Each property becomes a column in that table.
 * Every time the user records a pressure, one new row is inserted.
 *
 * @param id               Auto-generated unique ID. Room sets this automatically
 *                         using autoGenerate = true — you never set it yourself.
 * @param tyrePosition     Which tyre was measured, stored as a String (the enum's
 *                         .name, e.g. "FRONT_LEFT"). See [TyrePosition].
 * @param measuredPressure The pressure in PSI before any inflation. Required.
 * @param inflatedPressure The pressure in PSI after topping up the tyre. Null
 *                         if the user did not inflate.
 * @param mileage          The odometer reading in miles at the time of measurement.
 *                         Null if the user chose not to enter it.
 * @param timestamp        When this reading was recorded, as milliseconds since
 *                         1 January 1970 (Unix time). Set automatically to
 *                         System.currentTimeMillis() in the ViewModel.
 */
@Entity(tableName = "tyre_pressure_readings")
data class TyrePressureReading(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tyrePosition: String,
    val measuredPressure: Float,
    val inflatedPressure: Float?,
    val mileage: Int?,
    val timestamp: Long
)
