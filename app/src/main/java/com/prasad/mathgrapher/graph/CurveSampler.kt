package com.prasad.mathgrapher.graph

import com.prasad.mathgrapher.math.AstNode
import com.prasad.mathgrapher.math.Evaluator
import kotlin.math.abs

data class SamplePoint(val x: Float, val y: Float, val isValid: Boolean)

object CurveSampler {
    fun sample(
        ast: AstNode,
        viewport: Viewport,
        screenWidth: Float,
        screenHeight: Float,
        samplesPerPixel: Float = 1.5f
    ): List<SamplePoint> {
        val numSamples = (screenWidth * samplesPerPixel).toInt().coerceIn(200, 2000)
        val result = mutableListOf<SamplePoint>()
        val step = viewport.xRange / numSamples
        
        val maxYScreen = screenHeight * 2
        val minYScreen = -screenHeight

        var prevX = viewport.xMin
        var prevYWorld = Evaluator.evaluate(ast, prevX)
        
        for (i in 0..numSamples) {
            val worldX = viewport.xMin + i * step
            val worldY = Evaluator.evaluate(ast, worldX)
            
            // Adaptive refinement
            if (i > 0 && worldY.isFinite() && prevYWorld.isFinite()) {
                val slope = abs((worldY - prevYWorld) / step)
                if (slope > 10.0) { // steep slope threshold
                    val midX = prevX + step / 2
                    val midYWorld = Evaluator.evaluate(ast, midX)
                    val midScreenX = viewport.worldToScreenX(midX, screenWidth)
                    val midScreenY = viewport.worldToScreenY(midYWorld, screenHeight)
                    val midIsValid = midYWorld.isFinite() && midScreenY in minYScreen..maxYScreen
                    result.add(SamplePoint(midScreenX, midScreenY, midIsValid))
                }
            }

            val screenX = viewport.worldToScreenX(worldX, screenWidth)
            val screenY = viewport.worldToScreenY(worldY, screenHeight)
            val isValid = worldY.isFinite() && screenY in minYScreen..maxYScreen
            
            result.add(SamplePoint(screenX, screenY, isValid))
            
            prevX = worldX
            prevYWorld = worldY
        }
        
        return result
    }
}
