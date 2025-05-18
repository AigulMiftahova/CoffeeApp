package com.coffeeshopmanager.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CartRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun addOrder(order: Order) {
        db.collection("orders").add(order).await()
    }

    suspend fun getOrders(userId: String): List<Order> {
        val snapshot = db.collection("orders").whereEqualTo("userId", userId).get().await()
        return snapshot.documents.mapNotNull { it.toObject(Order::class.java)?.copy(id = it.id) }
    }
} 