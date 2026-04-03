package com.example.tyrepressure

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Example instrumented test stub.
 *
 * Instrumented tests run on a real Android device or emulator. They have access
 * to the full Android framework — useful for testing Room database operations,
 * UI interactions (via Espresso), or anything that needs a real Context.
 *
 * Run instrumented tests from Android Studio: Run → Run 'ExampleInstrumentedTest'
 * Or from the terminal: ./gradlew connectedAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.tyrepressure", appContext.packageName)
    }
}
