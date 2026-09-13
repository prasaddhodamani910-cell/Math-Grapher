package com.prasad.mathgrapher.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prasad.mathgrapher.graph.Equation
import com.prasad.mathgrapher.ui.theme.EquationFontFamily
import com.prasad.mathgrapher.ui.theme.LocalMathGrapherColors

@Composable
fun EquationList(
    equations: List<Equation>,
    curveColors: List<Color>,
    onRemoveEquation: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val mathColors = LocalMathGrapherColors.current
    if (equations.isEmpty()) {
        Column(
            modifier = modifier.fillMaxWidth().padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No equations yet", color = mathColors.onSurfaceMuted, style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = equations,
            key = { it.id }
        ) { equation ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(150)),
                exit = fadeOut(animationSpec = tween(150))
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Color dot matching the curve
                        val color = if (curveColors.isNotEmpty()) {
                            curveColors[equation.colorIndex % curveColors.size]
                        } else {
                            Color.Blue
                        }
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(color = color, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        // Equation text
                        Text(
                            text = equation.text,
                            style = TextStyle(
                                fontFamily = EquationFontFamily, 
                                fontSize = 18.sp, 
                                fontWeight = FontWeight.Medium
                            ),
                            color = if (equation.error != null) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        // Remove button (using X for simplicity in absence of more icons)
                        Text(
                            text = "✕",
                            color = mathColors.onSurfaceMuted,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { onRemoveEquation(equation.id) }
                                .padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}
