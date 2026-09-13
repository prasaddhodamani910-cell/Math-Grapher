package com.prasad.mathgrapher.math

import kotlin.math.*

object Evaluator {
    fun evaluate(node: AstNode, x: Double): Double {
        return evaluate(node, mapOf("x" to x))
    }
    
    var isDegreesMode = false
    
    private fun isOddIntegerReciprocal(exp: Double): Boolean {
        val rec = 1.0 / exp
        return abs(rec - round(rec)) < 1e-9 && (round(rec).toLong() % 2L != 0L)
    }

    fun evaluate(node: AstNode, bindings: Map<String, Double>): Double {
        return when (node) {
            is AstNode.Num -> node.value
            is AstNode.Var -> {
                bindings[node.name] ?: 1.0 // Bug 2/3 fix: default unknown parameters to 1.0
            }
            is AstNode.BinOp -> {
                val leftVal = evaluate(node.left, bindings)
                val rightVal = evaluate(node.right, bindings)
                when (node.op) {
                    '+' -> leftVal + rightVal
                    '-' -> leftVal - rightVal
                    '*' -> leftVal * rightVal
                    '/' -> leftVal / rightVal
                    '^' -> {
                        if (leftVal < 0 && isOddIntegerReciprocal(rightVal)) {
                            -abs(leftVal).pow(rightVal)
                        } else {
                            leftVal.pow(rightVal)
                        }
                    }
                    '%' -> leftVal % rightVal
                    else -> throw MathException.EvaluationError("Unknown operator: ${node.op}")
                }
            }
            is AstNode.UnaryOp -> {
                val value = evaluate(node.operand, bindings)
                when (node.op) {
                    '-' -> -value
                    '+' -> value
                    else -> throw MathException.EvaluationError("Unknown unary operator: ${node.op}")
                }
            }
            is AstNode.FuncCall -> {
                val argVal = evaluate(node.arg, bindings)
                val trigArg = if (isDegreesMode && node.name in listOf("sin", "cos", "tan")) {
                    argVal * PI / 180.0
                } else {
                    argVal
                }
                when (node.name) {
                    "sin" -> sin(trigArg)
                    "cos" -> cos(trigArg)
                    "tan" -> tan(trigArg)
                    "asin" -> {
                        val res = asin(argVal)
                        if (isDegreesMode) res * 180.0 / PI else res
                    }
                    "acos" -> {
                        val res = acos(argVal)
                        if (isDegreesMode) res * 180.0 / PI else res
                    }
                    "atan" -> {
                        val res = atan(argVal)
                        if (isDegreesMode) res * 180.0 / PI else res
                    }
                    "log" -> log10(argVal)
                    "ln" -> ln(argVal)
                    "sqrt" -> sqrt(argVal)
                    "abs" -> abs(argVal)
                    "exp" -> exp(argVal)
                    "ceil" -> ceil(argVal)
                    "floor" -> floor(argVal)
                    else -> throw MathException.EvaluationError("Unknown function: ${node.name}")
                }
            }
        }
    }
    
    fun evaluate(input: String, x: Double): Result<Double> {
        return try {
            val tokenizer = Tokenizer(input)
            val tokens = tokenizer.tokenize()
            val parser = Parser(tokens)
            val ast = parser.parse()
            Result.success(evaluate(ast, x))
        } catch (e: MathException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(MathException.EvaluationError(e.message ?: "Unknown error"))
        }
    }
}
