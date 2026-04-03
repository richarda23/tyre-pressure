package com.example.tyrepressure.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores the user's target (recommended) tyre pressure for a single tyre.
 *
 * This table always contains exactly four rows — one per [TyrePosition].
 * The rows are created with a default of 32 PSI when the database is first
 * built (see [TyreDatabase]). The user can change them on the Settings screen.
 *
 * The home screen uses targetPressure to colour-code each tyre button:
 *   Green  = measured pressure is within 10% of target  (tyre is fine)
 *   Amber  = measured pressure is 10–20% below target   (needs attention)
 *   Red    = measured pressure is more than 20% below   (inflate now)
 *
 * @param tyrePosition  The tyre this setting applies to. This is the primary
 *                      key, so there is at most one settings row per tyre.
 * @param targetPressure The recommended pressure in PSI. Defaults to 32 PSI,
 *                      which is typical for many passenger cars.
 */
@Entity(tableName = "tyre_settings")
data class TyreSettings(
    @PrimaryKey
    val tyrePosition: String,
    val targetPressure: Float = 32f
)
