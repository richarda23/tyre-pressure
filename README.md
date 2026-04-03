# TyrePressure

A simple Android app for tracking car tyre pressures and visualising deflation rates over time.

## Motivation

Slow punctures and faulty tyre valves are easy to miss until a tyre is dangerously low. This app lets you log pressure readings for each tyre and plot them on a chart, making gradual deflation visible before it becomes a problem.

## Screenshots

*(Screenshots to be added after first working build)*

## Features

- Record tyre pressure readings for all four tyres
- Optional: enter post-inflation pressure and current mileage alongside each reading
- Home screen shows the last recorded pressure per tyre with a traffic-light colour indicator (green / amber / red)
- Set your car's recommended target pressure per tyre in Settings
- Line chart showing pressure over time (by calendar date or by mileage)
- Data backed up automatically to Google Drive (Android Auto Backup)

## Tech Stack

| Concern | Technology |
|---|---|
| Language | Kotlin |
| Architecture | MVVM with ViewModel + LiveData |
| Database | Room (SQLite) |
| Charts | MPAndroidChart 3.1.0 |
| Navigation | Navigation Component |
| Min Android version | Android 5.0 (API 21) |
| Target Android version | Android 12L (API 32) |

## Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) Chipmunk (2021.2.1) or later
- Android SDK 32 installed (via Android Studio's SDK Manager)
- A device or emulator running Android 5.0 (API 21) or higher

### Setup

1. Clone the repository:
   ```
   git clone https://github.com/your-username/tyrepressure.git
   ```

2. Open Android Studio and choose **File → Open**, then select the `tyrepressure` folder.

3. Android Studio will detect the Gradle project and download dependencies automatically.
   > **Note:** The first build may take a few minutes while Gradle downloads libraries.

4. If you see an error about a missing `gradle-wrapper.jar`, open a terminal in the project root and run:
   ```
   gradle wrapper --gradle-version 7.3.3
   ```
   This requires Gradle to be installed separately. Alternatively, Android Studio can generate the wrapper automatically.

5. Run the app on a device or emulator using the **Run ▶** button.

No API keys or internet permission are required. All data is stored locally on the device.

## Project Structure

```
app/src/main/java/com/example/tyrepressure/
  data/               Room database: entities, DAOs, database singleton, repository
  ui/home/            Home screen: four tyre buttons with pressure summary
  ui/entry/           Pressure entry screen: record a new measurement
  ui/chart/           Chart screen: visualise deflation trends
  ui/settings/        Settings screen: configure target pressures
  util/               Date formatting and pressure unit helpers
  MainActivity.kt     Single activity hosting the Navigation Component
```

## Maintenance Notes

### Database schema changes
If you modify the Room database schema (add a column, rename a table, add a new entity, etc.) you **must** increment `version` in `TyreDatabase.kt` and provide a migration:

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE tyre_pressure_reading ADD COLUMN notes TEXT")
    }
}
```

Without a migration, Room will throw an `IllegalStateException` on launch. If you use `fallbackToDestructiveMigration()` instead, Room will silently wipe all user data.

Reinstalling the app over an existing installation (e.g. pushing a new debug build via Android Studio) does **not** affect stored data — Android treats it as an update and leaves the database untouched.

## Contributing

This is a personal project but issues and pull requests are welcome. Please open an issue first to discuss significant changes.

## Licence

[MIT](https://choosealicense.com/licenses/mit/)
