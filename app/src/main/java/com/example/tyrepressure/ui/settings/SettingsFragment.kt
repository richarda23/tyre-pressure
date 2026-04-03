package com.example.tyrepressure.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.tyrepressure.data.TyrePosition
import com.example.tyrepressure.databinding.FragmentSettingsBinding

/**
 * Screen for configuring the target (recommended) pressure for each tyre.
 *
 * The target pressure is used by the home screen to colour-code each tyre button.
 * The user should set it to their car's recommended tyre pressure — usually found
 * in the vehicle handbook or on a sticker inside the driver's door frame.
 *
 * The form has one text input per tyre, pre-filled with the current saved value.
 * Tapping Save stores all four values and returns to the home screen.
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Pre-fill each field with the currently saved target pressure.
        // Only update the fields the first time (when they are empty), so
        // rotation does not overwrite values the user is mid-editing.
        viewModel.settings.observe(viewLifecycleOwner) { settings ->
            if (binding.editTargetFrontLeft.text.isNullOrEmpty()) {
                settings[TyrePosition.FRONT_LEFT]?.let {
                    binding.editTargetFrontLeft.setText(it.toString())
                }
                settings[TyrePosition.FRONT_RIGHT]?.let {
                    binding.editTargetFrontRight.setText(it.toString())
                }
                settings[TyrePosition.REAR_LEFT]?.let {
                    binding.editTargetRearLeft.setText(it.toString())
                }
                settings[TyrePosition.REAR_RIGHT]?.let {
                    binding.editTargetRearRight.setText(it.toString())
                }
            }
        }

        // Navigate back once saving completes.
        viewModel.saveComplete.observe(viewLifecycleOwner) { isDone ->
            if (isDone) findNavController().popBackStack()
        }

        val gitHash = try {
            requireContext().assets.open("git_hash.txt").bufferedReader().readText().trim()
        } catch (e: Exception) {
            "unknown"
        }
        val versionName = requireContext().packageManager
            .getPackageInfo(requireContext().packageName, 0).versionName
        binding.textVersion.text = "Version $versionName ($gitHash)"

        binding.buttonSaveSettings.setOnClickListener {
            saveSettings()
        }
    }

    /**
     * Read the four input fields and pass their values to the ViewModel.
     * Falls back to 32 PSI for any field that is blank or contains invalid input.
     */
    private fun saveSettings() {
        val targets = mapOf(
            TyrePosition.FRONT_LEFT  to (binding.editTargetFrontLeft.text.toString().toFloatOrNull()  ?: 32f),
            TyrePosition.FRONT_RIGHT to (binding.editTargetFrontRight.text.toString().toFloatOrNull() ?: 32f),
            TyrePosition.REAR_LEFT   to (binding.editTargetRearLeft.text.toString().toFloatOrNull()   ?: 32f),
            TyrePosition.REAR_RIGHT  to (binding.editTargetRearRight.text.toString().toFloatOrNull()  ?: 32f)
        )
        viewModel.saveSettings(targets)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
