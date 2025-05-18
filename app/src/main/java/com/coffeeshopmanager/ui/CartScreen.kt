package com.coffeeshopmanager.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coffeeshopmanager.data.MenuItem
import com.coffeeshopmanager.data.Order
import com.coffeeshopmanager.data.OrderItem
import com.coffeeshopmanager.data.CartRepository
import com.coffeeshopmanager.data.Sale
import com.coffeeshopmanager.data.SaleItem
import com.coffeeshopmanager.data.SalesRepository
import kotlinx.coroutines.launch
import java.util.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.coffeeshopmanager.data.LocalCart

@Composable
fun CartScreen(userId: String = "test") {
    val cartState = LocalCart.current
    var cart by remember { cartState }
    var error by remember { mutableStateOf<String?>(null) }
    val repo = remember { CartRepository() }
    val salesRepo = remember { SalesRepository() }
    val scope = rememberCoroutineScope()
    val total = cart.sumOf { it.first.price * it.second }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Заголовок
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Корзина",
                style = MaterialTheme.typography.headlineLarge
            )
            
            if (cart.isNotEmpty()) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(cart.size.toString())
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Содержимое корзины
        if (cart.isEmpty()) {
            EmptyCartView()
        } else {
            // Список товаров в корзине
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(cart) { (item, qty) ->
                    CartItemCard(
                        menuItem = item,
                        quantity = qty,
                        onRemove = { cart = cart.filter { it.first.id != item.id } },
                        onDecrease = {
                            val current = cart.toMutableList()
                            val index = current.indexOfFirst { it.first.id == item.id }
                            if (index >= 0) {
                                val (menuItem, count) = current[index]
                                if (count > 1) {
                                    current[index] = menuItem to (count - 1)
                                    cart = current
                                } else {
                                    cart = current.filter { it.first.id != item.id }
                                }
                            }
                        },
                        onIncrease = {
                            val current = cart.toMutableList()
                            val index = current.indexOfFirst { it.first.id == item.id }
                            if (index >= 0) {
                                val (menuItem, count) = current[index]
                                current[index] = menuItem to (count + 1)
                                cart = current
                            }
                        }
                    )
                }
            }
            
            if (error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            // Итоговая сумма
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Итого:",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${total}₽",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Кнопки действий
            Button(
                onClick = {
                    scope.launch {
                        try {
                            val order = Order(
                                userId = userId,
                                date = System.currentTimeMillis(),
                                items = cart.map { 
                                    OrderItem(
                                        menuItemId = it.first.id,
                                        quantity = it.second
                                    )
                                },
                                total = total
                            )
                            
                            repo.addOrder(order)
                            
                            val sale = Sale(
                                date = System.currentTimeMillis(),
                                items = cart.map { 
                                    SaleItem(
                                        menuItemId = it.first.id,
                                        quantity = it.second
                                    )
                                },
                                total = total
                            )
                            
                            salesRepo.addSale(sale)
                            
                            cart = emptyList()
                            error = null
                        } catch (e: Exception) {
                            error = e.message
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Оформить заказ на ${total}₽")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedButton(
                onClick = { cart = emptyList() },
                enabled = cart.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(bottom = 4.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Очистить корзину")
            }
        }
    }
}

@Composable
private fun EmptyCartView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Корзина пуста",
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Добавьте товары из меню",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CartItemCard(
    menuItem: MenuItem,
    quantity: Int,
    onRemove: () -> Unit,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        menuItem.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${menuItem.price}₽",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    "${menuItem.price * quantity}₽",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedIconButton(
                    onClick = onRemove,
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить"
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDecrease
                    ) {
                        Text(
                            "-",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Text(
                        quantity.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    IconButton(
                        onClick = onIncrease
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Увеличить"
                        )
                    }
                }
            }
        }
    }
} 