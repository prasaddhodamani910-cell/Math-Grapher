package com.prasad.mathgrapher.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.prasad.mathgrapher.R
import com.prasad.mathgrapher.auth.GoogleUser
import com.prasad.mathgrapher.auth.signInWithGoogle
import com.prasad.mathgrapher.ui.theme.LocalMathGrapherColors
import kotlinx.coroutines.launch
import kotlin.math.sin

@Composable
fun AnimatedMathBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "math_waves")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(animation = tween(4000, easing = LinearEasing)),
        label = "phase"
    )

    val primaryColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    val mathColors = LocalMathGrapherColors.current
    val secondaryColor = if (mathColors.curveColors.isNotEmpty()) mathColors.curveColors[1].copy(alpha = 0.3f) else primaryColor

    Canvas(modifier = Modifier.fillMaxSize()) {
        val path1 = Path()
        val path2 = Path()
        val width = size.width
        val height = size.height

        for (x in 0..width.toInt() step 5) {
            val normalizedX = (x / width) * 2f * Math.PI.toFloat()
            
            // Wave 1
            val y1 = height / 2f + sin(normalizedX + phase) * (height / 6f)
            if (x == 0) path1.moveTo(x.toFloat(), y1) else path1.lineTo(x.toFloat(), y1)

            // Wave 2
            val y2 = height / 2f + sin(normalizedX * 1.5f - phase) * (height / 5f)
            if (x == 0) path2.moveTo(x.toFloat(), y2) else path2.lineTo(x.toFloat(), y2)
        }

        drawPath(path1, primaryColor, style = Stroke(width = 8f))
        drawPath(path2, secondaryColor, style = Stroke(width = 8f))
    }
}

@Composable
fun PulsingLogo() {
    val infiniteTransition = rememberInfiniteTransition(label = "logoPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    Icon(
        painter = painterResource(id = R.drawable.ic_launcher_foreground),
        contentDescription = "Logo",
        modifier = Modifier
            .size(100.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale),
        tint = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun LoginScreen(onSignedIn: (GoogleUser) -> Unit, onSkip: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSigningIn by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var visible by remember { mutableStateOf(false) }
    val mathColors = LocalMathGrapherColors.current
    
    LaunchedEffect(Unit) { visible = true }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Beautiful animated gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            mathColors.surfaceElevated.copy(alpha = 0.5f)
                        )
                    )
                )
        )
        
        AnimatedMathBackground()

        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { it / 4 }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PulsingLogo()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Math Grapher",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Visualize your equations beautifully.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = mathColors.onSurfaceMuted
                        )
                        Spacer(Modifier.height(48.dp))
                        
                        if (errorMessage != null) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth(),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red)
                            ) {
                                Text(
                                    text = errorMessage!!,
                                    color = Color.Red,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                        
                        Button(
                            enabled = !isSigningIn,
                            onClick = {
                                isSigningIn = true
                                errorMessage = null
                                scope.launch {
                                    val result = signInWithGoogle(context)
                                    isSigningIn = false
                                    result.fold(
                                        onSuccess = { user ->
                                            try {
                                                scope.launch { com.prasad.mathgrapher.auth.AuthRepository.saveUserProfile(user) }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                            onSignedIn(user)
                                        },
                                        onFailure = { e ->
                                            errorMessage = "ERROR: ${e.javaClass.simpleName} - ${e.message}"
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (isSigningIn) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.background, strokeWidth = 2.dp)
                            } else {
                                Text("Sign in with Google", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.background)
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        TextButton(
                            onClick = onSkip,
                            enabled = !isSigningIn,
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Skip for now", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
