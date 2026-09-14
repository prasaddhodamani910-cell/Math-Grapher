package com.prasad.mathgrapher.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

private const val WEB_CLIENT_ID = "405263536624-qdkb4ckabcrl8hmrifet00tctu2e49hc.apps.googleusercontent.com"

data class GoogleUser(val name: String?, val email: String?, val photoUrl: String?, val uid: String)

suspend fun signInWithGoogle(context: Context): Result<GoogleUser> {
    val credentialManager = CredentialManager.create(context)
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(WEB_CLIENT_ID)
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    return try {
        val result = credentialManager.getCredential(
            request = request,
            context = context
        )
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            val authResult = FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).await()
            val user = authResult.user
            if (user != null) {
                Result.success(GoogleUser(user.displayName, user.email, user.photoUrl?.toString(), user.uid))
            } else {
                Result.failure(Exception("Firebase auth returned null user"))
            }
        } else {
            Result.failure(Exception("Unexpected credential type: ${credential.type}"))
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}
