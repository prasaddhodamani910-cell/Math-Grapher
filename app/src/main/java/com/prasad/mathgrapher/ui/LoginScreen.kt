package com.prasad.mathgrapher.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.prasad.mathgrapher.auth.GoogleUser
import com.prasad.mathgrapher.auth.signInWithGoogle
import kotlinx.coroutines.launch

@Composable
fun PulsingLogo() {
    val infiniteTransition = rememberInfiniteTransition(label = "logoPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    Icon(
        imageVector = Icons.Default.Info, // Use your actual logo if available
        contentDescription = null,
        modifier = Modifier.size(72.dp).graphicsLayer(scaleX = scale, scaleY = scale),
        tint = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun LoginScreen(onSignedIn: (GoogleUser) -> Unit, onSkip: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSigningIn by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp), 
        horizontalAlignment = Alignment.CenterHorizontally, 
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 3 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                PulsingLogo()
                Spacer(Modifier.height(24.dp))
                Text("Math Grapher", style = MaterialTheme.typography.displayLarge)
                Spacer(Modifier.height(48.dp))
                
                Button(
                    enabled = !isSigningIn,
                    onClick = {
                        isSigningIn = true
                        scope.launch {
                            val user = signInWithGoogle(context)
                            isSigningIn = false
                            if (user != null) {
                                try {
                                    com.prasad.mathgrapher.auth.AuthRepository.saveUserProfile(user)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                onSignedIn(user)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
                ) {
                    Text(if (isSigningIn) "Signing in..." else "Sign in with Google")
                }
                
                Spacer(Modifier.height(16.dp))
                
                TextButton(onClick = onSkip) { 
                    Text("Continue without signing in") 
                }
            }
        }
    }
}
