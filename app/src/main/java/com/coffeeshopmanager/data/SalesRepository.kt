package com.coffeeshopmanager.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SalesRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getSales(): List<Sale> {
        val snapshot = db.collection("sales").get().await()
        return snapshot.documents.mapNotNull { it.toObject(Sale::class.java)?.copy(id = it.id) }
    }

    suspend fun addSale(sale: Sale) {
        db.collection("sales").add(sale).await()
    }
} 