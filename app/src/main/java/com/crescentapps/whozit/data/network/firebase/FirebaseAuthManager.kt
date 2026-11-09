package com.crescentapps.whozit.data.network.firebase

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager {
    val isFirebaseConfigured: Boolean
        get() = try {
            FirebaseApp.getInstance() != null
        } catch (e: IllegalStateException) {
            false
        }

    val currentUid: String?
        get() = if (isFirebaseConfigured) FirebaseAuth.getInstance().currentUser?.uid else null

    suspend fun authenticateAnonymously(): Result<String> {
        if (!isFirebaseConfigured) {
            return Result.failure(IllegalStateException("Firebase is not configured (missing google-services.json)."))
        }

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            return Result.success(currentUser.uid)
        }

        return try {
            val result = auth.signInAnonymously().await()
            val uid = result.user?.uid ?: throw IllegalStateException("No user returned from anonymous sign in.")
            Result.success(uid)
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Error authenticating anonymously", e)
            Result.failure(e)
        }
    }
}
