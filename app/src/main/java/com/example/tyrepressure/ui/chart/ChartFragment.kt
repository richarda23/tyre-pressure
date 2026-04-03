package com.example.tyrepressure.ui.chart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.tyrepressure.R
import com.example.tyrepressure.data.TyrePosition
import com.example.tyrepressure.databinding.FragmentChartBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

/**
 * Screen for visualising tyre pressure trends.
 *
 * A line chart shows measured pressure (Y-axis) against either:
 *   - Calendar date  — "By Date" mode, default
 *   - Odometer miles — "By Mileage" mode (only readings with mileage entered)
 *
 * A downward slope on the chart indicates deflation. A steep or sudden slope
 * suggests a fault (slow puncture or leaking valve).
 *
 * The user picks which tyre to view with a Spinner, and toggles axis mode
 * with a RadioGroup. Both controls update the chart immediately.
 */
class ChartFragment : Fragment() {

    private var _binding: FragmentChartBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChartViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTyreSpinner()
        setupChartModeToggle()
        setupChart()
        observeData()
    }

    /**
     * Populate the tyre-selector Spinner with the four position display names
     * and notify the ViewModel when the selection changes.
     */
    private fun setupTyreSpinner() {
        val tyreNames = TyrePosition.values().map { it.displayName }

        // ArrayAdapter converts the list of strings into spinner rows.
        // simple_spinner_item and simple_spinner_dropdown_item are built-in
        // Android layouts for standard spinner appearance.
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            tyreNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTyre.adapter = adapter

        binding.spinnerTyre.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                viewModel.selectTyre(TyrePosition.values()[position])
            }
            // Required by the interface but nothing to do when nothing is selected.
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    /**
     * Wire up the By Date / By Mileage radio buttons.
     */
    private fun setupChartModeToggle() {
        binding.radioGroupMode.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioByDate    -> viewModel.setChartMode(ChartMode.BY_DATE)
                R.id.radioByMileage -> viewModel.setChartMode(ChartMode.BY_MILEAGE)
            }
        }
    }

    /**
     * Configure the MPAndroidChart LineChart with sensible visual defaults.
     * Data is loaded separately in [observeData].
     */
    private fun setupChart() {
        binding.lineChart.apply {
            description.isEnabled = false    // Remove the default "Description" label
            setTouchEnabled(true)            // Allow the user to pan and zoom
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)

            // X-axis: labels at the bottom, no vertical grid lines
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f           // Minimum gap between axis labels
            xAxis.setDrawGridLines(false)

            // Left Y-axis: pressure in PSI, minimum 0
            axisLeft.axisMinimum = 0f
            axisLeft.setDrawGridLines(true)

            // Right Y-axis: not needed
            axisRight.isEnabled = false

            legend.isEnabled = false         // Single line per chart — no legend needed
        }
    }

    /**
     * Observe the ViewModel's LiveData and update the chart when data changes.
     */
    private fun observeData() {
        viewModel.chartEntries.observe(viewLifecycleOwner) { entries ->
            if (entries.isEmpty()) {
                binding.lineChart.clear()
                binding.lineChart.setNoDataText("No readings recorded for this tyre yet")
                binding.lineChart.invalidate()
                return@observe
            }

            val dataSet = LineDataSet(entries, "Pressure (PSI)").apply {
                color = ContextCompat.getColor(requireContext(), R.color.chart_line)
                setCircleColor(ContextCompat.getColor(requireContext(), R.color.chart_line))
                lineWidth = 2f
                circleRadius = 4f
                setDrawValues(true)          // Show the PSI number above each data point
                valueTextSize = 10f
            }

            binding.lineChart.data = LineData(dataSet)
            // invalidate() tells Android to redraw the View with the new data.
            binding.lineChart.invalidate()
        }

        // In BY_DATE mode, swap in date strings as X-axis labels.
        viewModel.dateLabels.observe(viewLifecycleOwner) { labels ->
            if (labels.isNotEmpty()) {
                // IndexAxisValueFormatter maps integer X positions (0, 1, 2…)
                // to the corresponding date strings in the list.
                binding.lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            } else {
                // BY_MILEAGE mode: X values are real mileage floats — show them as integers.
                binding.lineChart.xAxis.valueFormatter = DefaultValueFormatter(0)
            }
            binding.lineChart.invalidate()
        }

        // Show/hide the "mileage mode" notice.
        viewModel.chartMode.observe(viewLifecycleOwner) { mode ->
            binding.textMileageNote.visibility =
                if (mode == ChartMode.BY_MILEAGE) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
