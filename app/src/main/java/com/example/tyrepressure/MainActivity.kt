package com.example.tyrepressure

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.tyrepressure.databinding.ActivityMainBinding

/**
 * The single Activity that hosts the entire app.
 *
 * Modern Android apps typically use a "single-activity" pattern: one Activity
 * that acts as a window frame, with Fragments handling each individual screen.
 * The Navigation Component (res/navigation/nav_graph.xml) manages transitions
 * between screens and the back stack.
 *
 * This Activity's responsibilities are minimal:
 *   1. Inflate the layout (which contains the NavHostFragment container)
 *   2. Register the Toolbar as the action bar
 *   3. Connect the action bar to the Navigation Component so titles and
 *      the back arrow update automatically as the user navigates
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding: inflate the layout and get a typed reference to every view.
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Register our custom Toolbar as the app's action bar.
        // This replaces the default title bar and lets us add action buttons.
        setSupportActionBar(binding.toolbar)

        // Find the NavHostFragment — the container that swaps Fragments in and out.
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // AppBarConfiguration tells Navigation which screens are "top-level"
        // (no back arrow shown). The home screen is the only top-level destination.
        appBarConfiguration = AppBarConfiguration(setOf(R.id.homeFragment))

        // Connect the toolbar to the Navigation Component:
        //   - Title updates automatically to match the current Fragment's label
        //   - Back arrow appears automatically on non-top-level screens
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    /**
     * Handle taps on the back arrow (←) in the toolbar.
     *
     * navigateUp() pops the back stack (goes back one screen). If already on
     * the home screen, it falls back to the default Activity up behaviour.
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
