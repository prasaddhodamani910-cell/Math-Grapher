package com.prasad.mathgrapher.math

import org.junit.Test
import org.junit.Assert.*

class EvaluatorTest {

    @Test
    fun testNumericEvaluation() {
        assertEquals(5.0, Evaluator.evaluate("2 + 3", 0.0).getOrThrow(), 0.0001)
        assertEquals(14.0, Evaluator.evaluate("2 + 3 * 4", 0.0).getOrThrow(), 0.0001)
    }

    @Test
    fun testVariableEvaluation() {
        assertEquals(9.0, Evaluator.evaluate("x^2", 3.0).getOrThrow(), 0.0001)
    }

    @Test
    fun testFunctionEvaluation() {
        assertEquals(0.0, Evaluator.evaluate("sin(0)", 0.0).getOrThrow(), 0.0001)
        assertEquals(1.0, Evaluator.evaluate("cos(0)", 0.0).getOrThrow(), 0.0001)
        assertEquals(2.0, Evaluator.evaluate("sqrt(4)", 0.0).getOrThrow(), 0.0001)
    }

    @Test
    fun testConstants() {
        assertEquals(Math.PI, Evaluator.evaluate("pi", 0.0).getOrThrow(), 0.0001)
        assertEquals(Math.E, Evaluator.evaluate("e", 0.0).getOrThrow(), 0.0001)
    }

    @Test
    fun testDivisionByZero() {
        val result = Evaluator.evaluate("1 / 0", 0.0).getOrThrow()
        assertTrue(result.isInfinite())
    }
}
