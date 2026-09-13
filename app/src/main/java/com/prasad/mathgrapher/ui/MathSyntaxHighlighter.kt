package com.prasad.mathgrapher.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.prasad.mathgrapher.math.Tokenizer
import com.prasad.mathgrapher.math.TokenType

class MathSyntaxHighlighter(
    private val primaryColor: Color,
    private val secondaryColor: Color,
    private val errorColor: Color,
    private val defaultColor: Color
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val inputText = text.text
        
        val annotatedString = buildAnnotatedString {
            append(inputText)
            
            try {
                // Tokenize but ignore the synthetic implicit multiplication tokens for highlighting
                val tokenizer = Tokenizer(inputText)
                val tokens = tokenizer.tokenize().filter { it.type != TokenType.EOF && it.value.isNotEmpty() }
                
                for (i in tokens.indices) {
                    val token = tokens[i]
                    val start = token.position
                    val end = start + token.value.length
                    
                    if (start >= 0 && end <= inputText.length && inputText.substring(start, end) == token.value) {
                        val color = when (token.type) {
                            TokenType.NUMBER -> defaultColor
                            TokenType.PLUS, TokenType.MINUS, TokenType.STAR, TokenType.SLASH, TokenType.CARET, TokenType.PERCENT -> primaryColor
                            TokenType.LPAREN, TokenType.RPAREN, TokenType.COMMA -> defaultColor
                            TokenType.IDENTIFIER -> {
                                if (Tokenizer.KNOWN_FUNCTIONS.contains(token.value)) secondaryColor else defaultColor
                            }
                            else -> defaultColor
                        }
                        
                        addStyle(SpanStyle(color = color), start, end)
                    }
                }
            } catch (e: Exception) {
                // If it fails to parse completely, we might color the rest red
            }
        }
        
        return TransformedText(annotatedString, OffsetMapping.Identity)
    }
}
