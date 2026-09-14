package com.prasad.mathgrapher.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.prasad.mathgrapher.R
import com.prasad.mathgrapher.auth.GoogleUser
import com.prasad.mathgrapher.auth.signInWithGoogle
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onSignedIn: (GoogleUser) -> Unit, onSkip: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSigningIn by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) { visible = true }
    
    val scale = remember { Animatable(0.8f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = tween(1000))
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(800)) + scaleIn(tween(800))
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .graphicsLayer {
                            scaleX = scale.value
                            scaleY = scale.value
                        },
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text("Math Grapher", style = MaterialTheme.typography.displayLarge)
                Spacer(Modifier.height(48.dp))
                
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
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
                                        com.prasad.mathgrapher.auth.AuthRepository.saveUserProfile(user)
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
                    modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
                ) {
                    Text(if (isSigningIn) "Signing in..." else "Sign in with Google")
                }
                
                Spacer(Modifier.height(16.dp))
                
                TextButton(onClick = onSkip, enabled = !isSigningIn) {
                    Text("Skip for now")
                }
            }
        }
    }
}
