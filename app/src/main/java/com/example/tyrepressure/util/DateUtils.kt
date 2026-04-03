package com.example.tyrepressure.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility functions for formatting Unix timestamps into human-readable strings.
 *
 * Android stores dates as Long values (milliseconds since 1 January 1970).
 * These helpers convert those values into readable strings for axis labels
 * and display in the UI.
 */
object DateUtils {

    /**
     * SimpleDateFormat is not thread-safe — if two threads use the same instance
     * simultaneously, they can corrupt each other's output. ThreadLocal gives each
     * thread its own private instance, solving this safely without synchronisation.
     */
    private val dateFormat = ThreadLocal.withInitial {
        SimpleDateFormat("dd MMM yy", Locale.getDefault())
    }

    /**
     * Format a Unix timestamp as a short date string.
     *
     * Example: 1704067200000L → "01 Jan 24"
     *
     * @param timestampMs Milliseconds since the Unix epoch.
     * @return A short date string in the device's locale.
     */
    fun formatTimestamp(timestampMs: Long): String {
        return dateFormat.get()!!.format(Date(timestampMs))
    }
}
