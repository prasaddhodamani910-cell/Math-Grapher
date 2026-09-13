package com.prasad.mathgrapher.ui

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

@Composable
fun EquationInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onAddEquation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mathColors = LocalMathGrapherColors.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
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
