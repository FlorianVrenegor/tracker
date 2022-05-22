package com.example.tracker.weight

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.tracker.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Collections
import kotlin.math.roundToLong

class WeightFragment : Fragment(R.layout.fragment_weight) {

    internal enum class DisplayMode {
        NONE, WEEK, MONTH
    }

    private lateinit var yearWeek: YearWeek
    private lateinit var yearMonth: YearMonth
    private var displayMode = DisplayMode.WEEK
    private var adapter: WeightAdapter? = null
    private var lineChart: LineChart? = null
    private var weightViewModel: WeightViewModel? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val swipeRefreshLayout: SwipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener {
            weightViewModel!!.loadWeights()
        }

        val floatingActionButton: FloatingActionButton = view.findViewById(R.id.fab)
        floatingActionButton.setOnClickListener {
            showAddWeightDialog()
        }

        yearWeek = YearWeek.now()
        yearMonth = YearMonth.now()

        adapter = WeightAdapter()

        val weightListView = view.findViewById<ListView>(R.id.weight_list_view)
        weightListView.isNestedScrollingEnabled = true
        weightListView.adapter = adapter
        weightListView.onItemLongClickListener = OnItemLongClickListener { _, _, position: Int, _ ->
            showDeleteWeightDialog(position)
            false
        }

        val lineChartNavigateBar = view.findViewById<LinearLayout>(R.id.line_chart_navigate_bar)
        val lineChartDescription = view.findViewById<TextView>(R.id.line_chart_description)
        lineChartDescription.text = yearWeek.dayRange

        val lineChartWeek = view.findViewById<Button>(R.id.button_chart_week)
        lineChartWeek.setOnClickListener {
            if (displayMode != DisplayMode.WEEK) {
                yearWeek = YearWeek.now()
                displayMode = DisplayMode.WEEK
            }
            lineChartDescription.text = yearWeek.dayRange
            lineChartNavigateBar.visibility = View.VISIBLE
            setupLineChart()
        }

        val lineChartMonth = view.findViewById<Button>(R.id.button_chart_month)
        lineChartMonth.setOnClickListener {
            if (displayMode != DisplayMode.MONTH) {
                yearMonth = YearMonth.now()
                displayMode = DisplayMode.MONTH
            }
            lineChartDescription.text = yearMonth.format(DateTimeFormatter.ofPattern(yearMonthPattern))
            lineChartNavigateBar.visibility = View.VISIBLE
            setupLineChart()
        }

        val lineChartAll = view.findViewById<Button>(R.id.button_chart_all)
        lineChartAll.setOnClickListener { v: View? ->
            displayMode = DisplayMode.NONE
            lineChartNavigateBar.visibility = View.GONE
            setupLineChartAll()
        }

        val lineChartNextImageView = view.findViewById<ImageView>(R.id.line_chart_navigate_next)
        lineChartNextImageView.setOnClickListener {
            if (displayMode == DisplayMode.WEEK) {
                yearWeek.plusWeek()
                lineChartDescription.text = yearWeek.dayRange
            } else if (displayMode == DisplayMode.MONTH) {
                yearMonth = yearMonth.plusMonths(1)
                lineChartDescription.text = yearMonth.format(DateTimeFormatter.ofPattern(yearMonthPattern))
            }
            setupLineChart()
        }

        val lineChartBeforeImageView = view.findViewById<ImageView>(R.id.line_chart_navigate_before)
        lineChartBeforeImageView.setOnClickListener { v: View? ->
            if (displayMode == DisplayMode.WEEK) {
                yearWeek.minusWeek()
                lineChartDescription.text = yearWeek.dayRange
            } else if (displayMode == DisplayMode.MONTH) {
                yearMonth = yearMonth.minusMonths(1)
                lineChartDescription.text = yearMonth.format(DateTimeFormatter.ofPattern(yearMonthPattern))
            }
            setupLineChart()
        }

        weightViewModel = ViewModelProvider(this)[WeightViewModel::class.java]
        weightViewModel!!.getWeights().observe(viewLifecycleOwner, { weights: List<WeightDto?>? ->
            adapter!!.setWeights(weights)
            setupLineChart()
            swipeRefreshLayout.isRefreshing = false
        })
        weightViewModel!!.loadWeights()
        lineChart = view.findViewById(R.id.line_chart)
    }

    private fun showAddWeightDialog() {
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED

        val layoutName = LinearLayout(context)
        layoutName.orientation = LinearLayout.VERTICAL
        layoutName.setPadding(60, 0, 60, 0)
        layoutName.addView(input)

        val adb = AlertDialog.Builder(context!!)
        adb.setTitle("Gewicht eingeben")
        adb.setView(layoutName)
        adb.setPositiveButton("OK") { _, _ ->
            val weightString = input.text.toString()
            if (weightString.isNotEmpty()) {
                weightViewModel!!.saveWeight(WeightDto(System.currentTimeMillis(), weightString.toDouble()))
            }
        }
        adb.setNegativeButton("Abbrechen") { dialog, _ -> dialog.cancel() }
        adb.show()
    }

    private fun showDeleteWeightDialog(position: Int) {
        val adb = AlertDialog.Builder(activity!!)
        adb.setTitle("Delete?")
        adb.setMessage("Are you sure you want to delete this entry?")
        adb.setNegativeButton("Cancel", null)
        adb.setPositiveButton("Ok") { _, _ ->
            weightViewModel!!.deleteWeight(adapter!!.weights[position])
            adapter!!.weights.removeAt(position)
            adapter!!.notifyDataSetChanged()
        }
        adb.show()
    }

    private fun setupLineChart() {
        when (displayMode) {
            DisplayMode.WEEK -> {
                setupLineChartWeek(yearWeek)
            }
            DisplayMode.MONTH -> {
                setupLineChartMonth(yearMonth)
            }
            DisplayMode.NONE -> {
                setupLineChartAll()
            }
        }
        adapter!!.weights.sortWith(Comparator.reverseOrder())
    }

    private fun setupLineChartWeek(yearWeek: YearWeek?) {
        val xAxis = lineChart!!.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f // only intervals of 1 day
        xAxis.labelCount = 7
        xAxis.axisMinimum = -0.25f
        xAxis.axisMaximum = 6.25f
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(false)
        xAxis.valueFormatter = object : ValueFormatter() {
            val days = arrayOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")
            override fun getFormattedValue(value: Float): String {
                return days[value.toInt() % 7]
            }
        }
        val yVals = ArrayList<Entry>()
        adapter!!.weights.sort()
        var hasBefore = false
        for (i in adapter!!.weights.indices) {
            val dto = adapter!!.weights[i]
            if (dto.week == yearWeek!!.week) {
                if (i > 0 && yVals.size == 0) {
                    yVals.add(Entry(-1f, adapter!!.weights[i - 1].weightInKgs.toFloat()))
                    hasBefore = true
                }
                yVals.add(
                    Entry(dto.dayOfWeek.toFloat(), dto.weightInKgs.toFloat())
                )
                if (adapter!!.weights.size > i + 1 && yVals.size == (if (hasBefore) 8 else 7)) {
                    val dto2 = adapter!!.weights[i + 1]
                    yVals.add(Entry(7f, dto2.weightInKgs.toFloat()))
                }
            }
        }
        adapter!!.weights.sortWith(Collections.reverseOrder())
        yVals.sortWith { e1: Entry, e2: Entry -> (e1.x - e2.x).toInt() }
        val dataSet = LineDataSet(yVals, "Weights")
        // dataSet.setDrawCircles(false);
        dataSet.circleRadius = 3f
        dataSet.setDrawCircleHole(false)
        dataSet.lineWidth = 2f
        dataSet.setDrawValues(false)
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // For smooth curve
        dataSet.setDrawFilled(true)
        val drawable = ContextCompat.getDrawable(context!!, R.drawable.gradient_linechart_background)
        dataSet.fillDrawable = drawable
        // dataSet.setFillAlpha(0);
        val lineData = LineData(dataSet)
        // lineChart.setXAxisRenderer();
        val leftAxis = lineChart!!.axisLeft
        leftAxis.setDrawAxisLine(false)
        leftAxis.granularity = 1f
        // leftAxis.setAxisMinimum(lineData.getYMin() - 0.25f);
        // leftAxis.setAxisMaximum(lineData.getYMax() + 0.25f);
        leftAxis.axisMinimum = 85 - 0.25f
        leftAxis.axisMaximum = 90 + 0.25f
        val rightAxis = lineChart!!.axisRight
        rightAxis.setDrawAxisLine(false)
        rightAxis.setDrawLabels(false)
        rightAxis.setDrawGridLines(false)
        // rightAxis.setAxisLineWidth(0);
        // rightAxis.setXOffset(-5f);
        lineChart!!.description.isEnabled = false
        lineChart!!.legend.isEnabled = false
        lineChart!!.data = lineData
        lineChart!!.setTouchEnabled(false)
        lineChart!!.invalidate() // So the chart refreshes and you don't have to click it
        lineChart!!.setExtraOffsets(5f, 10f, 0f, 10f)
    }

    private fun setupLineChartMonth(yearMonth: YearMonth?) {
        adapter!!.weights.sort()
        val yVals = ArrayList<Entry>()
        for (i in adapter!!.weights.indices) {
            val dto = adapter!!.weights[i]
            if (dto.month == yearMonth!!.month.value && dto.year == yearMonth.year) {
                if (i - 1 >= 0 && yVals.size == 0) {
                    yVals.add(Entry(0f, adapter!!.weights[i - 1].weightInKgs.toFloat()))
                }
                yVals.add(Entry(dto.dayInMonth.toFloat(), dto.weightInKgs.toFloat()))
                if (adapter!!.weights.size >= i + 1 && yVals.size == 28) {
                    yVals.add(Entry(7f, adapter!!.weights[i + 1].weightInKgs.toFloat()))
                }
            }
        }
        // yVals.sort((e1, e2) -> (int) (e1.getX() - e2.getX()));
        val xAxis = lineChart!!.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 5f
        xAxis.axisMinimum = 0.75f
        val max = yearMonth!!.lengthOfMonth() + 0.25f
        xAxis.axisMaximum = max
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(false)
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.roundToLong().toString() + ""
            }
        }
        val dataSet = LineDataSet(yVals, "Weights")
        // dataSet.setDrawCircles(false);
        dataSet.circleRadius = 3f
        dataSet.setDrawCircleHole(false)
        dataSet.lineWidth = 2f
        dataSet.setDrawValues(false)
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // For smooth curve
        dataSet.setDrawFilled(true)
        val drawable = ContextCompat.getDrawable(context!!, R.drawable.gradient_linechart_background)
        dataSet.fillDrawable = drawable
        // dataSet.setFillAlpha(0);
        val lineData = LineData(dataSet)
        // lineChart.setXAxisRenderer();
        val leftAxis = lineChart!!.axisLeft
        leftAxis.setDrawAxisLine(false)
        leftAxis.granularity = 0.5f
        // leftAxis.setAxisMinimum(lineData.getYMin() - 0.25f);
        // leftAxis.setAxisMaximum(lineData.getYMax() + 0.25f);
        leftAxis.axisMinimum = 85 - 0.25f
        leftAxis.axisMaximum = 90 + 0.25f
        val rightAxis = lineChart!!.axisRight
        rightAxis.setDrawAxisLine(false)
        rightAxis.setDrawLabels(false)
        rightAxis.setDrawGridLines(false)
        // rightAxis.setAxisLineWidth(0);
        // rightAxis.setXOffset(-5f);
        lineChart!!.description.isEnabled = false
        lineChart!!.legend.isEnabled = false
        lineChart!!.data = lineData
        lineChart!!.setTouchEnabled(false)
        lineChart!!.invalidate() // So the chart refreshes and you don't have to click it
        lineChart!!.setExtraOffsets(5f, 10f, 0f, 10f)
    }

    private fun setupLineChartAll() {
        adapter!!.weights.sort()
        val yVals = ArrayList<Entry>()
        val x: MutableList<Int> = ArrayList()
        val y: MutableList<Float> = ArrayList()
        var xSum = 0
        var ySum = 0f
        var crossDeviationSum = 0f
        var deviationSum = 0f
        for (i in adapter!!.weights.indices) {
            yVals.add(Entry(i.toFloat(), adapter!!.weights[i].weightInKgs.toFloat()))
            val yVal = adapter!!.weights[i].weightInKgs.toFloat()
            x.add(i)
            y.add(yVal)
            xSum += i
            ySum += yVal
            crossDeviationSum += i * yVal
            deviationSum += (i * i).toFloat()
        }
        // yVals.sort((e1, e2) -> (int) (e1.getX() - e2.getX()));
        val n = x.size
        val xMean = xSum.toFloat() / n
        val yMean = ySum / n
        val b1 = (crossDeviationSum - n * xMean * yMean) / (deviationSum - n * xMean * xMean)
        val b0 = yMean - b1 * xMean
        val yValsRegression = ArrayList<Entry>()
        for (i in adapter!!.weights.indices) {
            yValsRegression.add(Entry(i.toFloat(), b0 + i * b1))
        }
        val xAxis = lineChart!!.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.axisMinimum = -0.25f
        xAxis.axisMaximum = yVals.size - 1 + 0.25f
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(false)
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.roundToLong().toString() + ""
            }
        }
        val regressionDataSet = LineDataSet(yValsRegression, "Average")
        regressionDataSet.setDrawCircles(false)
        regressionDataSet.lineWidth = 2f
        regressionDataSet.setDrawValues(false)
        regressionDataSet.enableDashedLine(50f, 25f, 0f)
        regressionDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // Has to be set to display dashed line
        regressionDataSet.color = ContextCompat.getColor(context!!, R.color.colorPrimaryInvert)
        val dataSet = LineDataSet(yVals, "Weights")
        // dataSet.setDrawCircles(false);
        dataSet.circleRadius = 3f
        dataSet.setDrawCircleHole(false)
        dataSet.lineWidth = 2f
        dataSet.setDrawValues(false)
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // For smooth curve
        dataSet.setDrawFilled(true)
        val drawable = ContextCompat.getDrawable(context!!, R.drawable.gradient_linechart_background)
        dataSet.fillDrawable = drawable
        // dataSet.setFillAlpha(0);
        val lineData = LineData(dataSet, regressionDataSet)
        // lineChart.setXAxisRenderer();
        val leftAxis = lineChart!!.axisLeft
        leftAxis.setDrawAxisLine(false)
        leftAxis.granularity = 0.5f
        // leftAxis.setAxisMinimum(lineData.getYMin() - 0.25f);
        // leftAxis.setAxisMaximum(lineData.getYMax() + 0.25f);
        leftAxis.axisMinimum = 85 - 0.25f
        leftAxis.axisMaximum = 90 + 0.25f
        val rightAxis = lineChart!!.axisRight
        rightAxis.setDrawAxisLine(false)
        rightAxis.setDrawLabels(false)
        rightAxis.setDrawGridLines(false)
        // rightAxis.setAxisLineWidth(0);
        // rightAxis.setXOffset(-5f);
        lineChart!!.description.isEnabled = false
        lineChart!!.legend.isEnabled = false
        lineChart!!.data = lineData
        lineChart!!.setTouchEnabled(false)
        lineChart!!.invalidate() // So the chart refreshes and you don't have to click it
        lineChart!!.setExtraOffsets(5f, 10f, 0f, 10f)
        adapter!!.weights.sortWith(Comparator.reverseOrder())
    }

    companion object {
        private const val yearMonthPattern = "MMMM yyyy"
    }
}