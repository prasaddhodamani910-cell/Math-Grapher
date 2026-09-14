package com.prasad.mathgrapher.ui
import androidx.compose.foundation.layout.imePadding

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
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider

import com.prasad.mathgrapher.ui.theme.LocalMathGrapherColors
import com.prasad.mathgrapher.ui.theme.MathGrapherTheme
import com.prasad.mathgrapher.auth.GoogleUser

@Composable
fun GraphScreen(
    user: GoogleUser? = null,
    onSignOut: () -> Unit = {},
    viewModel: GraphViewModel = viewModel()
) {
    val systemDark = isSystemInDarkTheme()
    var isDarkTheme by rememberSaveable { mutableStateOf(true) } // Default to dark first

    MathGrapherTheme(darkTheme = isDarkTheme) {
        var showHelpDialog by rememberSaveable { mutableStateOf(false) }
        var showMenu by rememberSaveable { mutableStateOf(false) }
        var showAboutDialog by rememberSaveable { mutableStateOf(false) }
        var showCreditsDialog by rememberSaveable { mutableStateOf(false) }
        var showProfileDialog by rememberSaveable { mutableStateOf(false) }
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
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.width(230.dp)
                        ) {
                            // --- Settings ---
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("Angle unit", style = MaterialTheme.typography.labelSmall, color = mathColors.onSurfaceMuted)
                                        Text(if (viewModel.isDegreesMode.value) "Degrees" else "Radians", fontWeight = FontWeight.Medium)
                                    }
                                },
                                leadingIcon = { Text("∠", fontSize = 18.sp) },
                                onClick = {
                                    viewModel.toggleAngleMode()
                                    showMenu = false
                                }
                            )

                            Divider(modifier = Modifier.padding(vertical = 4.dp))

                            // --- Info ---
                            if (user != null) {
                                DropdownMenuItem(
                                    text = { Text("Profile") },
                                    leadingIcon = { Text("\uD83D\uDC64", fontSize = 16.sp) },
                                    onClick = {
                                        showMenu = false
                                        showProfileDialog = true
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Help — how to write equations") },
                                leadingIcon = { Text("❓", fontSize = 16.sp) },
                                onClick = {
                                    showMenu = false
                                    showHelpDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("About") },
                                leadingIcon = { Icon(imageVector = Icons.Default.Info, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    showAboutDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Credits") },
                                leadingIcon = { Text("★", fontSize = 16.sp) },
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
                        onEquationRuntimeStatus = { id, message -> 
                            viewModel.setRuntimeNote(id, message) 
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
                    modifier = Modifier.fillMaxWidth().imePadding()
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

        if (showHelpDialog) {
            AlertDialog(
                onDismissRequest = { showHelpDialog = false },
                title = { Text("How to write equations") },
                text = {
                    LazyColumn(modifier = Modifier.height(420.dp)) {
                        item {
                            Text(
                                "This app understands 5 kinds of equations. Pick the form that " +
                                "matches what you're trying to graph:",
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                        item { HelpSection(
                            title = "1. Explicit — y in terms of x",
                            body = "Write it starting with \"y =\". Covers lines, parabolas, " +
                                   "sine/cosine waves, anything you can solve for y.",
                            examples = listOf("y = 2*x + 1", "y = x^2 - 4", "y = sin(x)")
                        ) }
                        item { HelpSection(
                            title = "2. Implicit — x and y mixed together",
                            body = "No clean \"y =\" form. Used for circles, ellipses, and " +
                                   "curves where y can't be isolated.",
                            examples = listOf("x^2 + y^2 = 25", "x^2/4 + y^2/9 = 1")
                        ) }
                        item { HelpSection(
                            title = "3. Parametric — both x and y depend on t",
                            body = "Separate the x-part and y-part with a comma. Great for " +
                                   "circles, spirals, and anything that looks like motion.",
                            examples = listOf("x = cos(t), y = sin(t)", "x = t, y = t^2")
                        ) }
                        item { HelpSection(
                            title = "4. Polar — distance from center depends on angle",
                            body = "Start with \"r =\". Used for spirals, roses, and cardioids.",
                            examples = listOf("r = 2 + 2*cos(theta)", "r = theta")
                        ) }
                        item { HelpSection(
                            title = "5. Inequality — shade a region instead of a line",
                            body = "Use <, >, <=, or >= instead of =.",
                            examples = listOf("y < x^2", "x^2 + y^2 <= 9")
                        ) }
                        item {
                            Text(
                                "Common mistakes",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                            Text("• Function names need parentheses: write sin(x), not sinx.")
                            Text("• Use * for multiplication: 2*x, not 2x written as \"2x\" alone works, but a*b needs the star.")
                            Text("• Use ^ for powers: x^2, not x².")
                            Text("• Parametric equations need a comma between the x-part and y-part.")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showHelpDialog = false }) { Text("Got it") }
                }
            )
        }

        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text("About") },
                text = {
                    Column {
                        Text("Math Grapher graphs 5 kinds of equations:")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• Explicit (y = ...) — lines, parabolas, trig curves")
                        Text("• Implicit (x and y mixed) — circles, ellipses")
                        Text("• Parametric (x = ..., y = ...) — motion-style curves")
                        Text("• Polar (r = ...) — spirals, roses, cardioids")
                        Text("• Inequalities (<, >, <=, >=) — shaded regions")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Open Help from the ⋮ menu for the full writing guide with examples.")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }

        if (showProfileDialog && user != null) {
            ProfileDialog(
                user = user,
                onDismiss = { showProfileDialog = false },
                onSignOut = onSignOut
            )
        }

        if (showCreditsDialog) {
            val context = LocalContext.current
            androidx.compose.ui.window.Dialog(onDismissRequest = { showCreditsDialog = false }) {
                androidx.compose.material3.Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth()
                    ) {
                        Text(
                            "Credits",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Drawmath is a modern mathematical graphing app built to make exploring mathematics simple, visual, and interactive. It combines a clean Android interface with a powerful graphing engine for plotting equations and understanding mathematical concepts visually.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "The project is independently developed with a focus on simplicity, performance, privacy, and learning. It also includes secure cloud authentication and profile synchronization, allowing users to safely access their account across sessions.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Created by Prasad Dhodamani",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "An independent project built with curiosity, mathematics, and a lot of code.",
                            style = MaterialTheme.typography.bodySmall,
                            color = mathColors.onSurfaceMuted
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { showCreditsDialog = false }) {
                                Text("Close", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpSection(title: String, body: String, examples: List<String>) {
    Column(modifier = Modifier.padding(bottom = 14.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(body, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp, bottom = 4.dp))
        examples.forEach { ex ->
            Text("→ $ex", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

