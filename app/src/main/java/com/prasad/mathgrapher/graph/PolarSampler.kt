package com.prasad.mathgrapher.graph

import com.prasad.mathgrapher.math.AstNode
import com.prasad.mathgrapher.math.Evaluator
import kotlin.math.cos
import kotlin.math.sin

object PolarSampler {
    fun sample(
        rExpr: AstNode,
        thetaMin: Double,
        thetaMax: Double,
        viewport: Viewport,
        screenWidth: Float,
        screenHeight: Float,
        numSamples: Int = 800
    ): List<SamplePoint> {
        val result = mutableListOf<SamplePoint>()
        val step = if (numSamples > 0) (thetaMax - thetaMin) / numSamples else 0.0

        for (i in 0..numSamples) {
            val theta = thetaMin + i * step
            val bindings = mapOf("theta" to theta, "t" to theta)
            val r = Evaluator.evaluate(rExpr, bindings)
            
            val worldX = r * cos(theta)
            val worldY = r * sin(theta)

            val screenX = viewport.worldToScreenX(worldX, screenWidth)
            val screenY = viewport.worldToScreenY(worldY, screenHeight)

            val isValid = r.isFinite()
                          
            result.add(SamplePoint(screenX, screenY, isValid))
        }

        return result
    }
}
