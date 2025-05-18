package com.coffeeshopmanager.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MenuRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getMenu(): List<MenuItem> {
        val snapshot = db.collection("menu").get().await()
        return snapshot.documents.mapNotNull { it.toObject(MenuItem::class.java)?.copy(id = it.id) }
    }

    suspend fun addMenuItem(item: MenuItem) {
        db.collection("menu").add(item).await()
    }

    suspend fun updateMenuItem(item: MenuItem) {
        db.collection("menu").document(item.id).set(item).await()
    }

    suspend fun deleteMenuItem(itemId: String) {
        db.collection("menu").document(itemId).delete().await()
    }
} 