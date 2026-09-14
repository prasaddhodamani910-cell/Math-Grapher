# Math Grapher — Internet Access, Google Sign-In, Animations & Daily Notifications

Paste-in guide, same as before — nothing in your project has been touched. Your app
package is `com.prasad.mathgrapher` (confirmed from your `build.gradle.kts`).

Do the parts in this order: **1 → 2 → 3 → 4**. Part 2 (Google Sign-In) needs real
setup outside the code (a free Google Cloud project) before any of its code will work,
so budget time for that specifically.

---

## PART 1 — Internet access

**File:** `app/src/main/AndroidManifest.xml`

Your manifest currently has no permissions at all. Add this line right above the
`<application ...>` tag:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

That's it — `INTERNET` is a "normal" permission, Android grants it automatically at
install time, no popup, no runtime request needed. You'll need it for Part 2 (Google
Sign-In talks to Google's servers) — the app doesn't currently need it for anything
else since graphing is 100% on-device math.

---

## PART 2 — "Sign in with Google"

Modern Android apps use a library called **Credential Manager** for this — the older
`GoogleSignInClient` approach is deprecated. Credential Manager needs no Firebase
project, just a free Google Cloud project.

### 2A. One-time external setup (can't be done in code)

**Step 1 — get your debug keystore's SHA-1 fingerprint, in Termux:**

```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

(If `keytool` isn't found: `pkg install openjdk-17` first — you already need a JDK to
build the app anyway, so it's likely already installed.)

Copy the line that starts with `SHA1:` — you'll paste that hex string into the console
in Step 3.

**Step 2 — set up the OAuth consent screen:**
1. Go to [console.cloud.google.com](https://console.cloud.google.com), sign in, create a new project (or pick an existing one) — free.
2. Left menu → **APIs & Services → OAuth consent screen**.
3. User type: **External**. Fill in app name ("Math Grapher"), your email as support contact.
4. Leave it in **Testing** mode and add your own Google account under **Test users**. Testing mode is free and needs no Google review — fine for a personal project. (Only "Publishing" the consent screen for public strangers requires verification.)

**Step 3 — create two OAuth client IDs:**
1. **APIs & Services → Credentials → Create Credentials → OAuth client ID**
2. First one — type **Android**: package name `com.prasad.mathgrapher` (or `com.prasad.mathgrapher.debug` if you're testing a debug build, check your `applicationIdSuffix`), and paste the SHA-1 from Step 1.
3. Second one — type **Web application**: no package name needed. This one gives you a **Client ID** ending in `.apps.googleusercontent.com` — **this is the one your code actually uses**, referred to below as `WEB_CLIENT_ID`. The Android client ID from step 2 is only used by Google to verify your app's identity; it never appears in your code.

### 2B. Gradle dependencies

**File:** `app/build.gradle.kts`

```kotlin
dependencies {
    // ... your existing dependencies stay as-is, add these:
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
}
```

Check [the androidx Credential Manager release notes](https://developer.android.com/jetpack/androidx/releases/credentials)
for a newer stable version before building — this library moves fast and these
numbers may be a minor version behind by the time you read this. Avoid `-alpha` /
`-beta` versions for anything you plan to keep working long-term.

### 2C. The sign-in code

Create a new file, e.g. `auth/GoogleAuth.kt`:

```kotlin
package com.prasad.mathgrapher.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

private const val WEB_CLIENT_ID = "PASTE_YOUR_WEB_CLIENT_ID_HERE.apps.googleusercontent.com"

data class GoogleUser(val name: String?, val email: String?, val photoUrl: String?)

suspend fun signInWithGoogle(context: Context): GoogleUser? {
    val credentialManager = CredentialManager.create(context)
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false) // show all Google accounts on the device, not just ones that used this app before
        .setServerClientId(WEB_CLIENT_ID)
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    return try {
        val result = credentialManager.getCredential(context, request)
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val tokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            GoogleUser(
                name = tokenCredential.displayName,
                email = tokenCredential.id, // this field holds the account's email
                photoUrl = tokenCredential.profilePictureUri?.toString()
            )
        } else null
    } catch (e: GetCredentialException) {
        null // user cancelled, no Google account on device, etc. — treat as "not signed in"
    }
}
```

**Important honesty note:** this gives you the user's name/email/photo to *display* in
your app — it does not verify anything with a backend server, because you don't have
one. That's fine for a personal project (showing "Hi, Prasad 👋" in your UI), but don't
call this "secure login" if you ever add a server — a real backend would need to verify
the ID token itself, which is a separate step this guide doesn't cover.

### 2D. Hook it into a login screen

```kotlin
@Composable
fun LoginScreen(onSignedIn: (GoogleUser) -> Unit, onSkip: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSigningIn by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ... your logo/title go here — see Part 3 for the animated version

        Button(
            enabled = !isSigningIn,
            onClick = {
                isSigningIn = true
                scope.launch {
                    val user = signInWithGoogle(context)
                    isSigningIn = false
                    if (user != null) onSignedIn(user)
                }
            }
        ) {
            Text(if (isSigningIn) "Signing in..." else "Sign in with Google")
        }

        TextButton(onClick = onSkip) { Text("Continue without signing in") }
    }
}
```

Always keep the "Continue without signing in" option — sign-in can fail for reasons
outside your control (no Google account on the test device, user cancels the sheet,
Play Protect environment issues), and the graphing itself doesn't need an account at
all.

---

## PART 3 — Animations

### 3A. Animated login screen

Fade + slide the content in when the screen first appears:

```kotlin
@Composable
fun LoginScreen(onSignedIn: (GoogleUser) -> Unit, onSkip: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 3 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                PulsingLogo()
                Spacer(Modifier.height(24.dp))
                Text("Math Grapher", style = MaterialTheme.typography.displayLarge)
                // ... rest of your login content
            }
        }
    }
}
```

A gently pulsing logo (infinite, subtle — not distracting):

```kotlin
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
        imageVector = Icons.Default.Info, // swap for your actual app icon/logo composable
        contentDescription = null,
        modifier = Modifier.size(72.dp).graphicsLayer(scaleX = scale, scaleY = scale)
    )
}
```

### 3B. Curves draw themselves in instead of just appearing

**Files:** `graph/GraphViewModel.kt` and `graph/GraphCanvas.kt`

Right now a new equation's curve just pops onto the screen fully drawn. To make it
draw itself in over ~600ms:

```kotlin
// in GraphCanvas.kt, inside the Composable, before the "--- Draw Curves ---" loop:
val progressMap = remember { mutableStateMapOf<Int, Animatable<Float, AnimationVector1D>>() }

for (equation in equations) {
    val animatable = progressMap.getOrPut(equation.id) { Animatable(0f) }
    LaunchedEffect(equation.id) {
        animatable.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))
    }
}
```

Then, when calling `drawCurve`, pass the progress and only draw that fraction of the
sampled points:

```kotlin
private fun DrawScope.drawCurve(points: List<SamplePoint>, color: Color, progress: Float = 1f) {
    if (points.isEmpty()) return
    val visibleCount = (points.size * progress).toInt().coerceIn(2, points.size)
    val visiblePoints = points.subList(0, visibleCount)

    val path = Path()
    var pathStarted = false
    for (i in visiblePoints.indices) {
        val point = visiblePoints[i]
        if (point.isValid) {
            if (!pathStarted) { path.moveTo(point.x, point.y); pathStarted = true }
            else {
                if (i > 0 && visiblePoints[i - 1].isValid) {
                    val dy = abs(point.y - visiblePoints[i - 1].y)
                    if (dy > size.height * 0.8f) { path.moveTo(point.x, point.y); continue }
                }
                path.lineTo(point.x, point.y)
            }
        } else pathStarted = false
    }
    drawPath(path = path, color = color, style = Stroke(width = 3.dp.toPx()))
}
```

Pass `progressMap[equation.id]?.value ?: 1f` as the `progress` argument at each
`drawCurve(...)` call site. Implicit curves (marching-squares segments) don't animate
as naturally this way — leave those appearing instantly, or animate their `alpha`
instead of a point count (`drawLine(..., alpha = progress)`).

---

## PART 4 — Random-hour "the app is alive" notification

One local notification a day, at a **different random hour each day**, all on-device
— no server needed.

### 4A. Gradle dependency

**File:** `app/build.gradle.kts`

```kotlin
implementation("androidx.work:work-runtime-ktx:2.9.1")
```

(Check [WorkManager's release notes](https://developer.android.com/jetpack/androidx/releases/work)
for anything newer/stable before building.)

### 4B. Manifest

**File:** `AndroidManifest.xml` — add alongside the `INTERNET` line from Part 1:

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

On Android 13+ this is a **runtime** permission — the manifest line alone isn't
enough, you also need to ask the user (Part 4E).

### 4C. The notification itself

New file, e.g. `notifications/DailyNudge.kt`:

```kotlin
package com.prasad.mathgrapher.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.prasad.mathgrapher.MainActivity
import com.prasad.mathgrapher.R

private const val CHANNEL_ID = "daily_nudge_channel"
private const val NOTIFICATION_ID = 1001

private val NUDGE_MESSAGES = listOf(
    "A curve is waiting to be graphed 📈",
    "Try a new equation today?",
    "Your graph paper misses you.",
    "Quick math break?",
    "Got a tricky equation? Come test it."
)

fun createNudgeChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Daily reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}

fun showNudgeNotification(context: Context) {
    if (Build.VERSION.SDK_INT >= 33 &&
        ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) {
        return // permission not granted — skip silently, don't crash
    }

    val intent = Intent(context, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent,
        PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground) // reuses your existing icon, no new asset needed
        .setContentTitle("Math Grapher")
        .setContentText(NUDGE_MESSAGES.random())
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
}
```

### 4D. The worker that fires it — and reschedules itself for tomorrow at a NEW random hour

```kotlin
package com.prasad.mathgrapher.notifications

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

private const val WORK_NAME = "daily_nudge"

class DailyNudgeWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        showNudgeNotification(applicationContext)
        scheduleNextNudge(applicationContext, ExistingWorkPolicy.REPLACE) // chain: pick tomorrow's random hour now
        return Result.success()
    }
}

// windowStartHour/EndHour: the range of hours it's allowed to fire in, 24h format.
// 10..20 means "somewhere between 10am and 8pm" — adjust to taste.
private fun computeRandomDelayMillis(windowStartHour: Int = 10, windowEndHour: Int = 20): Long {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, (windowStartHour until windowEndHour).random())
        set(Calendar.MINUTE, (0..59).random())
        set(Calendar.SECOND, 0)
    }
    return (target.timeInMillis - now.timeInMillis).coerceAtLeast(60_000L)
}

fun scheduleNextNudge(context: Context, policy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP) {
    val request = OneTimeWorkRequestBuilder<DailyNudgeWorker>()
        .setInitialDelay(computeRandomDelayMillis(), TimeUnit.MILLISECONDS)
        .build()
    WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, policy, request)
}
```

The `REPLACE` vs `KEEP` distinction matters:
- The **worker itself** uses `REPLACE` when it chains the next day's nudge — it just
  fired, so there's nothing to "keep," it needs to schedule a fresh one.
- The **app's own startup call** (next section) uses `KEEP` — so relaunching the app
  five times in one day doesn't keep resetting the countdown to "a random hour
  starting from right now," which would make it fire less randomly and more
  "shortly after whenever you last opened the app."

### 4E. Wire it up in `MainActivity.kt`

```kotlin
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import android.os.Build
```

```kotlin
// inside onCreate, before setContent { ... }:
com.prasad.mathgrapher.notifications.createNudgeChannel(this)
```

```kotlin
// inside your top-level @Composable (e.g. right at the start of GraphScreen or a
// wrapper Composable that hosts it):
val context = LocalContext.current
val notificationPermissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
) { granted ->
    if (granted) {
        com.prasad.mathgrapher.notifications.scheduleNextNudge(context)
    }
    // if denied, do nothing — the app still works fully, it just won't nudge you
}

LaunchedEffect(Unit) {
    if (Build.VERSION.SDK_INT >= 33) {
        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
    } else {
        com.prasad.mathgrapher.notifications.scheduleNextNudge(context)
    }
}
```

### 4F. What to expect

- The first notification lands at a random time **tomorrow**, somewhere in your
  chosen window (10am–8pm by default) — not today, since "today" might already be
  past that window by the time you install it.
- After that, each notification schedules the next one, so it keeps going
  indefinitely at a fresh random hour each day, without needing the app to be
  reopened.
- Android's battery optimization (Doze mode) can shift the exact minute by a bit if
  the phone's been idle — that's normal and actually harmless here, since the whole
  point is "not the exact same time every day" anyway.
- If the user denies the notification permission, nothing breaks — `showNudgeNotification`
  checks for it and just skips silently, and the rest of the app is unaffected.
