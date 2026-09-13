package com.prasad.mathgrapher.math

sealed class AstNode {
    data class Num(val value: Double) : AstNode()
    data class Var(val name: String) : AstNode()
    data class BinOp(val op: Char, val left: AstNode, val right: AstNode) : AstNode()
    data class UnaryOp(val op: Char, val operand: AstNode) : AstNode()
    data class FuncCall(val name: String, val arg: AstNode) : AstNode()
}
