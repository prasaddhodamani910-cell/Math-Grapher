package com.prasad.mathgrapher.graph

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.prasad.mathgrapher.math.AstNode
import com.prasad.mathgrapher.math.EquationClassifier
import com.prasad.mathgrapher.math.EquationType
import com.prasad.mathgrapher.math.MathException
import com.prasad.mathgrapher.math.Parser
import com.prasad.mathgrapher.math.Tokenizer

data class Equation(
    val id: Int,
    val text: String,
    val ast: AstNode? = null,
    val equationType: EquationType? = null,
    val error: String? = null,
    val runtimeNote: String? = null,
    val colorIndex: Int = 0
)

class GraphViewModel : ViewModel() {
    private val _viewport = mutableStateOf(Viewport.DEFAULT)
    val viewport: State<Viewport> = _viewport
    
    private val _equations = mutableStateListOf<Equation>()
    val equations: List<Equation> get() = _equations
    
    fun setRuntimeNote(id: Int, message: String?) {
        val index = _equations.indexOfFirst { it.id == id }
        if (index != -1 && _equations[index].runtimeNote != message) {
            _equations[index] = _equations[index].copy(runtimeNote = message)
        }
    }
    
    private val _currentInput = mutableStateOf("")
    val currentInput: State<String> = _currentInput
    
    private val _inspectedPoint = mutableStateOf<Pair<Double, Double>?>(null)
    val inspectedPoint: State<Pair<Double, Double>?> = _inspectedPoint
    
    private var nextId = 0
    private var nextColorIndex = 0
    
    private val _isDegreesMode = mutableStateOf(com.prasad.mathgrapher.math.Evaluator.isDegreesMode)
    val isDegreesMode: State<Boolean> = _isDegreesMode

    fun toggleAngleMode() {
        com.prasad.mathgrapher.math.Evaluator.isDegreesMode = !com.prasad.mathgrapher.math.Evaluator.isDegreesMode
        _isDegreesMode.value = com.prasad.mathgrapher.math.Evaluator.isDegreesMode
    }
    
    fun updateInput(text: String) {
        _currentInput.value = text
    }
    
    /**
     * Add an equation from the given input text.
     * Uses EquationClassifier to auto-detect equation type (explicit, implicit, parametric, polar, inequality).
     */
    fun addEquation(inputText: String) {
        val trimmed = inputText.trim()
        if (trimmed.isEmpty()) return
        
        var eqType: EquationType? = null
        var ast: AstNode? = null
        var errorMsg: String? = null
        
        try {
            eqType = EquationClassifier.classify(trimmed)
            // For backward compat, also set ast for explicit equations
            if (eqType is EquationType.Explicit) {
                ast = eqType.expr
            }
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
                equationType = eqType,
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
