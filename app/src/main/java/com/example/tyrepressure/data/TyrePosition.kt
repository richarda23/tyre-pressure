package com.example.tyrepressure.data

/**
 * Represents the four wheel positions on a car.
 *
 * Using an enum (a fixed set of named values) ensures that a tyre position
 * is always one of these four options — you can never accidentally store a
 * typo like "FRONT_LOFT" in the database.
 *
 * The [displayName] property is the human-readable label shown in the UI.
 *
 * When stored in Room, we save the enum's .name string (e.g. "FRONT_LEFT")
 * and retrieve it with TyrePosition.valueOf(string).
 */
enum class TyrePosition(val displayName: String) {
    FRONT_LEFT("Front Left"),
    FRONT_RIGHT("Front Right"),
    REAR_LEFT("Rear Left"),
    REAR_RIGHT("Rear Right")
}
