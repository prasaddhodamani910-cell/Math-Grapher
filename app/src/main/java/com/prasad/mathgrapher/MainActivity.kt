package com.prasad.mathgrapher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.os.Build
import com.prasad.mathgrapher.auth.GoogleUser
import com.google.firebase.auth.FirebaseAuth
import com.prasad.mathgrapher.ui.GraphScreen
import com.prasad.mathgrapher.ui.LoginScreen
import com.prasad.mathgrapher.notifications.createNudgeChannel
import com.prasad.mathgrapher.notifications.scheduleNextNudge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNudgeChannel(this)
        setContent {
            val context = LocalContext.current
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                if (granted) {
                    scheduleNextNudge(context)
                }
            }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= 33) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    scheduleNextNudge(context)
                }
            }

            var user by remember { 
                val currentUser = FirebaseAuth.getInstance().currentUser
                val initialUser = currentUser?.let {
                    GoogleUser(
                        name = it.displayName,
                        email = it.email,
                        photoUrl = it.photoUrl?.toString(),
                        uid = it.uid
                    )
                }
                mutableStateOf(initialUser) 
            }
            var skippedLogin by remember { mutableStateOf(false) }

            if (user == null && !skippedLogin) {
                LoginScreen(
                    onSignedIn = { user = it },
                    onSkip = { skippedLogin = true }
                )
            } else {
                GraphScreen(
                    user = user,
                    onSignOut = {
                        FirebaseAuth.getInstance().signOut()
                        user = null
                        skippedLogin = false
                    }
                )
            }
        }
    }
}
