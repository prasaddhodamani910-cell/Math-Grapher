package com.prasad.mathgrapher.math

enum class CompareOp { LESS, GREATER, LESS_EQUAL, GREATER_EQUAL }

sealed class EquationType {
    data class Explicit(val expr: AstNode) : EquationType()
    data class Implicit(val lhs: AstNode, val rhs: AstNode) : EquationType()
    data class Parametric(val xExpr: AstNode, val yExpr: AstNode, val tMin: Double = 0.0, val tMax: Double = 2 * Math.PI) : EquationType()
    data class Polar(val rExpr: AstNode, val thetaMin: Double = 0.0, val thetaMax: Double = 2 * Math.PI) : EquationType()
    data class Inequality(val lhs: AstNode, val rhs: AstNode, val op: CompareOp) : EquationType()
}
