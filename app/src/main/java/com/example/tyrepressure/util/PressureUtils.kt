package com.example.tyrepressure.util

/**
 * Utility functions for tyre pressure unit conversions.
 *
 * The app stores and displays all pressures in PSI (pounds per square inch),
 * the most common unit in the UK. These helpers are provided in case Bar
 * support is added in a future version.
 *
 * 1 Bar = 14.5038 PSI (approximately).
 */
object PressureUtils {

    private const val PSI_PER_BAR = 14.5038f

    /**
     * Convert a pressure value from Bar to PSI.
     * Example: 2.2 Bar → 31.9 PSI
     */
    fun barToPsi(bar: Float): Float = bar * PSI_PER_BAR

    /**
     * Convert a pressure value from PSI to Bar.
     * Example: 32 PSI → 2.21 Bar
     */
    fun psiToBar(psi: Float): Float = psi / PSI_PER_BAR
}
