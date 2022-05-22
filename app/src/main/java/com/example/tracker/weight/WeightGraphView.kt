package com.example.tracker.weight

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.tracker.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Collections
import kotlin.math.roundToLong

class WeightGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    internal enum class DisplayMode {
        NONE, WEEK, MONTH
    }

    private lateinit var yearWeek: YearWeek
    private lateinit var yearMonth: YearMonth
    private lateinit var weights: ArrayList<WeightDto>
    private lateinit var weightViewModel: WeightViewModel
    private lateinit var lineChart: LineChart

    private var displayMode = DisplayMode.WEEK

    private fun initializeViews() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.view_weight_graph, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        orientation = VERTICAL

        weightViewModel = ViewModelProvider(context as FragmentActivity)[WeightViewModel::class.java]
        weightViewModel.getWeights().observeForever { weights: List<WeightDto>? ->
            if (weights != null) {
                this.weights = weights as ArrayList<WeightDto>
                setupLineChart()
            }
        }
        weightViewModel.loadWeights()

        lineChart = findViewById(R.id.line_chart)

        yearWeek = YearWeek.now()
        yearMonth = YearMonth.now()

        val lineChartNavigateBar = findViewById<LinearLayout>(R.id.line_chart_navigate_bar)
        val lineChartDescription = findViewById<TextView>(R.id.line_chart_description)
        lineChartDescription.text = yearWeek.dayRange

        val lineChartWeek = findViewById<Button>(R.id.button_chart_week)
        lineChartWeek.setOnClickListener {
            if (displayMode != DisplayMode.WEEK) {
                yearWeek = YearWeek.now()
                displayMode = DisplayMode.WEEK
            }
            lineChartDescription.text = yearWeek.dayRange
            lineChartNavigateBar.visibility = View.VISIBLE
            setupLineChart()
        }

        val lineChartMonth = findViewById<Button>(R.id.button_chart_month)
        lineChartMonth.setOnClickListener {
            if (displayMode != DisplayMode.MONTH) {
                yearMonth = YearMonth.now()
                displayMode = DisplayMode.MONTH
            }
            lineChartDescription.text = yearMonth.format(DateTimeFormatter.ofPattern(yearMonthPattern))
            lineChartNavigateBar.visibility = View.VISIBLE
            setupLineChart()
        }

        val lineChartAll = findViewById<Button>(R.id.button_chart_all)
        lineChartAll.setOnClickListener {
            displayMode = DisplayMode.NONE
            lineChartNavigateBar.visibility = View.GONE
            setupLineChartAll()
        }

        val lineChartNextImageView = findViewById<ImageView>(R.id.line_chart_navigate_next)
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

        val lineChartBeforeImageView = findViewById<ImageView>(R.id.line_chart_navigate_before)
        lineChartBeforeImageView.setOnClickListener {
            if (displayMode == DisplayMode.WEEK) {
                yearWeek.minusWeek()
                lineChartDescription.text = yearWeek.dayRange
            } else if (displayMode == DisplayMode.MONTH) {
                yearMonth = yearMonth.minusMonths(1)
                lineChartDescription.text = yearMonth.format(DateTimeFormatter.ofPattern(yearMonthPattern))
            }
            setupLineChart()
        }
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
        weights.sortWith(Comparator.reverseOrder())
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
        weights.sort()
        var hasBefore = false
        for (i in weights.indices) {
            val dto = weights[i]
            if (dto.week == yearWeek!!.week) {
                if (i > 0 && yVals.size == 0) {
                    yVals.add(Entry(-1f, weights[i - 1].weightInKgs.toFloat()))
                    hasBefore = true
                }
                yVals.add(
                    Entry(dto.dayOfWeek.toFloat(), dto.weightInKgs.toFloat())
                )
                if (weights.size > i + 1 && yVals.size == (if (hasBefore) 8 else 7)) {
                    val dto2 = weights[i + 1]
                    yVals.add(Entry(7f, dto2.weightInKgs.toFloat()))
                }
            }
        }
        weights.sortWith(Collections.reverseOrder())
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
        weights.sort()
        val yVals = ArrayList<Entry>()
        for (i in weights.indices) {
            val dto = weights[i]
            if (dto.month == yearMonth!!.month.value && dto.year == yearMonth.year) {
                if (i - 1 >= 0 && yVals.size == 0) {
                    yVals.add(Entry(0f, weights[i - 1].weightInKgs.toFloat()))
                }
                yVals.add(Entry(dto.dayInMonth.toFloat(), dto.weightInKgs.toFloat()))
                if (weights.size >= i + 1 && yVals.size == 28) {
                    yVals.add(Entry(7f, weights[i + 1].weightInKgs.toFloat()))
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
        weights.sort()
        val yVals = ArrayList<Entry>()
        val x: MutableList<Int> = ArrayList()
        val y: MutableList<Float> = ArrayList()
        var xSum = 0
        var ySum = 0f
        var crossDeviationSum = 0f
        var deviationSum = 0f
        for (i in weights.indices) {
            yVals.add(Entry(i.toFloat(), weights[i].weightInKgs.toFloat()))
            val yVal = weights[i].weightInKgs.toFloat()
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
        for (i in weights.indices) {
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
        weights.sortWith(Comparator.reverseOrder())
    }

    companion object {
        private const val yearMonthPattern = "MMMM yyyy"
    }

    init {
        initializeViews()
    }
}