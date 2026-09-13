package com.prasad.mathgrapher.math

sealed class MathException(override val message: String) : Exception(message) {
    class UnexpectedToken(val token: String, val position: Int) : 
        MathException("Unexpected token '$token' at position $position")
        
    class UnexpectedEndOfInput(val position: Int) : 
        MathException("Unexpected end of input at position $position")
        
    class UnknownFunction(val name: String, val position: Int) : 
        MathException("Unknown function '$name' at position $position")
        
    class EvaluationError(message: String) : 
        MathException(message)
}
