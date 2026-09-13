package com.prasad.mathgrapher.math

import org.junit.Test
import org.junit.Assert.*

class TokenizerTest {

    @Test
    fun testNumbers() {
        val tokenizer = Tokenizer("3.14 5 5. .5 1e3 2.5e-4")
        val tokens = tokenizer.tokenize()
        val values = tokens.filter { it.type == TokenType.NUMBER }.map { it.value }
        assertEquals(listOf("3.14", "5", "5.", ".5", "1e3", "2.5e-4"), values)
    }

    @Test
    fun testImplicitMultiplication() {
        // 2x -> 2 * x
        val t1 = Tokenizer("2x").tokenize()
        assertEquals(listOf(TokenType.NUMBER, TokenType.STAR, TokenType.IDENTIFIER, TokenType.EOF), t1.map { it.type })
        
        // 2(x+1) -> 2 * (x+1)
        val t2 = Tokenizer("2(x+1)").tokenize()
        assertEquals(listOf(TokenType.NUMBER, TokenType.STAR, TokenType.LPAREN, TokenType.IDENTIFIER, TokenType.PLUS, TokenType.NUMBER, TokenType.RPAREN, TokenType.EOF), t2.map { it.type })
        
        // )x -> ) * x
        val t3 = Tokenizer("(2)x").tokenize()
        assertEquals(listOf(TokenType.LPAREN, TokenType.NUMBER, TokenType.RPAREN, TokenType.STAR, TokenType.IDENTIFIER, TokenType.EOF), t3.map { it.type })
        
        // )( -> ) * (
        val t4 = Tokenizer("(1)(2)").tokenize()
        assertEquals(listOf(TokenType.LPAREN, TokenType.NUMBER, TokenType.RPAREN, TokenType.STAR, TokenType.LPAREN, TokenType.NUMBER, TokenType.RPAREN, TokenType.EOF), t4.map { it.type })
        
        // x(2) -> x * (2)
        val t5 = Tokenizer("x(2)").tokenize()
        assertEquals(listOf(TokenType.IDENTIFIER, TokenType.STAR, TokenType.LPAREN, TokenType.NUMBER, TokenType.RPAREN, TokenType.EOF), t5.map { it.type })
        
        // sin(x) -> sin ( x ) -> NO STAR
        val t6 = Tokenizer("sin(x)").tokenize()
        assertEquals(listOf(TokenType.IDENTIFIER, TokenType.LPAREN, TokenType.IDENTIFIER, TokenType.RPAREN, TokenType.EOF), t6.map { it.type })
        
        // 2sin(x) -> 2 * sin ( x )
        val t7 = Tokenizer("2sin(x)").tokenize()
        assertEquals(listOf(TokenType.NUMBER, TokenType.STAR, TokenType.IDENTIFIER, TokenType.LPAREN, TokenType.IDENTIFIER, TokenType.RPAREN, TokenType.EOF), t7.map { it.type })
    }
    
    @Test
    fun testEdgeCases() {
        assertEquals(listOf(TokenType.EOF), Tokenizer("   ").tokenize().map { it.type })
        assertEquals(listOf(TokenType.NUMBER, TokenType.EOF), Tokenizer("42").tokenize().map { it.type })
    }
}
