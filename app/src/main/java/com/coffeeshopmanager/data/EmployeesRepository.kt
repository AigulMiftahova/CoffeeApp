package com.coffeeshopmanager.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class EmployeesRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getEmployees(): List<Employee> {
        val snapshot = db.collection("employees").get().await()
        return snapshot.documents.mapNotNull { it.toObject(Employee::class.java)?.copy(id = it.id) }
    }

    suspend fun addEmployee(employee: Employee) {
        db.collection("employees").add(employee).await()
    }

    suspend fun updateEmployee(employee: Employee) {
        db.collection("employees").document(employee.id).set(employee).await()
    }

    suspend fun deleteEmployee(employeeId: String) {
        db.collection("employees").document(employeeId).delete().await()
    }
} 