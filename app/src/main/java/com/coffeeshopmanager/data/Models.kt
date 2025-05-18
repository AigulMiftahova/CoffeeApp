package com.coffeeshopmanager.data

data class MenuItem(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val cost: Double = 0.0, // Себестоимость
    val description: String = "",
    val imageUrl: String = ""
)

data class Employee(
    val id: String = "",
    val name: String = "",
    val position: String = "",
    val salaryPerDay: Double = 0.0
)

data class Sale(
    val id: String = "",
    val date: Long = 0L,
    val items: List<SaleItem> = emptyList(),
    val total: Double = 0.0
)

data class SaleItem(
    val menuItemId: String = "",
    val quantity: Int = 0
)

data class Order(
    val id: String = "",
    val userId: String = "",
    val date: Long = 0L,
    val items: List<OrderItem> = emptyList(),
    val total: Double = 0.0
)

data class OrderItem(
    val menuItemId: String = "",
    val quantity: Int = 0
)

enum class UserRole { ADMIN, USER, OWNER }

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.USER
) 