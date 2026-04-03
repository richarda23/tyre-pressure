package com.example.tyrepressure.ui.entry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.tyrepressure.data.TyrePosition
import com.example.tyrepressure.databinding.FragmentPressureEntryBinding

/**
 * Screen for recording a new tyre pressure measurement.
 *
 * The user fills in:
 *   - Measured Pressure (PSI) — required
 *   - Inflated Pressure (PSI) — optional (only if they topped the tyre up)
 *   - Current Mileage          — optional (enables mileage-based charting)
 *
 * On tapping Save, inputs are validated, the reading is stored via the
 * ViewModel, and the user is returned to the home screen.
 *
 * --- Safe Args ---
 * The tyre position is passed from the home screen using the Navigation
 * Component's Safe Args feature. Safe Args generates [PressureEntryFragmentArgs]
 * from the <argument> tag in nav_graph.xml — it is a type-safe, compile-time-
 * checked way to pass data between screens.
 */
class PressureEntryFragment : Fragment() {

    private var _binding: FragmentPressureEntryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PressureEntryViewModel by viewModels()

    // Safe Args: retrieves arguments passed when navigating to this screen.
    // 'by navArgs()' is a Kotlin property delegate that lazily reads the Bundle.
    private val args: PressureEntryFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPressureEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Convert the String argument back to the enum value.
        val position = TyrePosition.valueOf(args.tyrePosition)

        // Show which tyre is being recorded at the top of the form.
        binding.textTyreLabel.text = "${position.displayName} Tyre"

        // When the ViewModel signals that the save is done, navigate back.
        viewModel.saveComplete.observe(viewLifecycleOwner) { isDone ->
            if (isDone) {
                // popBackStack() removes the current screen from the back stack,
                // returning to the home screen.
                findNavController().popBackStack()
            }
        }

        binding.buttonSave.setOnClickListener {
            saveReading(position)
        }
    }

    /**
     * Read and validate the form inputs, then hand them to the ViewModel.
     */
    private fun saveReading(position: TyrePosition) {
        // --- Measured pressure (required) ---
        val measuredText = binding.editMeasuredPressure.text.toString().trim()
        if (measuredText.isEmpty()) {
            binding.layoutMeasuredPressure.error = "Please enter the measured pressure"
            return
        }
        val measuredPressure = measuredText.toFloatOrNull()
        if (measuredPressure == null || measuredPressure <= 0f) {
            binding.layoutMeasuredPressure.error = "Please enter a valid pressure (e.g. 32.5)"
            return
        }
        // Clear any previous error message now that the value is valid.
        binding.layoutMeasuredPressure.error = null

        // --- Inflated pressure (optional) ---
        // toFloatOrNull() returns null if the text is blank or not a number.
        val inflatedText = binding.editInflatedPressure.text.toString().trim()
        val inflatedPressure: Float? = if (inflatedText.isEmpty()) null
        else inflatedText.toFloatOrNull()?.takeIf { it > 0f }

        // --- Mileage (optional) ---
        val mileageText = binding.editMileage.text.toString().trim()
        val mileage: Int? = if (mileageText.isEmpty()) null
        else mileageText.toIntOrNull()?.takeIf { it >= 0 }

        viewModel.saveReading(position, measuredPressure, inflatedPressure, mileage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
