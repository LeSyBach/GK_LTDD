package com.example.gk_ltdd.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveUserData(uid: String, username: String, email: String, role: String): Result<Unit> {
        return try {
            val userData = mapOf(
                "username" to username,
                "email" to email,
                "role" to role,
                "password" to "",
                "imageUrl" to "",
                "fileUrl" to "",
                "fileName" to ""
            )
            db.collection("users").document(uid).set(userData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun getUserRole(uid: String): String {
        return try {
            val doc = db.collection("users").document(uid).get().await()
            doc.getString("role") ?: "user"
        } catch (_: Exception) {
            "user"
        }
    }

    suspend fun isAdmin(uid: String): Boolean {
        return getUserRole(uid) == "admin"
    }

    suspend fun createUserAccount(email: String, password: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user?.uid ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}