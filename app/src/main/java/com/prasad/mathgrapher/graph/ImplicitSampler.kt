package com.prasad.mathgrapher.graph

import com.prasad.mathgrapher.math.AstNode
import com.prasad.mathgrapher.math.Evaluator

data class LineSegment(val x1: Float, val y1: Float, val x2: Float, val y2: Float)

object ImplicitSampler {
    fun sample(
        lhs: AstNode,
        rhs: AstNode,
        viewport: Viewport,
        screenWidth: Float,
        screenHeight: Float,
        gridResolution: Int = 150
    ): List<LineSegment> {
        val result = mutableListOf<LineSegment>()
        
        val stepX = viewport.xRange / gridResolution
        val stepY = viewport.yRange / gridResolution
        
        val values = Array(gridResolution + 1) { DoubleArray(gridResolution + 1) }
        
        for (i in 0..gridResolution) {
            val x = viewport.xMin + i * stepX
            for (j in 0..gridResolution) {
                val y = viewport.yMin + j * stepY
                val bindings = mapOf("x" to x, "y" to y)
                val lhsVal = Evaluator.evaluate(lhs, bindings)
                val rhsVal = Evaluator.evaluate(rhs, bindings)
                val fVal = lhsVal - rhsVal
                values[i][j] = if (fVal.isFinite()) fVal else Double.NaN
            }
        }
        
        fun interpolate(x1: Double, y1: Double, val1: Double, x2: Double, y2: Double, val2: Double): Pair<Float, Float> {
            val t = if (val1 == val2 || val1.isNaN() || val2.isNaN()) 0.5 else val1 / (val1 - val2)
            val x = x1 + t * (x2 - x1)
            val y = y1 + t * (y2 - y1)
            return Pair(viewport.worldToScreenX(x, screenWidth), viewport.worldToScreenY(y, screenHeight))
        }

        for (i in 0 until gridResolution) {
            for (j in 0 until gridResolution) {
                val v00 = values[i][j]
                val v10 = values[i + 1][j]
                val v11 = values[i + 1][j + 1]
                val v01 = values[i][j + 1]
                
                if (v00.isNaN() || v10.isNaN() || v11.isNaN() || v01.isNaN()) {
                    continue
                }

                var caseIndex = 0
                if (v00 > 0) caseIndex = caseIndex or 1
                if (v10 > 0) caseIndex = caseIndex or 2
                if (v11 > 0) caseIndex = caseIndex or 4
                if (v01 > 0) caseIndex = caseIndex or 8
                
                if (caseIndex == 0 || caseIndex == 15) continue
                
                val x0 = viewport.xMin + i * stepX
                val x1 = x0 + stepX
                val y0 = viewport.yMin + j * stepY
                val y1 = y0 + stepY

                val p0 = lazy { interpolate(x0, y0, v00, x1, y0, v10) }
                val p1 = lazy { interpolate(x1, y0, v10, x1, y1, v11) }
                val p2 = lazy { interpolate(x0, y1, v01, x1, y1, v11) }
                val p3 = lazy { interpolate(x0, y0, v00, x0, y1, v01) }
                
                when (caseIndex) {
                    1, 14 -> result.add(LineSegment(p0.value.first, p0.value.second, p3.value.first, p3.value.second))
                    2, 13 -> result.add(LineSegment(p0.value.first, p0.value.second, p1.value.first, p1.value.second))
                    4, 11 -> result.add(LineSegment(p1.value.first, p1.value.second, p2.value.first, p2.value.second))
                    8, 7 -> result.add(LineSegment(p3.value.first, p3.value.second, p2.value.first, p2.value.second))
                    3, 12 -> result.add(LineSegment(p3.value.first, p3.value.second, p1.value.first, p1.value.second))
                    6, 9 -> result.add(LineSegment(p0.value.first, p0.value.second, p2.value.first, p2.value.second))
                    5 -> {
                        val centerX = (x0 + x1) / 2
                        val centerY = (y0 + y1) / 2
                        val bindings = mapOf("x" to centerX, "y" to centerY)
                        val centerVal = Evaluator.evaluate(lhs, bindings) - Evaluator.evaluate(rhs, bindings)
                        
                        if (centerVal > 0) {
                            result.add(LineSegment(p0.value.first, p0.value.second, p1.value.first, p1.value.second))
                            result.add(LineSegment(p2.value.first, p2.value.second, p3.value.first, p3.value.second))
                        } else {
                            result.add(LineSegment(p0.value.first, p0.value.second, p3.value.first, p3.value.second))
                            result.add(LineSegment(p1.value.first, p1.value.second, p2.value.first, p2.value.second))
                        }
                    }
                    10 -> {
                        val centerX = (x0 + x1) / 2
                        val centerY = (y0 + y1) / 2
                        val bindings = mapOf("x" to centerX, "y" to centerY)
                        val centerVal = Evaluator.evaluate(lhs, bindings) - Evaluator.evaluate(rhs, bindings)
                        
                        if (centerVal > 0) {
                            result.add(LineSegment(p0.value.first, p0.value.second, p3.value.first, p3.value.second))
                            result.add(LineSegment(p1.value.first, p1.value.second, p2.value.first, p2.value.second))
                        } else {
                            result.add(LineSegment(p0.value.first, p0.value.second, p1.value.first, p1.value.second))
                            result.add(LineSegment(p2.value.first, p2.value.second, p3.value.first, p3.value.second))
                        }
                    }
                }
            }
        }
        
        return result
    }
}
