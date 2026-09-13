package com.prasad.mathgrapher.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prasad.mathgrapher.graph.GraphCanvas
import com.prasad.mathgrapher.graph.GraphViewModel
import com.prasad.mathgrapher.ui.theme.LocalMathGrapherColors
import com.prasad.mathgrapher.ui.theme.MathGrapherTheme

@Composable
fun GraphScreen(
    viewModel: GraphViewModel = viewModel()
) {
    val systemDark = isSystemInDarkTheme()
    var isDarkTheme by rememberSaveable { mutableStateOf(true) } // Default to dark first

    MathGrapherTheme(darkTheme = isDarkTheme) {
        var showMenu by rememberSaveable { mutableStateOf(false) }
        var showAboutDialog by rememberSaveable { mutableStateOf(false) }
        var showCreditsDialog by rememberSaveable { mutableStateOf(false) }
        val mathColors = LocalMathGrapherColors.current
        var inputText by rememberSaveable { mutableStateOf("") }
        val equations = viewModel.equations
        val viewport by viewModel.viewport
        val inspectedPoint by viewModel.inspectedPoint

        Scaffold(
            modifier = Modifier.systemBarsPadding(),
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Math Grapher",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { isDarkTheme = !isDarkTheme }) {
                        Text(
                            text = if (isDarkTheme) "☀️" else "🌙",
                            fontSize = 20.sp
                        )
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Add") },
                                onClick = { showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("About") },
                                onClick = { 
                                    showMenu = false 
                                    showAboutDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Credits") },
                                onClick = { 
                                    showMenu = false
                                    showCreditsDialog = true
                                }
                            )
                        }
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Graph canvas area (fills remaining space)
                Box(modifier = Modifier.weight(1f)) {
                    GraphCanvas(
                        equations = equations,
                        viewport = viewport,
                        curveColors = mathColors.curveColors,
                        inspectedPoint = inspectedPoint,
                        onPan = { dx, dy, width, height ->
                            viewModel.panViewport(dx, dy, width, height)
                        },
                        onZoom = { factor, focusX, focusY, width, height ->
                            viewModel.zoomViewport(factor, focusX, focusY, width, height)
                        },
                        onTap = { x, y, width, height ->
                            viewModel.inspectPoint(x, y, width, height)
                        },
                        mathColors = mathColors,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Vertical floating graph controls
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 2.dp,
                            modifier = Modifier.width(48.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clickable { viewModel.zoomCenter(0.5f) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(mathColors.onSurfaceMuted.copy(alpha = 0.2f))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clickable { viewModel.resetViewport() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("⌖", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(mathColors.onSurfaceMuted.copy(alpha = 0.2f))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clickable { viewModel.zoomCenter(2f) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("−", fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }

                // Bottom Panel for Equations
                Surface(
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = mathColors.surfaceElevated,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 24.dp)) {
                        // Drag handle indicator
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height(4.dp)
                                .background(mathColors.onSurfaceMuted.copy(alpha = 0.3f), CircleShape)
                                .align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Equations",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Equation list
                        EquationList(
                            equations = equations,
                            curveColors = mathColors.curveColors,
                            onRemoveEquation = { viewModel.removeEquation(it) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Equation input bar
                        EquationInputBar(
                            value = inputText,
                            onValueChange = { inputText = it },
                            onAddEquation = {
                                if (inputText.isNotBlank()) {
                                    viewModel.addEquation(inputText)
                                    inputText = ""
                                }
                            },
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }

        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text("About") },
                text = { Text("Math Grapher is a powerful tool to graph mathematical equations.") },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }

        if (showCreditsDialog) {
            val context = LocalContext.current
            AlertDialog(
                onDismissRequest = { showCreditsDialog = false },
                title = { Text("Credits") },
                text = {
                    Column {
                        Text("Developer: Prasad Dhodamani")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("11th Standard Student")
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Instagram: @prasad.dhodamani",
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/prasad.dhodamani"))
                                context.startActivity(intent)
                            },
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Email: prasaddhodamani910@gmail.com",
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:prasaddhodamani910@gmail.com")
                                }
                                context.startActivity(intent)
                            },
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("13 September 2026")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCreditsDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
