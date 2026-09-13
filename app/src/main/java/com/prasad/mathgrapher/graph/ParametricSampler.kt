package com.prasad.mathgrapher.graph

import com.prasad.mathgrapher.math.AstNode
import com.prasad.mathgrapher.math.Evaluator

object ParametricSampler {
    fun sample(
        xExpr: AstNode,
        yExpr: AstNode,
        tMin: Double,
        tMax: Double,
        viewport: Viewport,
        screenWidth: Float,
        screenHeight: Float,
        numSamples: Int = 500
    ): List<SamplePoint> {
        val result = mutableListOf<SamplePoint>()
        val step = if (numSamples > 0) (tMax - tMin) / numSamples else 0.0

        for (i in 0..numSamples) {
            val t = tMin + i * step
            val bindings = mapOf("t" to t)
            val worldX = Evaluator.evaluate(xExpr, bindings)
            val worldY = Evaluator.evaluate(yExpr, bindings)

            val screenX = viewport.worldToScreenX(worldX, screenWidth)
            val screenY = viewport.worldToScreenY(worldY, screenHeight)

            val isValid = worldX.isFinite() && worldY.isFinite()
                          
            result.add(SamplePoint(screenX, screenY, isValid))
        }

        return result
    }
}
