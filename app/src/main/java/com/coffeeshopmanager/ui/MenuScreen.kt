package com.coffeeshopmanager.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coffeeshopmanager.data.MenuItem
import com.coffeeshopmanager.data.MenuRepository
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.RoundedCornerShape
import com.coffeeshopmanager.data.LocalCart

@Composable
fun MenuScreen(userRole: String = "USER", showAddToCartButton: Boolean = true) {
    val menuRepository = remember { MenuRepository() }
    var menu by remember { mutableStateOf(listOf<MenuItem>()) }
    var showDialog by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<MenuItem?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val cartState = LocalCart.current

    LaunchedEffect(Unit) {
        try {
            menu = menuRepository.getMenu()
        } catch (e: Exception) {
            error = e.message
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Меню",
                style = MaterialTheme.typography.headlineLarge
            )
            
            if (userRole == "ADMIN" || userRole == "OWNER") {
                FilledTonalButton(
                    onClick = { editItem = null; showDialog = true },
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Добавить",
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Добавить")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
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
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(menu) { item ->
                MenuItemCard(
                    menuItem = item, 
                    userRole = userRole,
                    showAddToCartButton = showAddToCartButton,
                    onEdit = { editItem = item; showDialog = true },
                    onDelete = {
                        scope.launch {
                            try {
                                menuRepository.deleteMenuItem(item.id)
                                menu = menuRepository.getMenu()
                            } catch (e: Exception) {
                                error = e.message
                            }
                        }
                    },
                    onAddToCart = {
                        val current = cartState.value.toMutableList()
                        val idx = current.indexOfFirst { it.first.id == item.id }
                        if (idx >= 0) {
                            current[idx] = current[idx].copy(second = current[idx].second + 1)
                        } else {
                            current.add(item to 1)
                        }
                        cartState.value = current
                    }
                )
            }
        }
    }

    if (showDialog) {
        MenuEditDialog(
            initial = editItem,
            onDismiss = { showDialog = false },
            onSave = { item ->
                scope.launch {
                    try {
                        if (item.id.isEmpty()) {
                            menuRepository.addMenuItem(item)
                        } else {
                            menuRepository.updateMenuItem(item)
                        }
                        menu = menuRepository.getMenu()
                        showDialog = false
                    } catch (e: Exception) {
                        error = e.message
                    }
                }
            }
        )
    }
}

@Composable
private fun MenuItemCard(
    menuItem: MenuItem,
    userRole: String,
    showAddToCartButton: Boolean = true,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddToCart: () -> Unit
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        menuItem.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${menuItem.price}₽",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (showAddToCartButton) {
                    FilledIconButton(
                        onClick = { 
                            onAddToCart() 
                            // Снэкбар будет показан в onAddToCart
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = "В корзину"
                        )
                    }
                }
            }
            
            if (menuItem.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    menuItem.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            if (userRole == "ADMIN" || userRole == "OWNER") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedIconButton(
                        onClick = onEdit,
                        modifier = Modifier.padding(end = 8.dp),
                        colors = IconButtonDefaults.outlinedIconButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                    }
                    
                    OutlinedIconButton(
                        onClick = onDelete,
                        colors = IconButtonDefaults.outlinedIconButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить")
                    }
                }
            }
        }
    }
}

@Composable
fun MenuEditDialog(initial: MenuItem?, onDismiss: () -> Unit, onSave: (MenuItem) -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var price by remember { mutableStateOf(initial?.price?.toString() ?: "") }
    var cost by remember { mutableStateOf(initial?.cost?.toString() ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                if (initial == null) "Добавить позицию" else "Редактировать позицию",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Цена") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = cost,
                    onValueChange = { cost = it },
                    label = { Text("Себестоимость") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val item = MenuItem(
                        id = initial?.id ?: "",
                        name = name,
                        price = price.toDoubleOrNull() ?: 0.0,
                        cost = cost.toDoubleOrNull() ?: 0.0,
                        description = description
                    )
                    onSave(item)
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
} 