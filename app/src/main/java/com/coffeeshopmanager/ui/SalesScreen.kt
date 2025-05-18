package com.coffeeshopmanager.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coffeeshopmanager.data.Sale
import com.coffeeshopmanager.data.SalesRepository
import com.coffeeshopmanager.data.MenuRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import android.app.DatePickerDialog
import java.util.Calendar
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.clickable

@Composable
fun SalesScreen() {
    val repo = remember { SalesRepository() }
    val menuRepo = remember { MenuRepository() }
    var sales by remember { mutableStateOf(listOf<Sale>()) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var date by remember { mutableStateOf(Date()) }
    var isLoading by remember { mutableStateOf(true) }
    val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val showDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date)
    var selectedSale by remember { mutableStateOf<Sale?>(null) }
    var menuItems by remember { mutableStateOf(mapOf<String, String>()) }
    
    // Контекст для DatePickerDialog
    val context = LocalContext.current

    fun loadSales() {
        isLoading = true
        scope.launch {
            try {
                sales = repo.getSales().sortedByDescending { it.date }
                // Загрузим информацию о товарах для отображения названий
                val menu = menuRepo.getMenu()
                menuItems = menu.associate { it.id to it.name }
                error = null
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(date) { loadSales() }

    val filteredSales = sales.filter {
        dateFormat.format(Date(it.date)) == dateFormat.format(date)
    }
    val totalToday = filteredSales.sumOf { it.total }
    val countToday = filteredSales.size

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            "Продажи",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        DatePickerCard(
            currentDate = date,
            displayDate = showDate,
            onSelectDate = {
                // Открыть выбор даты
                val calendar = Calendar.getInstance()
                calendar.time = date
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val newCalendar = Calendar.getInstance()
                        newCalendar.set(year, month, dayOfMonth)
                        date = newCalendar.time
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        )
        
        Spacer(Modifier.height(16.dp))
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Text(text = error!!, color = MaterialTheme.colorScheme.error)
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Выручка за день: ${totalToday}₽",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "Чеков за день: $countToday",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredSales) { sale ->
                    SaleCard(
                        sale = sale,
                        onClick = { selectedSale = sale }
                    )
                }
            }
        }
    }
    
    // Диалог с деталями продажи
    selectedSale?.let { sale ->
        SaleDetailsDialog(
            sale = sale,
            menuItems = menuItems,
            onDismiss = { selectedSale = null }
        )
    }
}

@Composable
private fun SaleDetailsDialog(
    sale: Sale,
    menuItems: Map<String, String>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Детали заказа",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Дата: " + SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                        .format(Date(sale.date)),
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Text(
                    "Сумма: ${sale.total}₽",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Позиции:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Список позиций заказа
                Column {
                    sale.items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                menuItems[item.menuItemId] ?: "Неизвестный товар",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Text(
                                "${item.quantity} шт.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Закрыть")
                }
            }
        }
    }
}

@Composable
private fun SaleCard(
    sale: Sale,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = MaterialTheme.shapes.medium)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Чек #${sale.id}", style = MaterialTheme.typography.titleMedium)
            Text("Дата: " + SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(sale.date)), style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Позиций: ${sale.items.size}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Сумма: ${sale.total}₽", 
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun DatePickerCard(
    currentDate: Date,
    displayDate: String,
    onSelectDate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Выберите дату",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedButton(
                onClick = onSelectDate,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "Выбрать дату",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(displayDate)
            }
        }
    }
} 