package com.prasad.mathgrapher.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.Date

object AuthRepository {
    private val firestore = FirebaseFirestore.getInstance()
    
    suspend fun saveUserProfile(user: GoogleUser) {
        val userRef = firestore.collection("users").document(user.uid)
        
        val data = hashMapOf(
            "uid" to user.uid,
            "displayName" to user.name,
            "email" to user.email,
            "photoUrl" to user.photoUrl,
            "lastLoginAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )
        
        // Use SetOptions.merge() so we don't overwrite createdAt if it exists
        userRef.set(data, SetOptions.merge()).await()
        
        // Only set createdAt if the document was just created
        val doc = userRef.get().await()
        if (!doc.contains("createdAt")) {
            userRef.set(hashMapOf("createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()), SetOptions.merge()).await()
        }
    }
}
