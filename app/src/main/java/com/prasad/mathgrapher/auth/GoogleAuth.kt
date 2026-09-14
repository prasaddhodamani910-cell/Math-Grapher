package com.prasad.mathgrapher.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.prasad.mathgrapher.R
import kotlinx.coroutines.tasks.await

private const val WEB_CLIENT_ID = "405263536624-qdkb4ckabcrl8hmrifet00tctu2e49hc.apps.googleusercontent.com"

data class GoogleUser(val name: String?, val email: String?, val photoUrl: String?, val uid: String)

suspend fun signInWithGoogle(context: Context): GoogleUser? {
    val credentialManager = CredentialManager.create(context)
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
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
            
            // Link with Firebase Authentication
            val firebaseCredential = GoogleAuthProvider.getCredential(tokenCredential.idToken, null)
            val authResult = FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).await()
            val user = authResult.user
            
            if (user != null) {
                GoogleUser(
                    name = user.displayName,
                    email = user.email,
                    photoUrl = user.photoUrl?.toString(),
                    uid = user.uid
                )
            } else null
        } else null
    } catch (e: Exception) {
        null 
    }
}
