package com.example.tyrepressure.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.tyrepressure.R
import com.example.tyrepressure.data.TyrePosition
import com.example.tyrepressure.data.TyrePressureReading
import com.example.tyrepressure.data.TyreSettings
import com.example.tyrepressure.databinding.FragmentHomeBinding
import com.google.android.material.button.MaterialButton

/**
 * The home screen — four tyre buttons in a 2×2 grid.
 *
 * Each button shows:
 *   - The tyre's position label (e.g. "Front Left")
 *   - The most recently recorded pressure in PSI (or "-- PSI" if none yet)
 *   - A background colour indicating pressure status relative to target:
 *       Green  = within 10% of target
 *       Amber  = 10–20% below target
 *       Red    = more than 20% below target
 *       Grey   = no readings recorded yet
 *
 * Tapping a button navigates to the pressure entry screen for that tyre.
 * The Chart and Settings icons in the toolbar navigate to those screens.
 */
class HomeFragment : Fragment() {

    // _binding is nullable so we can null it out in onDestroyView to avoid
    // memory leaks. The 'binding' getter asserts non-null for convenience.
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // 'by viewModels()' creates (or retrieves) the ViewModel scoped to this Fragment.
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tell Android this Fragment contributes items to the options menu (toolbar).
        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)

        // Map each TyrePosition to its corresponding button in the layout.
        val tyreButtons: Map<TyrePosition, MaterialButton> = mapOf(
            TyrePosition.FRONT_LEFT  to binding.buttonFrontLeft,
            TyrePosition.FRONT_RIGHT to binding.buttonFrontRight,
            TyrePosition.REAR_LEFT   to binding.buttonRearLeft,
            TyrePosition.REAR_RIGHT  to binding.buttonRearRight
        )

        // Wire up tap handlers: each button navigates to the entry screen
        // for the corresponding tyre, passing the position as a Safe Args argument.
        tyreButtons.forEach { (position, button) ->
            button.setOnClickListener {
                val action = HomeFragmentDirections.actionHomeToEntry(position.name)
                findNavController().navigate(action)
            }
        }

        // Observe latest readings: update button text when a reading changes.
        TyrePosition.values().forEach { position ->
            viewModel.latestReadings[position]?.observe(viewLifecycleOwner) { reading ->
                updateButton(
                    button   = tyreButtons.getValue(position),
                    position = position,
                    reading  = reading,
                    settings = viewModel.settings[position]?.value
                )
            }

            // Observe settings: update colour-coding if the target pressure changes.
            viewModel.settings[position]?.observe(viewLifecycleOwner) { settings ->
                updateButton(
                    button   = tyreButtons.getValue(position),
                    position = position,
                    reading  = viewModel.latestReadings[position]?.value,
                    settings = settings
                )
            }
        }
    }

    /**
     * Update a tyre button's label and background colour.
     *
     * @param button   The MaterialButton to update.
     * @param position The tyre this button represents.
     * @param reading  The most recent pressure reading (null if none yet).
     * @param settings The target pressure settings (null until loaded from DB).
     */
    private fun updateButton(
        button: MaterialButton,
        position: TyrePosition,
        reading: TyrePressureReading?,
        settings: TyreSettings?
    ) {
        if (reading == null) {
            button.text = "${position.displayName}\n-- PSI"
            button.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.tyre_no_data)
            )
            return
        }

        val pressure = reading.measuredPressure
        // String.format("%.1f", pressure) rounds to one decimal place (e.g. "31.5")
        button.text = "${position.displayName}\n${String.format("%.1f", pressure)} PSI"

        // Fall back to 32 PSI if settings haven't loaded from the DB yet.
        val target = settings?.targetPressure ?: 32f

        // ratio = 1.0 means exactly at target; 0.9 means 10% below, etc.
        val ratio = pressure / target
        val colourRes = when {
            ratio >= 0.90f -> R.color.tyre_ok        // green: within 10%
            ratio >= 0.80f -> R.color.tyre_low        // amber: 10–20% below
            else           -> R.color.tyre_critical   // red: more than 20% below
        }
        button.setBackgroundColor(ContextCompat.getColor(requireContext(), colourRes))
    }

    @Deprecated("Deprecated in API 28 but still correct for minSdk 21")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu)
    }

    @Deprecated("Deprecated in API 28 but still correct for minSdk 21")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_chart -> {
                findNavController().navigate(R.id.action_home_to_chart)
                true
            }
            R.id.action_settings -> {
                findNavController().navigate(R.id.action_home_to_settings)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh readings when returning from the entry screen so the home
        // screen immediately reflects any newly saved reading.
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Null out the binding to release references to the Views, which are
        // destroyed when the Fragment's view is destroyed (e.g. during navigation).
        // Failing to do this causes a memory leak.
        _binding = null
    }
}
