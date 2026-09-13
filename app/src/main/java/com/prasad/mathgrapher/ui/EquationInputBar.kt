package com.prasad.mathgrapher.ui
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prasad.mathgrapher.ui.theme.EquationFontFamily
import com.prasad.mathgrapher.ui.theme.LocalMathGrapherColors

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun EquationInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onAddEquation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mathColors = LocalMathGrapherColors.current
    var isParametricMode by remember { mutableStateOf(false) }
    var parametricX by remember { mutableStateOf("") }
    var parametricY by remember { mutableStateOf("") }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            // Live Preview Strip
            if (!isParametricMode && value.isNotBlank()) {
                val previewText = value
                    .replace("*", "×")
                    .replace("/", "÷")
                    .replace("^2", "²")
                    .replace("^3", "³")
                    .replace("-", "−")
                
                Text(
                    text = previewText,
                    style = TextStyle(
                        fontFamily = EquationFontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
            
            // Mode Toggle
            Row(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp), horizontalArrangement = Arrangement.End) {
                Text(
                    text = if (isParametricMode) "Parametric Mode" else "Standard Mode",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.clickable { isParametricMode = !isParametricMode }.padding(4.dp)
                )
            }
            
            if (isParametricMode) {
                // Parametric Inputs
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("x(t) =", color = mathColors.onSurfaceMuted, style = TextStyle(fontFamily = EquationFontFamily, fontSize = 20.sp), modifier = Modifier.padding(end = 8.dp))
                        BasicTextField(
                            value = parametricX, onValueChange = { parametricX = it },
                            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontFamily = EquationFontFamily, fontSize = 20.sp),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary), singleLine = true, modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("y(t) =", color = mathColors.onSurfaceMuted, style = TextStyle(fontFamily = EquationFontFamily, fontSize = 20.sp), modifier = Modifier.padding(end = 8.dp))
                        BasicTextField(
                            value = parametricY, onValueChange = { parametricY = it },
                            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontFamily = EquationFontFamily, fontSize = 20.sp),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary), singleLine = true, modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (parametricX.isNotBlank() && parametricY.isNotBlank()) {
                                    onValueChange("x=${parametricX}, y=${parametricY}")
                                    onAddEquation()
                                    parametricX = ""
                                    parametricY = ""
                                }
                            },
                            modifier = Modifier.size(36.dp).background(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = CircleShape)
                        ) {
                            Text("+", color = MaterialTheme.colorScheme.primary, fontSize = 20.sp)
                        }
                    }
                }
            } else {
                // Standard Input
                Row(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp, top = if (value.isNotBlank()) 0.dp else 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "y =",
                        color = mathColors.onSurfaceMuted,
                        style = TextStyle(fontFamily = EquationFontFamily, fontSize = 20.sp),
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = EquationFontFamily,
                            fontSize = 20.sp
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        visualTransformation = MathSyntaxHighlighter(
                            primaryColor = MaterialTheme.colorScheme.primary,
                            secondaryColor = MaterialTheme.colorScheme.secondary,
                            errorColor = MaterialTheme.colorScheme.error,
                            defaultColor = MaterialTheme.colorScheme.onSurface
                        ),
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (value.isEmpty()) {
                                    Text(
                                        text = "Try: x² − 3",
                                        color = mathColors.onSurfaceMuted.copy(alpha = 0.5f),
                                        style = TextStyle(fontFamily = EquationFontFamily, fontSize = 20.sp)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onAddEquation,
                        modifier = Modifier
                            .size(36.dp)
                            .background(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = CircleShape)
                    ) {
                        Text(
                            text = "+",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }
}
