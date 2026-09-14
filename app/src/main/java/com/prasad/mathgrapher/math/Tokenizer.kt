package com.prasad.mathgrapher.math

enum class TokenType {
    NUMBER, IDENTIFIER, PLUS, MINUS, STAR, SLASH, CARET, PERCENT, LPAREN, RPAREN, COMMA, EQUALS, LESS, GREATER, LESS_EQUAL, GREATER_EQUAL, EOF
}

data class Token(val type: TokenType, val value: String, val position: Int)

class Tokenizer(private val input: String) {
    private var pos = 0
    
    companion object {
        val KNOWN_FUNCTIONS = setOf("sin", "cos", "tan", "asin", "acos", "atan", "log", "ln", "sqrt", "abs", "exp", "ceil", "floor")
    }

    fun tokenize(): List<Token> {
        val tokens = mutableListOf<Token>()
        
        while (pos < input.length) {
            val char = input[pos]
            
            when {
                char.isWhitespace() -> pos++
                char == '+' -> tokens.add(Token(TokenType.PLUS, "+", pos++))
                char == '-' -> tokens.add(Token(TokenType.MINUS, "-", pos++))
                char == '*' -> tokens.add(Token(TokenType.STAR, "*", pos++))
                char == '/' -> tokens.add(Token(TokenType.SLASH, "/", pos++))
                char == '^' -> tokens.add(Token(TokenType.CARET, "^", pos++))
                char == '%' -> tokens.add(Token(TokenType.PERCENT, "%", pos++))
                char == '(' -> tokens.add(Token(TokenType.LPAREN, "(", pos++))
                char == ')' -> tokens.add(Token(TokenType.RPAREN, ")", pos++))
                char == ',' -> tokens.add(Token(TokenType.COMMA, ",", pos++))
                char == '=' -> tokens.add(Token(TokenType.EQUALS, "=", pos++))
                char == '<' -> {
                    if (pos + 1 < input.length && input[pos + 1] == '=') {
                        tokens.add(Token(TokenType.LESS_EQUAL, "<=", pos))
                        pos += 2
                    } else {
                        tokens.add(Token(TokenType.LESS, "<", pos++))
                    }
                }
                char == '>' -> {
                    if (pos + 1 < input.length && input[pos + 1] == '=') {
                        tokens.add(Token(TokenType.GREATER_EQUAL, ">=", pos))
                        pos += 2
                    } else {
                        tokens.add(Token(TokenType.GREATER, ">", pos++))
                    }
                }
                char.isDigit() || char == '.' -> tokens.add(readNumber())
                char.isLetter() -> tokens.addAll(readIdentifierTokens())
                else -> throw MathException.UnexpectedToken(char.toString(), pos)
            }
        }
        tokens.add(Token(TokenType.EOF, "", pos))
        
        return insertImplicitMultiplication(tokens)
    }
    
    private fun readNumber(): Token {
        val startPos = pos
        var hasDot = false
        var hasE = false
        
        while (pos < input.length) {
            val c = input[pos]
            if (c.isDigit()) {
                pos++
            } else if (c == '.' && !hasDot) {
                hasDot = true
                pos++
            } else if ((c == 'e' || c == 'E') && !hasE) {
                hasE = true
                pos++
                if (pos < input.length && (input[pos] == '+' || input[pos] == '-')) {
                    pos++
                }
            } else {
                break
            }
        }
        
        val value = input.substring(startPos, pos)
        if (value == ".") throw MathException.UnexpectedToken(".", startPos)
        
        return Token(TokenType.NUMBER, value, startPos)
    }
    
    private val KNOWN_WORDS = KNOWN_FUNCTIONS + setOf("pi", "e")
    
    private fun readIdentifierTokens(): List<Token> {
        val startPos = pos
        while (pos < input.length && input[pos].isLetter()) {
            pos++
        }
        val word = input.substring(startPos, pos)
    
        if (KNOWN_WORDS.contains(word)) {
            return listOf(Token(TokenType.IDENTIFIER, word, startPos))
        }
    
        for (fn in KNOWN_FUNCTIONS.sortedByDescending { it.length }) {
            if (word.length > fn.length && word.startsWith(fn)) {
                val result = mutableListOf(Token(TokenType.IDENTIFIER, fn, startPos))
                val rest = word.substring(fn.length)
                for ((idx, ch) in rest.withIndex()) {
                    result.add(Token(TokenType.IDENTIFIER, ch.toString(), startPos + fn.length + idx))
                }
                return result
            }
        }
    
        return word.mapIndexed { idx, ch -> Token(TokenType.IDENTIFIER, ch.toString(), startPos + idx) }
    }
    
    private fun insertImplicitMultiplication(tokens: List<Token>): List<Token> {
        if (tokens.isEmpty()) return tokens
        
        val result = mutableListOf<Token>()
        var prevToken = tokens[0]
        result.add(prevToken)
        
        for (i in 1 until tokens.size) {
            val currentToken = tokens[i]
            var insertStar = false
            
            if (prevToken.type == TokenType.NUMBER && currentToken.type == TokenType.IDENTIFIER) {
                insertStar = true
            } else if (prevToken.type == TokenType.NUMBER && currentToken.type == TokenType.LPAREN) {
                insertStar = true
            } else if (prevToken.type == TokenType.RPAREN && currentToken.type == TokenType.IDENTIFIER) {
                insertStar = true
            } else if (prevToken.type == TokenType.RPAREN && currentToken.type == TokenType.LPAREN) {
                insertStar = true
            } else if (prevToken.type == TokenType.IDENTIFIER && currentToken.type == TokenType.LPAREN) {
                if (!KNOWN_FUNCTIONS.contains(prevToken.value)) {
                    insertStar = true
                }
            } else if (prevToken.type == TokenType.IDENTIFIER && currentToken.type == TokenType.IDENTIFIER) {
                if (!KNOWN_FUNCTIONS.contains(prevToken.value)) {
                    insertStar = true
                }
            }
            
            if (insertStar) {
                result.add(Token(TokenType.STAR, "*", currentToken.position))
            }
            
            result.add(currentToken)
            prevToken = currentToken
        }
        
        return result
    }
}
