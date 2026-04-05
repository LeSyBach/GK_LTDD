package com.example.gk_ltdd.repository

import android.net.Uri
import com.example.gk_ltdd.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val usersCollection = db.collection("users")
    private val authRepository = AuthRepository()

    fun getUsersFlow(): Flow<List<User>> = callbackFlow {
        val listener = usersCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val users = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(User::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(users)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addUser(user: User): Result<String> {
        return try {
            val docRef = usersCollection.add(user).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            usersCollection.document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserById(userId: String): Result<User?> {
        return try {
            val doc = usersCollection.document(userId).get().await()
            val user = doc.toObject(User::class.java)?.copy(id = doc.id)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadFile(uri: Uri, path: String): Result<String> {
        return try {
            val ref = storage.reference.child(path)
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createUserWithAuth(email: String, password: String, user: User): Result<String> {
        return try {
            // Tạo account Firebase Auth
            val authResult = authRepository.createUserAccount(email, password)
            if (authResult.isSuccess) {
                val uid = authResult.getOrNull() ?: ""
                // Lưu dữ liệu user vào Firestore với uid làm document ID
                val userWithId = user.copy(id = uid)
                usersCollection.document(uid).set(userWithId).await()
                Result.success(uid)
            } else {
                Result.failure(authResult.exceptionOrNull() ?: Exception("Tạo tài khoản thất bại"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}