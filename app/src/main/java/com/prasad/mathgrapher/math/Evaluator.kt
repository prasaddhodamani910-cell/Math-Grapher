package com.prasad.mathgrapher.math

import kotlin.math.*

object Evaluator {
    fun evaluate(node: AstNode, x: Double): Double {
        return when (node) {
            is AstNode.Num -> node.value
            is AstNode.Var -> {
                if (node.name == "x") x
                else throw MathException.EvaluationError("Unknown variable: ${node.name}")
            }
            is AstNode.BinOp -> {
                val leftVal = evaluate(node.left, x)
                val rightVal = evaluate(node.right, x)
                when (node.op) {
                    '+' -> leftVal + rightVal
                    '-' -> leftVal - rightVal
                    '*' -> leftVal * rightVal
                    '/' -> leftVal / rightVal
                    '^' -> leftVal.pow(rightVal)
                    '%' -> leftVal % rightVal
                    else -> throw MathException.EvaluationError("Unknown operator: ${node.op}")
                }
            }
            is AstNode.UnaryOp -> {
                val value = evaluate(node.operand, x)
                when (node.op) {
                    '-' -> -value
                    '+' -> value
                    else -> throw MathException.EvaluationError("Unknown unary operator: ${node.op}")
                }
            }
            is AstNode.FuncCall -> {
                val argVal = evaluate(node.arg, x)
                when (node.name) {
                    "sin" -> sin(argVal)
                    "cos" -> cos(argVal)
                    "tan" -> tan(argVal)
                    "asin" -> asin(argVal)
                    "acos" -> acos(argVal)
                    "atan" -> atan(argVal)
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
