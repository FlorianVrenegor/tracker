package com.example.tracker.weight

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.tracker.R
import kotlin.math.abs

class LineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val lineColor = ContextCompat.getColor(context, R.color.colorPrimary)

    private val linePaint = Paint().apply {
        color = lineColor
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val pointPaint = Paint().apply {
        color = lineColor
        strokeWidth = 10f
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val gridPaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    // Variables for preallocated objects
    private var dataPoints: List<Float> = emptyList()
    private val path = Path()  // Reuse Path object for the gradient and line
    private val curvePath = Path()  // Reuse Path object for the line curve

    // Background gradient paint
    private val gradientPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // Gradient shader will be preallocated and updated only if needed
    private var gradient: LinearGradient? = null

    // Method to set data points
    fun setDataPoints(dataPoints: List<Float>) {
        this.dataPoints = dataPoints
        invalidate() // Redraw the view when data changes
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Define the LinearGradient shader for the background
        gradient = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            Color.parseColor("#88C6F5FF"), // Light color at the top
            ContextCompat.getColor(context, R.color.light_gray_transparent), // Darker color at the bottom
            Shader.TileMode.CLAMP
        )
        gradientPaint.shader = gradient
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // Ensure canvas and dataPoints are not empty
        if (canvas == null || dataPoints.isEmpty()) return

        val width = width.toFloat()
        val height = height.toFloat()
        val padding = 50f

        // Draw grid lines
        drawGrid(canvas, width, height, padding)

        // Calculate scaling factors for data points
        val maxDataPoint = dataPoints.maxOrNull() ?: 0f
        val xInterval = (width - 2 * padding) / (dataPoints.size - 1)
        val yScale = (height - 2 * padding) / maxDataPoint

        // Path for smooth curve
        path.reset()
        val startX = padding
        val startY = height - padding - dataPoints[0] * yScale
        path.moveTo(startX, startY)

        // Draw Bézier curves between points
        for (i in 0 until dataPoints.size - 1) {
            val x1 = padding + i * xInterval
            val y1 = height - padding - dataPoints[i] * yScale
            val x2 = padding + (i + 1) * xInterval
            val y2 = height - padding - dataPoints[i + 1] * yScale

            // Calculate control points for Bézier curve
            val midX = (x1 + x2) / 2
            val controlY1 = y1 + abs(y2 - y1) * 0.25f
            val controlY2 = y2 - abs(y2 - y1) * 0.25f

            // Add cubic Bézier curve to path
            path.cubicTo(midX, controlY1, midX, controlY2, x2, y2)
        }

        // Close the path to create a shape under the curve (to the bottom of the chart)
        path.lineTo(width - padding, height - padding)
        path.lineTo(padding, height - padding)
        path.close()

        // Draw the gradient under the curve
        canvas.drawPath(path, gradientPaint)

        // Now draw the curve line on top
        curvePath.reset()
        curvePath.moveTo(startX, startY)
        for (i in 0 until dataPoints.size - 1) {
            val x1 = padding + i * xInterval
            val y1 = height - padding - dataPoints[i] * yScale
            val x2 = padding + (i + 1) * xInterval
            val y2 = height - padding - dataPoints[i + 1] * yScale

            val midX = (x1 + x2) / 2
            val controlY1 = y1 + abs(y2 - y1) * 0.25f
            val controlY2 = y2 - abs(y2 - y1) * 0.25f

            curvePath.cubicTo(midX, controlY1, midX, controlY2, x2, y2)
        }
        canvas.drawPath(curvePath, linePaint)

        // Draw data points
        for (i in dataPoints.indices) {
            val x = padding + i * xInterval
            val y = height - padding - dataPoints[i] * yScale

            canvas.drawCircle(x, y, 8f, pointPaint)
        }
    }

    // Draw grid on the canvas
    private fun drawGrid(canvas: Canvas, width: Float, height: Float, padding: Float) {
        val numGridLines = 5
        val gridIntervalY = (height - 2 * padding) / numGridLines

        for (i in 0..numGridLines) {
            val y = padding + i * gridIntervalY
            canvas.drawLine(padding, y, width - padding, y, gridPaint)
        }
    }
}