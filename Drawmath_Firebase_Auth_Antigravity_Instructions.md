# Drawmath — Firebase Authentication & Cloud Profile Implementation Plan

## Purpose

Implement authentication for the existing **Drawmath Android app**.

The app is built in **Kotlin + Jetpack Compose** and is developed/builds through **Termux/Gradle**, not Android Studio.

The authentication system should support:

- Google Sign-In
- GitHub Sign-In
- LinkedIn Sign-In if the Firebase/LinkedIn configuration supports the required OAuth/OIDC flow
- Cloud-stored user profile
- Profile photo URL, display name, email and provider information
- A stable Firebase UID
- Automatic restoration of the user's profile when they sign in again
- Ability to link multiple providers to one Drawmath account
- Secure Firestore rules
- No passwords or OAuth client secrets stored in the Android app

## Existing project facts

Use the existing project; do **not** replace or rewrite the graphing engine.

Package/application ID:

```text
com.prasad.mathgrapher
```

Debug application ID is:

```text
com.prasad.mathgrapher.debug
```

Current project-level Gradle plugins include:

```kotlin
id("com.android.application") version "8.5.2" apply false
id("org.jetbrains.kotlin.android") version "2.0.20" apply false
id("org.jetbrains.kotlin.plugin.compose") version "2.0.20" apply false
```

Current Android configuration:

- compileSdk 34
- targetSdk 34
- minSdk 26
- Java 17
- Kotlin JVM target 17
- Compose enabled
- release minification/shrinking enabled
- debug has `applicationIdSuffix = ".debug"`

Firebase project:

```text
Project name: Math Grapher
Project ID: math-grapher
Android app package: com.prasad.mathgrapher
```

The user has already:

1. Created the Firebase project.
2. Registered the Android app in Firebase.
3. Downloaded `google-services.json`.
4. Placed `google-services.json` in the app module.
5. Started adding the Firebase Gradle configuration.

---

# IMPORTANT RULES FOR THE CODING AGENT

## 1. Preserve the existing app

Do NOT rewrite the graphing engine, equation parser, graph renderer, UI architecture, or unrelated files.

Make the smallest clean changes necessary.

Before editing:

```bash
git status
git diff
```

Create a safety checkpoint/commit if appropriate before making substantial changes.

## 2. Inspect the current project first

Do not blindly apply snippets from this document.

Inspect:

```text
settings.gradle.kts
build.gradle.kts
app/build.gradle.kts
app/src/main/AndroidManifest.xml
app/src/main/java/...
app/src/main/kotlin/...
```

Find the actual package and existing Activity/navigation/ViewModel architecture.

Reuse existing architecture where possible.

## 3. Do not put secrets in the APK

Never hard-code:

- OAuth client secrets
- GitHub client secret
- LinkedIn client secret
- Firebase Admin SDK credentials
- service-account private keys
- database admin credentials

`google-services.json` is normal for Firebase Android configuration, but OAuth client secrets must remain in the provider/Firebase server-side configuration.

## 4. Do not store passwords

Drawmath must never ask for or store Google/GitHub/LinkedIn passwords.

Firebase Authentication handles provider authentication.

## 5. Do not store government identity documents

"Photo ID" in this project should mean the user's **profile photo/avatar URL** supplied by the authentication provider.

Do NOT implement storage of Aadhaar, passport, driving licence, school ID, government ID, or other identity documents unless a separate legitimate requirement is introduced later.

---

# PHASE 1 — Firebase Gradle integration

## Project-level `build.gradle.kts`

Add the Google Services Gradle plugin:

```kotlin
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20" apply false
    id("com.google.gms.google-services") version "<compatible-version>" apply false
}
```

Do not blindly use an old version. Verify the compatible Google Services plugin version against the current Gradle/AGP setup before changing it.

## App-level `app/build.gradle.kts`

Add:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
}
```

Add Firebase Authentication using the Firebase Android BoM.

Prefer the current Firebase BoM rather than hard-coding individual Firebase library versions.

Example pattern:

```kotlin
dependencies {
    implementation(platform("com.google.firebase:firebase-bom:<current-compatible-version>"))
    implementation("com.google.firebase:firebase-auth")
}
```

For Google Sign-In using the modern Credential Manager flow, also add the current compatible versions of:

```text
androidx.credentials:credentials
androidx.credentials:credentials-play-services-auth
com.google.android.libraries.identity.googleid:googleid
```

Do not downgrade existing dependencies unnecessarily.

After editing, build from Termux:

```bash
./gradlew assembleDebug
```

If Gradle wrapper permissions are missing:

```bash
chmod +x ./gradlew
./gradlew assembleDebug
```

Fix build errors based on the actual error rather than randomly changing versions.

---

# PHASE 2 — Firebase configuration

The user already registered:

```text
com.prasad.mathgrapher
```

in Firebase.

## SHA fingerprints

Run from the project root:

```bash
./gradlew signingReport
```

Record the SHA-1 and SHA-256 for the debug build.

Because debug uses:

```kotlin
applicationIdSuffix = ".debug"
```

the debug package is:

```text
com.prasad.mathgrapher.debug
```

The release package is:

```text
com.prasad.mathgrapher
```

For development, make sure the relevant SHA-1 is registered in Firebase.

If a separate release keystore exists, register its SHA-1 as well before release testing.

Do NOT ask the user for keystore passwords or private keys.

---

# PHASE 3 — Enable Google Authentication

In Firebase Console:

```text
Firebase Console
→ Authentication
→ Sign-in method
→ Google
→ Enable
→ Save
```

After enabling Google, download the updated:

```text
google-services.json
```

Replace the existing app-level file with the updated one if Firebase tells you to.

Then rebuild:

```bash
./gradlew assembleDebug
```

## Google Android implementation

Use the modern Android Credential Manager + Google ID flow recommended by Firebase.

Do not introduce the obsolete/deprecated legacy Google Sign-In implementation unless there is a project-specific compatibility reason.

High-level flow:

```text
Drawmath
   ↓
Credential Manager
   ↓
Google account
   ↓
Google ID token
   ↓
Firebase GoogleAuthProvider credential
   ↓
Firebase Authentication
   ↓
FirebaseUser
```

The server client ID used by `GetGoogleIdOption` should be the **web/server OAuth client ID**, not the Android client ID.

Firebase's generated resources from `google-services.json` should be used instead of hard-coding the client ID.

Handle both:

- authorized accounts already used by the app
- first-time account selection

If no previously authorized account exists, allow the account-selection fallback.

---

# PHASE 4 — Authentication architecture

Create a small authentication layer instead of putting Firebase calls directly throughout Compose UI.

Suggested structure, adapting to the project's existing package layout:

```text
auth/
    AuthRepository.kt
    AuthState.kt
    AuthViewModel.kt
    GoogleSignInManager.kt
```

If the project already has an appropriate ViewModel/repository architecture, integrate with it instead of creating duplicate architecture.

## Auth state

Use states similar to:

```text
Loading
SignedOut
SigningIn
SignedIn
Error
```

The UI should observe authentication state rather than directly managing Firebase callbacks everywhere.

## Firebase initialization

Use the Firebase SDK normally:

```kotlin
FirebaseAuth.getInstance()
```

or the current Kotlin Firebase API if the project already uses it.

Observe the current user:

```text
FirebaseAuth.currentUser
```

and/or an auth-state listener.

On application start:

```text
Firebase Auth
    ↓
currentUser?
    ├── null → show login
    └── user → load cloud profile
```

This means returning users do not need to fill in their profile again.

---

# PHASE 5 — Cloud user profile

Use **Cloud Firestore** for the app profile.

Add the current compatible Firebase Firestore dependency:

```kotlin
implementation("com.google.firebase:firebase-firestore")
```

using the same Firebase BoM.

Recommended document structure:

```text
users/
    {firebaseUid}/
        uid
        displayName
        email
        photoUrl
        primaryProvider
        providers
        createdAt
        lastLoginAt
```

Example conceptual document:

```json
{
  "uid": "firebase-generated-uid",
  "displayName": "Example User",
  "email": "example@example.com",
  "photoUrl": "https://...",
  "primaryProvider": "google.com",
  "providers": ["google.com"],
  "createdAt": "server timestamp",
  "lastLoginAt": "server timestamp"
}
```

Use the Firebase UID as the document ID.

Do NOT use email as the document ID.

Do NOT store OAuth access tokens in the Firestore profile document.

Do NOT store provider passwords.

## Profile creation/update

On successful sign-in:

1. Get `FirebaseUser`.
2. Read:
   - `uid`
   - `displayName`
   - `email`
   - `photoUrl`
3. Determine provider(s).
4. Create/update:

```text
users/{uid}
```

Use a server timestamp for cloud timestamps.

For the first login:

```text
createdAt = server timestamp
```

For every login:

```text
lastLoginAt = server timestamp
```

Do not overwrite an existing `createdAt`.

Use merge semantics so future fields can be added safely.

---

# PHASE 6 — Firestore security rules

Do NOT leave Firestore in test/open mode.

The fundamental rule should be:

```text
A signed-in user can read/write only their own users/{uid} document.
```

Conceptual rule:

```text
match /users/{userId} {
    allow read, write: if request.auth != null
                       && request.auth.uid == userId;
}
```

However, the coding agent must inspect the final document structure and write complete valid Firestore rules.

If saved graphs are later stored under the user, use the same UID ownership principle.

Never create rules such as:

```text
allow read, write: if true;
```

Never ship open database rules.

---

# PHASE 7 — Profile photo

For Google/GitHub/LinkedIn provider profiles, use the provider/Firebase profile photo URL where available.

Display:

```text
FirebaseUser.photoUrl
```

and store the URL in the user's Firestore profile.

Do not download/re-upload provider photos to Firebase Storage unless there is a specific product reason.

If a future feature lets the user upload a custom avatar, handle that separately with Firebase Storage and Storage Security Rules.

---

# PHASE 8 — GitHub Login

Firebase supports GitHub authentication through OAuth.

Firebase Console:

```text
Authentication
→ Sign-in method
→ GitHub
→ Enable
```

Create/register the OAuth app in GitHub's developer settings.

Firebase provides the authorization callback URL. Use the exact callback URL shown by Firebase; do not invent one.

Configure:

```text
Client ID
Client Secret
Authorization callback URL
```

The GitHub client secret stays in Firebase/provider configuration and must never be placed in the Android source.

Android flow should use Firebase's Android OAuth provider flow:

```kotlin
val provider = OAuthProvider.newBuilder("github.com")
```

and the Firebase Android sign-in flow.

Handle pending OAuth results because the Android activity can be backgrounded/recreated during browser authentication.

Also support linking GitHub to an existing Firebase account rather than automatically creating a separate account when the user is already signed in.

---

# PHASE 9 — LinkedIn Login

Do NOT assume LinkedIn can be configured exactly like GitHub.

First verify the current LinkedIn developer authentication capabilities and Firebase support.

If LinkedIn provides an OIDC-compatible configuration that works with the Firebase project's authentication setup, use Firebase's supported OIDC provider configuration.

If the required Firebase OIDC capability is unavailable on the current Firebase plan/configuration, do not invent a workaround.

Instead report the exact limitation and stop LinkedIn implementation until the required supported configuration is available.

For OIDC, the server-side configuration may require:

```text
Issuer
Client ID
Client Secret
```

and Firebase's OIDC provider configuration.

The client secret must remain server-side.

Use the authorization-code flow when supported; do not use a weaker implicit flow unnecessarily.

---

# PHASE 10 — Link multiple login providers to one account

This is important.

A user might:

1. Sign up with Google.
2. Later choose "Link GitHub".
3. Later link LinkedIn.

The result should be:

```text
Firebase UID: abc123
    ├── Google
    ├── GitHub
    └── LinkedIn
```

NOT:

```text
Google → UID A
GitHub → UID B
LinkedIn → UID C
```

When a user is already signed in, use Firebase's provider-linking APIs.

If a provider is already linked, show an appropriate message.

If Firebase returns an "account exists with different credential" error, do not silently create another account.

Provide a safe account-linking flow.

---

# PHASE 11 — Login UI

Add authentication UI without damaging the existing Drawmath graphing interface.

Suggested screen:

```text
             Drawmath

       [ Continue with Google ]

       [ Continue with GitHub ]

       [ Continue with LinkedIn ]

       -----------------------

       Continue as guest
```

Only show providers that are actually configured and working.

Use official provider branding according to the provider's current brand requirements.

Do not create fake provider logos.

After login:

```text
Home / Graph Screen
```

Profile screen can show:

```text
[Profile photo]

Display Name
Email

Signed in with:
Google / GitHub / LinkedIn

[Manage linked accounts]
[Sign out]
```

Do not expose OAuth tokens.

---

# PHASE 12 — Automatic login restoration

On app launch:

```text
FirebaseAuth.currentUser
```

If a Firebase user exists:

```text
Signed in
    ↓
Load users/{uid}
    ↓
Display profile
```

If no Firebase user exists:

```text
Signed out
    ↓
Show login screen
```

Firebase Authentication persists the authenticated session so the user does not need to sign in every time.

If the Firestore profile document is missing even though authentication succeeds, recreate/update it safely from the authenticated Firebase user.

---

# PHASE 13 — Error handling

Handle at least:

- User cancels login
- No Google account available
- Network unavailable
- Firebase unavailable
- OAuth provider failure
- Invalid configuration
- Provider already linked
- Account exists with another credential
- Activity recreation during OAuth
- Firestore read/write failure
- Sign-out failure
- Corrupt/missing profile document

Do not show raw exception stack traces to normal users.

Use friendly messages.

For example:

```text
"Sign-in was cancelled."
"Couldn't connect. Check your internet connection and try again."
"Unable to complete sign-in. Please try again."
```

Keep detailed exception information in debug logs only.

---

# PHASE 14 — Testing from Termux

All testing must work without Android Studio.

Run:

```bash
./gradlew test
```

Then:

```bash
./gradlew assembleDebug
```

Install the debug APK using the user's existing Android/Termux workflow.

Test:

### Google

1. Fresh install.
2. Sign in with Google.
3. Verify Firebase Authentication user exists.
4. Verify Firestore `users/{uid}` exists.
5. Verify name/email/photo.
6. Force-close app.
7. Reopen.
8. Confirm user is still signed in.
9. Sign out.
10. Sign in again.
11. Confirm same Firebase UID/profile.

### GitHub

Repeat the same tests.

### LinkedIn

Test only after the provider configuration is actually completed.

### Provider linking

1. Sign in with Google.
2. Link GitHub.
3. Sign out.
4. Sign in using GitHub.
5. Confirm the same Firebase UID is used.

Do not accept the test as successful if two providers create different accounts when linking was intended.

---

# PHASE 15 — Release configuration

Before release:

```text
Debug:
com.prasad.mathgrapher.debug

Release:
com.prasad.mathgrapher
```

Register the correct release SHA-1/SHA-256 in Firebase.

Make sure the release `google-services.json` contains the appropriate OAuth configuration.

Build:

```bash
./gradlew assembleRelease
```

If Play App Signing is used, also account for the Play App Signing certificate fingerprint in Firebase.

Never commit:

```text
*.jks
*.keystore
keystore passwords
service account JSON files
OAuth client secrets
```

Check:

```bash
git status
git diff
```

before committing.

---

# PHASE 16 — Security checklist

The implementation is NOT complete until all of these are true:

- [ ] Firebase Authentication enabled
- [ ] Google enabled
- [ ] GitHub enabled/configured
- [ ] LinkedIn only implemented through a supported secure flow
- [ ] SHA-1 registered for debug
- [ ] SHA-1/SHA-256 registered for release where applicable
- [ ] `google-services.json` correctly placed
- [ ] Firebase Auth dependency added
- [ ] Firestore dependency added
- [ ] Credential Manager dependencies added for Google
- [ ] Firebase UID used as profile document ID
- [ ] Firestore rules restrict users to their own profile
- [ ] No open Firestore rules
- [ ] No passwords stored
- [ ] No OAuth client secrets in APK/source
- [ ] No OAuth tokens stored in Firestore profile
- [ ] Provider linking handled
- [ ] Returning users automatically restored
- [ ] Sign-out works
- [ ] Account-linking conflicts handled
- [ ] Network/auth errors handled
- [ ] Existing graphing functionality remains unchanged
- [ ] Debug build succeeds
- [ ] Release build succeeds

---

# PHASE 17 — Firebase App Check (recommended after basic auth works)

After authentication and Firestore are working, consider enabling Firebase App Check for abuse protection.

Do not add App Check before the basic authentication flow is stable unless the current Firebase configuration makes it necessary.

The coding agent should use the current Firebase Android documentation and choose the appropriate App Check provider for the app's deployment/testing environment.

During development, avoid locking yourself out by enforcing App Check before debug configuration is properly registered.

---

# PHASE 18 — What the coding agent must NOT do

Do NOT:

- Rewrite Drawmath's graphing engine.
- Change equation parsing behavior.
- Replace Compose architecture unnecessarily.
- Move all graph calculations to a server.
- Add a custom backend server just for authentication.
- Store Google/GitHub/LinkedIn passwords.
- Store OAuth client secrets in Kotlin.
- Store Firebase Admin credentials in the Android app.
- Make Firestore publicly readable/writable.
- Use email addresses as Firestore document IDs.
- Automatically create duplicate accounts when provider credentials differ.
- Upload provider profile photos to Storage unnecessarily.
- Store government IDs.
- Add unrelated cloud services.
- Remove existing Gradle dependencies without checking usage.

---

# Definition of Done

The feature is complete when:

```text
User
  ↓
Drawmath
  ↓
Google / GitHub / LinkedIn
  ↓
Firebase Authentication
  ↓
Stable Firebase UID
  ↓
Firestore users/{uid}
  ↓
Name + email + profile photo + provider information
```

and on a later launch:

```text
Drawmath
  ↓
FirebaseAuth.currentUser
  ↓
same UID
  ↓
same Firestore profile
  ↓
profile restored automatically
```

The existing Drawmath graphing functionality must continue working exactly as before.

---

# Useful current official references

Use the official Firebase documentation when implementation details differ from this plan:

- Firebase Android Authentication:
  https://firebase.google.com/docs/auth/android/start
- Firebase Google Sign-In on Android:
  https://firebase.google.com/docs/auth/android/google-signin
- Firebase GitHub authentication on Android:
  https://firebase.google.com/docs/auth/android/github-auth
- Firebase OpenID Connect:
  https://firebase.google.com/docs/auth/web/openid-connect

For LinkedIn, use LinkedIn's current official OAuth documentation rather than relying on old tutorials.

---

# Final instruction to the coding agent

Work incrementally.

After each major phase:

1. Run the relevant Gradle build/test.
2. Inspect the error if one occurs.
3. Fix only the cause.
4. Show a concise summary of changed files.
5. Do not claim Firebase Console configuration is complete unless it was actually configured.
6. Do not claim Google/GitHub/LinkedIn login works until it has been tested on the installed Android debug build.
7. Keep the user informed about any manual Firebase/GitHub/LinkedIn console steps that cannot be performed from the local project.

Start by inspecting the current project and Git state, then implement **Google Authentication first**. Do not implement GitHub or LinkedIn until Google login and cloud profile persistence are working.
