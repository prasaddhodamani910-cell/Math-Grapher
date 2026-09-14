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
        
        userRef.set(data, SetOptions.merge()).await()
        
        val doc = userRef.get().await()
        if (!doc.contains("createdAt")) {
            userRef.set(hashMapOf("createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()), SetOptions.merge()).await()
        }
    }
    
    suspend fun getUserProfile(uid: String): Map<String, Any>? {
        return firestore.collection("users").document(uid).get().await().data
    }
    
    suspend fun updateProfile(uid: String, customName: String?, customPhotoUrl: String?) {
        val updates = mutableMapOf<String, Any>()
        if (customName != null) updates["customDisplayName"] = customName
        if (customPhotoUrl != null) updates["customPhotoUrl"] = customPhotoUrl
        firestore.collection("users").document(uid).set(updates, SetOptions.merge()).await()
    }
}
