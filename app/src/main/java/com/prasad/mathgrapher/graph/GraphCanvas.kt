package com.prasad.mathgrapher.graph

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import java.util.Locale
import com.prasad.mathgrapher.ui.theme.LocalMathGrapherColors

@Composable
fun GraphCanvas(
    equations: List<Equation>,
    viewport: Viewport,
    curveColors: List<Color>,
    inspectedPoint: Pair<Double, Double>?,
    onPan: (dx: Float, dy: Float, screenWidth: Float, screenHeight: Float) -> Unit,
    onZoom: (factor: Float, focusX: Float, focusY: Float, screenWidth: Float, screenHeight: Float) -> Unit,
    onTap: (x: Float, y: Float, screenWidth: Float, screenHeight: Float) -> Unit,
    mathColors: com.prasad.mathgrapher.ui.theme.MathGrapherColors,
    modifier: Modifier = Modifier
) {
    // Track actual canvas size
    var canvasWidth by remember { mutableFloatStateOf(1080f) }
    var canvasHeight by remember { mutableFloatStateOf(1920f) }

    Canvas(
        modifier = modifier
            .clipToBounds()
            .onSizeChanged { size ->
                canvasWidth = size.width.toFloat()
                canvasHeight = size.height.toFloat()
            }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    if (zoom != 1f) {
                        onZoom(1f / zoom, centroid.x, centroid.y, canvasWidth, canvasHeight)
                    }
                    if (pan != Offset.Zero) {
                        onPan(pan.x, pan.y, canvasWidth, canvasHeight)
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onTap(offset.x, offset.y, canvasWidth, canvasHeight)
                }
            }
    ) {
        val screenWidth = size.width
        val screenHeight = size.height

        // --- Grid & Axes ---
        val majorGridColor = mathColors.gridLine
        val minorGridColor = mathColors.gridLine.copy(alpha = 0.5f)
        val axisColor = mathColors.axisLine
        val textColor = mathColors.onSurfaceMuted

        val xStep = calculateNiceStep(viewport.xRange)
        val yStep = calculateNiceStep(viewport.yRange)
        val minorXStep = xStep / 5
        val minorYStep = yStep / 5

        val firstXLine = floor(viewport.xMin / minorXStep) * minorXStep
        val firstYLine = floor(viewport.yMin / minorYStep) * minorYStep

        val textPaint = Paint().apply {
            color = android.graphics.Color.argb(
                (textColor.alpha * 255).toInt(),
                (textColor.red * 255).toInt(),
                (textColor.green * 255).toInt(),
                (textColor.blue * 255).toInt()
            )
            textSize = 24f
            isAntiAlias = true
            typeface = Typeface.DEFAULT
        }

        fun isNearMultiple(value: Double, step: Double, threshold: Double): Boolean {
            val rem = kotlin.math.abs(value % step)
            return rem < threshold || kotlin.math.abs(rem - step) < threshold
        }

        drawIntoCanvas { canvas ->
            // Vertical grid lines + X labels
            var xVal = firstXLine
            while (xVal <= viewport.xMax) {
                val sx = viewport.worldToScreenX(xVal, screenWidth)
                val isAxis = abs(xVal) < minorXStep * 0.01
                val isMajor = isAxis || isNearMultiple(xVal, xStep, minorXStep * 0.1)

                drawLine(
                    color = if (isAxis) axisColor else if (isMajor) majorGridColor else minorGridColor,
                    start = Offset(sx, 0f),
                    end = Offset(sx, screenHeight),
                    strokeWidth = if (isAxis) 3f else if (isMajor) 2f else 1f
                )

                if (isMajor && !isAxis) {
                    val labelY = viewport.worldToScreenY(0.0, screenHeight)
                        .coerceIn(40f, screenHeight - 10f)
                    canvas.nativeCanvas.drawText(
                        formatLabel(xVal), sx + 6f, labelY - 6f, textPaint
                    )
                }
                xVal += minorXStep
            }

            // Horizontal grid lines + Y labels
            var yVal = firstYLine
            while (yVal <= viewport.yMax) {
                val sy = viewport.worldToScreenY(yVal, screenHeight)
                val isAxis = abs(yVal) < minorYStep * 0.01
                val isMajor = isAxis || isNearMultiple(yVal, yStep, minorYStep * 0.1)

                drawLine(
                    color = if (isAxis) axisColor else if (isMajor) majorGridColor else minorGridColor,
                    start = Offset(0f, sy),
                    end = Offset(screenWidth, sy),
                    strokeWidth = if (isAxis) 3f else if (isMajor) 2f else 1f
                )

                if (isMajor && !isAxis) {
                    val labelX = viewport.worldToScreenX(0.0, screenWidth)
                        .coerceIn(10f, screenWidth - 60f)
                    canvas.nativeCanvas.drawText(
                        formatLabel(yVal), labelX + 6f, sy - 6f, textPaint
                    )
                }
                yVal += minorYStep
            }
        }

        // --- Draw Curves ---
        for (equation in equations) {
            val ast = equation.ast ?: continue
            val color = if (curveColors.isNotEmpty()) {
                curveColors[equation.colorIndex % curveColors.size]
            } else {
                Color.Red
            }

            val points = CurveSampler.sample(ast, viewport, screenWidth, screenHeight)
            if (points.isEmpty()) continue

            drawCurve(points, color)
        }

        // --- Inspected Point ---
        inspectedPoint?.let { (worldX, worldY) ->
            val sx = viewport.worldToScreenX(worldX, screenWidth)
            val sy = viewport.worldToScreenY(worldY, screenHeight)

            drawCircle(
                color = Color(0xFF2196F3),
                radius = 6.dp.toPx(),
                center = Offset(sx, sy)
            )

            drawIntoCanvas { canvas ->
                val text = String.format(Locale.US, "(%.2f, %.2f)", worldX, worldY)
                val paint = Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 36f
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                }
                canvas.nativeCanvas.drawText(text, sx, sy - 20f, paint)
            }
        }
    }
}

private fun DrawScope.drawCurve(points: List<SamplePoint>, color: Color) {
    val path = Path()
    var pathStarted = false

    for (i in points.indices) {
        val point = points[i]
        if (point.isValid) {
            if (!pathStarted) {
                path.moveTo(point.x, point.y)
                pathStarted = true
            } else {
                // Check for discontinuity (large gap = asymptote)
                if (i > 0 && points[i - 1].isValid) {
                    val dy = abs(point.y - points[i - 1].y)
                    if (dy > size.height * 0.8f) {
                        // Likely an asymptote — break the path
                        path.moveTo(point.x, point.y)
                        continue
                    }
                }
                path.lineTo(point.x, point.y)
            }
        } else {
            pathStarted = false
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 3.dp.toPx())
    )
}

private fun calculateNiceStep(range: Double, targetSteps: Int = 8): Double {
    val rawStep = range / targetSteps
    val magnitude = 10.0.pow(floor(log10(rawStep)))
    val normStep = rawStep / magnitude

    val niceNorm = when {
        normStep < 1.5 -> 1.0
        normStep < 3.0 -> 2.0
        normStep < 7.0 -> 5.0
        else -> 10.0
    }

    return niceNorm * magnitude
}

private fun formatLabel(value: Double): String {
    if (abs(value) < 1e-10) return "0"
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
}
