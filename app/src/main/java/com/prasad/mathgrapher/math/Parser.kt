package com.prasad.mathgrapher.math

class Parser(private val tokens: List<Token>) {
    private var pos = 0
    
    fun parse(): AstNode {
        if (tokens.isEmpty() || tokens[0].type == TokenType.EOF) {
            throw MathException.UnexpectedEndOfInput(0)
        }
        val result = parseExpression()
        if (!isAtEnd()) {
            throw MathException.UnexpectedToken(peek().value, peek().position)
        }
        return result
    }
    
    private fun parseExpression(): AstNode {
        var node = parseTerm()
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            val op = previous().type
            val opChar = if (op == TokenType.PLUS) '+' else '-'
            val right = parseTerm()
            node = AstNode.BinOp(opChar, node, right)
        }
        return node
    }
    
    private fun parseTerm(): AstNode {
        var node = parseFactor()
        while (match(TokenType.STAR, TokenType.SLASH, TokenType.PERCENT)) {
            val op = previous().type
            val opChar = when (op) {
                TokenType.STAR -> '*'
                TokenType.SLASH -> '/'
                TokenType.PERCENT -> '%'
                else -> throw IllegalStateException()
            }
            val right = parseFactor()
            node = AstNode.BinOp(opChar, node, right)
        }
        return node
    }
    
    private fun parseFactor(): AstNode {
        var node = parseUnary()
        if (match(TokenType.CARET)) {
            val right = parseFactor() // Right associative because parseFactor() evaluates the whole power chain
            node = AstNode.BinOp('^', node, right)
        }
        return node
    }
    
    private fun parseUnary(): AstNode {
        if (match(TokenType.PLUS, TokenType.MINUS)) {
            val op = previous().type
            val opChar = if (op == TokenType.PLUS) '+' else '-'
            val right = parsePrimary()
            return AstNode.UnaryOp(opChar, right)
        }
        return parsePrimary()
    }
    
    private fun parsePrimary(): AstNode {
        if (match(TokenType.NUMBER)) {
            return AstNode.Num(previous().value.toDouble())
        }
        if (match(TokenType.IDENTIFIER)) {
            val name = previous().value
            if (name == "pi") return AstNode.Num(Math.PI)
            if (name == "e") return AstNode.Num(Math.E)
            
            if (Tokenizer.KNOWN_FUNCTIONS.contains(name)) {
                if (match(TokenType.LPAREN)) {
                    val arg = parseExpression()
                    consume(TokenType.RPAREN, "Expected ')' after argument.")
                    return AstNode.FuncCall(name, arg)
                } else {
                    throw MathException.UnexpectedToken(peek().value, peek().position)
                }
            }
            
            return AstNode.Var(name)
        }
        if (match(TokenType.LPAREN)) {
            val expr = parseExpression()
            consume(TokenType.RPAREN, "Expected ')' after expression.")
            return expr
        }
        
        if (isAtEnd()) {
            throw MathException.UnexpectedEndOfInput(previous().position)
        }
        val token = peek()
        throw MathException.UnexpectedToken(token.value, token.position)
    }
    
    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }
    
    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        val token = peek()
        if (token.type == TokenType.EOF) {
            throw MathException.UnexpectedEndOfInput(token.position)
        }
        throw MathException.UnexpectedToken(token.value, token.position)
    }
    
    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }
    
    private fun advance(): Token {
        if (!isAtEnd()) pos++
        return previous()
    }
    
    private fun isAtEnd(): Boolean {
        return peek().type == TokenType.EOF
    }
    
    private fun peek(): Token {
        return tokens[pos]
    }
    
    private fun previous(): Token {
        return tokens[pos - 1]
    }
}
