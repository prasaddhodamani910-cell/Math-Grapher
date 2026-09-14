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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No equations yet", color = mathColors.onSurfaceMuted, style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    var errorDialogFor by remember { mutableStateOf<Equation?>(null) }

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 56.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
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
                val color = if (curveColors.isNotEmpty()) {
                    curveColors[equation.colorIndex % curveColors.size]
                } else {
                    Color.Blue
                }
                
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.08f), // Tonal fill
                    modifier = Modifier.heightIn(min = 40.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Color dot matching the curve
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(color = color, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        val statusMessage = equation.error ?: equation.runtimeNote
                        val textColor = when {
                            equation.error != null -> MaterialTheme.colorScheme.error
                            equation.runtimeNote != null -> mathColors.onSurfaceMuted
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                        
                        // Equation text
                        Text(
                            text = equation.text.take(15) + if (equation.text.length > 15) "..." else "",
                            style = TextStyle(
                                fontFamily = EquationFontFamily, 
                                fontSize = 16.sp, 
                                fontWeight = FontWeight.Medium
                            ),
                            color = textColor,
                            modifier = Modifier.clickable(enabled = statusMessage != null) {
                                errorDialogFor = equation
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Remove button
                        Text(
                            text = "✕",
                            color = mathColors.onSurfaceMuted,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { onRemoveEquation(equation.id) }
                        )
                    }
                }
            }
        }
    }
    
    errorDialogFor?.let { eq ->
        AlertDialog(
            onDismissRequest = { errorDialogFor = null },
            title = { Text(if (eq.error != null) "Couldn't graph this" else "Nothing to show") },
            text = { Text(eq.error ?: eq.runtimeNote ?: "") },
            confirmButton = {
                TextButton(onClick = { errorDialogFor = null }) { Text("OK") }
            }
        )
    }
}
