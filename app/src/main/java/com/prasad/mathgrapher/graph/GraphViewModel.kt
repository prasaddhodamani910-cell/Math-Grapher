package com.prasad.mathgrapher.graph

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.prasad.mathgrapher.math.AstNode
import com.prasad.mathgrapher.math.Evaluator
import com.prasad.mathgrapher.math.MathException
import com.prasad.mathgrapher.math.Parser
import com.prasad.mathgrapher.math.Tokenizer

data class Equation(
    val id: Int,
    val text: String,
    val ast: AstNode? = null,
    val error: String? = null,
    val colorIndex: Int = 0
)

class GraphViewModel : ViewModel() {
    private val _viewport = mutableStateOf(Viewport.DEFAULT)
    val viewport: State<Viewport> = _viewport
    
    private val _equations = mutableStateListOf<Equation>()
    val equations: List<Equation> get() = _equations
    
    private val _currentInput = mutableStateOf("")
    val currentInput: State<String> = _currentInput
    
    private val _inspectedPoint = mutableStateOf<Pair<Double, Double>?>(null)
    val inspectedPoint: State<Pair<Double, Double>?> = _inspectedPoint
    
    private var nextId = 0
    private var nextColorIndex = 0
    
    fun updateInput(text: String) {
        _currentInput.value = text
    }
    
    /**
     * Add an equation from the given input text.
     * Strips leading "y =" or "y=" prefix, then tokenizes/parses.
     */
    fun addEquation(inputText: String) {
        var exprText = inputText.trim()
        if (exprText.isEmpty()) return
        
        // Strip leading "y =" or "y="
        if (exprText.startsWith("y =")) {
            exprText = exprText.substring(3).trim()
        } else if (exprText.startsWith("y=")) {
            exprText = exprText.substring(2).trim()
        }
        
        // Sanitize unicode characters common on mobile keyboards
        exprText = exprText.replace("−", "-") // Unicode minus to ASCII hyphen
            .replace("²", "^2")
            .replace("³", "^3")
            .replace("×", "*")
            .replace("÷", "/")
        
        var ast: AstNode? = null
        var errorMsg: String? = null
        
        try {
            val tokens = Tokenizer(exprText).tokenize()
            ast = Parser(tokens).parse()
        } catch (e: MathException) {
            errorMsg = e.message
        } catch (e: Exception) {
            errorMsg = "Invalid equation"
        }
        
        _equations.add(
            Equation(
                id = nextId++,
                text = inputText,
                ast = ast,
                error = errorMsg,
                colorIndex = nextColorIndex++
            )
        )
        
        _currentInput.value = ""
    }
    
    fun removeEquation(id: Int) {
        _equations.removeAll { it.id == id }
    }
    
    fun updateViewport(viewport: Viewport) {
        _viewport.value = viewport
    }
    
    fun panViewport(dxScreen: Float, dyScreen: Float, screenWidth: Float, screenHeight: Float) {
        val dxWorld = (dxScreen / screenWidth) * _viewport.value.xRange
        val dyWorld = -(dyScreen / screenHeight) * _viewport.value.yRange
        _viewport.value = _viewport.value.pan(-dxWorld, -dyWorld)
    }
    
    fun zoomViewport(factor: Float, focusX: Float, focusY: Float, screenWidth: Float, screenHeight: Float) {
        val focusXWorld = _viewport.value.screenToWorldX(focusX, screenWidth)
        val focusYWorld = _viewport.value.screenToWorldY(focusY, screenHeight)
        _viewport.value = _viewport.value.zoom(factor.toDouble(), focusXWorld, focusYWorld)
    }
    
    fun zoomCenter(factor: Float) {
        val centerXWorld = (_viewport.value.xMin + _viewport.value.xMax) / 2.0
        val centerYWorld = (_viewport.value.yMin + _viewport.value.yMax) / 2.0
        _viewport.value = _viewport.value.zoom(factor.toDouble(), centerXWorld, centerYWorld)
    }
    
    fun resetViewport() {
        _viewport.value = Viewport.DEFAULT
    }
    
    fun inspectPoint(screenX: Float, screenY: Float, screenWidth: Float, screenHeight: Float) {
        val x = _viewport.value.screenToWorldX(screenX, screenWidth)
        val y = _viewport.value.screenToWorldY(screenY, screenHeight)
        _inspectedPoint.value = Pair(x, y)
    }
    
    fun clearInspection() {
        _inspectedPoint.value = null
    }
}
