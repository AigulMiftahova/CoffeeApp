package com.coffeeshopmanager.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coffeeshopmanager.data.CartRepository
import com.coffeeshopmanager.data.Order
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.DialogProperties
import android.app.DatePickerDialog
import java.util.Calendar

@Composable
fun OrderHistoryScreen(userId: String = "test") {
    val repo = remember { CartRepository() }
    var orders by remember { mutableStateOf(listOf<Order>()) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var sortAsc by remember { mutableStateOf(false) }
    
    // Устанавливаем даты как миллисекунды для упрощения
    var dateFromMillis by remember { mutableStateOf(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000) }
    var dateToMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    
    val dateFrom = Date(dateFromMillis)
    val dateTo = Date(dateToMillis)
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val showFrom = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(dateFrom)
    val showTo = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(dateTo)
    
    // Контекст для DatePickerDialog
    val context = LocalContext.current

    LaunchedEffect(userId) {
        isLoading = true
        try {
            orders = repo.getOrders(userId).sortedByDescending { it.date }
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    val filtered = orders.filter {
        it.date in dateFrom.time..dateTo.time
    }
    val sorted = if (sortAsc) filtered.sortedBy { it.total } else filtered.sortedByDescending { it.total }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            "История заказов",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        DateRangeFilter(
            dateFrom = showFrom,
            dateTo = showTo,
            onSelectDateFrom = { 
                // Открыть выбор начальной даты
                val calendar = Calendar.getInstance()
                calendar.time = dateFrom
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val newCalendar = Calendar.getInstance()
                        newCalendar.set(year, month, dayOfMonth)
                        dateFromMillis = newCalendar.timeInMillis
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            onSelectDateTo = { 
                // Открыть выбор конечной даты
                val calendar = Calendar.getInstance()
                calendar.time = dateTo
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val newCalendar = Calendar.getInstance()
                        newCalendar.set(year, month, dayOfMonth)
                        dateToMillis = newCalendar.timeInMillis
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            onSortToggle = { sortAsc = !sortAsc },
            isSortAscending = sortAsc
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            ErrorView(errorMessage = error!!)
        } else if (sorted.isEmpty()) {
            EmptyHistoryView()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sorted) { order ->
                    OrderCard(order = order)
                }
            }
        }
    }
}

@Composable
private fun DateRangeFilter(
    dateFrom: String,
    dateTo: String,
    onSelectDateFrom: () -> Unit,
    onSelectDateTo: () -> Unit,
    onSortToggle: () -> Unit,
    isSortAscending: Boolean
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
                "Фильтр по дате",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "С: ",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                OutlinedButton(
                    onClick = onSelectDateFrom,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Выбрать дату",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(dateFrom)
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "До: ",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                OutlinedButton(
                    onClick = onSelectDateTo,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Выбрать дату",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(dateTo)
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Сортировка по сумме:",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                FilterChip(
                    onClick = onSortToggle,
                    label = { 
                        Text(if (isSortAscending) "По возрастанию" else "По убыванию") 
                    },
                    selected = true,
                    leadingIcon = {
                        Icon(
                            if (isSortAscending) Icons.Default.ArrowForward else Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun OrderCard(order: Order) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Заказ #${order.id.takeLast(6).uppercase()}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "${order.total}₽",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                        .format(Date(order.date)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Позиций: ${order.items.size}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.List,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Нет заказов",
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "За выбранный период заказов не найдено",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorView(errorMessage: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Ошибка",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
} 