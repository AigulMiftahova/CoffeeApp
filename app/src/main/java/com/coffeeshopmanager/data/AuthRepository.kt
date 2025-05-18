package com.coffeeshopmanager.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun signIn(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user?.uid ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserRole(userId: String): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val doc = db.collection("users").document(userId).get().await()
            val role = doc.getString("role") ?: "USER"
            Result.success(role)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserName(userId: String): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val doc = db.collection("users").document(userId).get().await()
            val name = doc.getString("name") ?: "Пользователь"
            Result.success(name)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createUserIfNotExists(userId: String, email: String, name: String = "Пользователь") = withContext(Dispatchers.IO) {
        val userRef = db.collection("users").document(userId)
        val doc = userRef.get().await()
        if (!doc.exists()) {
            userRef.set(mapOf(
                "email" to email,
                "name" to name,
                "role" to "USER"
            )).await()
        }
    }

    suspend fun register(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user?.uid ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 