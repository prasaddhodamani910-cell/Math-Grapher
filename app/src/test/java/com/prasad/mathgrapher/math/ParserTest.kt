package com.prasad.mathgrapher.math

import org.junit.Test
import org.junit.Assert.*

class ParserTest {

    private fun parse(input: String): AstNode {
        return Parser(Tokenizer(input).tokenize()).parse()
    }

    @Test
    fun testSimpleExpressions() {
        val node = parse("2 + 3 * 4")
        assertTrue(node is AstNode.BinOp)
        node as AstNode.BinOp
        assertEquals('+', node.op)
        assertTrue(node.left is AstNode.Num)
        assertTrue(node.right is AstNode.BinOp)
        val right = node.right as AstNode.BinOp
        assertEquals('*', right.op)
    }
    
    @Test
    fun testRightAssociativity() {
        val node = parse("2^3^4")
        assertTrue(node is AstNode.BinOp)
        node as AstNode.BinOp
        assertEquals('^', node.op)
        assertTrue(node.left is AstNode.Num)
        assertTrue(node.right is AstNode.BinOp)
        assertEquals('^', (node.right as AstNode.BinOp).op)
    }

    @Test
    fun testImplicitMultiplication() {
        val node = parse("2x")
        assertTrue(node is AstNode.BinOp)
        node as AstNode.BinOp
        assertEquals('*', node.op)
        assertTrue(node.left is AstNode.Num)
        assertTrue(node.right is AstNode.Var)
    }

    @Test
    fun testFunctionCall() {
        val node = parse("sin(x)")
        assertTrue(node is AstNode.FuncCall)
        node as AstNode.FuncCall
        assertEquals("sin", node.name)
        assertTrue(node.arg is AstNode.Var)
    }
    
    @Test
    fun testErrors() {
        try {
            parse("2 +")
            fail("Expected exception")
        } catch (e: MathException.UnexpectedEndOfInput) {
            // Success
        }
        
        try {
            parse("2 + )")
            fail("Expected exception")
        } catch (e: MathException.UnexpectedToken) {
            // Success
        }
    }
}
