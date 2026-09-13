package com.prasad.mathgrapher.graph

import kotlin.math.max
import kotlin.math.min

data class Viewport(
    val xMin: Double = -10.0,
    val xMax: Double = 10.0,
    val yMin: Double = -10.0,
    val yMax: Double = 10.0
) {
    val xRange: Double get() = xMax - xMin
    val yRange: Double get() = yMax - yMin
    val centerX: Double get() = (xMin + xMax) / 2.0
    val centerY: Double get() = (yMin + yMax) / 2.0

    fun worldToScreenX(worldX: Double, screenWidth: Float): Float {
        return ((worldX - xMin) / xRange * screenWidth).toFloat()
    }

    fun worldToScreenY(worldY: Double, screenHeight: Float): Float {
        // Invert Y axis
        return ((yMax - worldY) / yRange * screenHeight).toFloat()
    }

    fun screenToWorldX(screenX: Float, screenWidth: Float): Double {
        return xMin + (screenX / screenWidth) * xRange
    }

    fun screenToWorldY(screenY: Float, screenHeight: Float): Double {
        return yMax - (screenY / screenHeight) * yRange
    }

    fun pan(dx: Double, dy: Double): Viewport {
        return this.copy(
            xMin = xMin + dx,
            xMax = xMax + dx,
            yMin = yMin + dy,
            yMax = yMax + dy
        )
    }

    fun zoom(factor: Double, focusX: Double, focusY: Double): Viewport {
        // factor > 1 means zoom out, < 1 means zoom in
        val newXRange = (xRange * factor).coerceIn(0.001, 1e8)
        val newYRange = (yRange * factor).coerceIn(0.001, 1e8)
        
        // Ensure ratio is kept around focus
        val rx = if (xRange > 0) (focusX - xMin) / xRange else 0.5
        val ry = if (yRange > 0) (focusY - yMin) / yRange else 0.5

        val newXMin = focusX - rx * newXRange
        val newXMax = newXMin + newXRange
        
        val newYMin = focusY - ry * newYRange
        val newYMax = newYMin + newYRange

        return this.copy(
            xMin = newXMin,
            xMax = newXMax,
            yMin = newYMin,
            yMax = newYMax
        )
    }

    companion object {
        val DEFAULT = Viewport()
    }
}
