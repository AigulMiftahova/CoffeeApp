package com.coffeeshopmanager.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AdminRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getAdmins(): List<User> {
        val snapshot = db.collection("users").whereEqualTo("role", "ADMIN").get().await()
        return snapshot.documents.mapNotNull { it.toObject(User::class.java)?.copy(id = it.id) }
    }

    suspend fun addAdminByEmail(email: String) {
        val users = db.collection("users").whereEqualTo("email", email).get().await()
        if (users.isEmpty) throw Exception("Пользователь не найден")
        val user = users.documents.first()
        db.collection("users").document(user.id).update("role", "ADMIN").await()
    }

    suspend fun removeAdmin(userId: String) {
        db.collection("users").document(userId).update("role", "USER").await()
    }
} 