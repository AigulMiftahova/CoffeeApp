package com.coffeeshopmanager.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coffeeshopmanager.data.SalesRepository
import com.coffeeshopmanager.data.MenuRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.platform.LocalContext
import android.app.DatePickerDialog
import java.util.Calendar

@Composable
fun StatisticsScreen() {
    val salesRepo = remember { SalesRepository() }
    val menuRepo = remember { MenuRepository() }
    var stats by remember { mutableStateOf(listOf<Pair<String, Int>>()) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var date by remember { mutableStateOf(Date()) }
    var isLoading by remember { mutableStateOf(true) }
    val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val showDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date)

    fun loadStats() {
        isLoading = true
        scope.launch {
            try {
                val sales = salesRepo.getSales()
                val menu = menuRepo.getMenu()
                val todaySales = sales.filter {
                    dateFormat.format(Date(it.date)) == dateFormat.format(date)
                }
                val counts = mutableMapOf<String, Int>()
                todaySales.forEach { sale ->
                    sale.items.forEach { item ->
                        counts[item.menuItemId] = counts.getOrDefault(item.menuItemId, 0) + item.quantity
                    }
                }
                val result = menu.map { it.name to (counts[it.id] ?: 0) }
                    .sortedByDescending { it.second }
                stats = result
                error = null
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(date) { loadStats() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            "Статистика продаж",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        DateFilterCard(
            currentDate = date,
            displayDate = showDate,
            onDateChange = { newDate -> date = newDate }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            LoadingIndicator()
        } else if (error != null) {
            ErrorView(errorMessage = error!!)
        } else if (stats.isEmpty()) {
            EmptyStatsView()
        } else {
            StatsList(stats = stats)
        }
    }
}

@Composable
private fun DateFilterCard(
    currentDate: Date,
    displayDate: String,
    onDateChange: (Date) -> Unit
) {
    val context = LocalContext.current
    
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
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onDateChange(Date(currentDate.time - 24 * 60 * 60 * 1000)) },
                    modifier = Modifier.weight(0.3f)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Предыдущий день"
                    )
                }
                
                OutlinedButton(
                    onClick = {
                        // Открыть календарь
                        val calendar = Calendar.getInstance()
                        calendar.time = currentDate
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val newCalendar = Calendar.getInstance()
                                newCalendar.set(year, month, dayOfMonth)
                                onDateChange(newCalendar.time)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Выбрать дату",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(displayDate)
                }
                
                OutlinedButton(
                    onClick = { onDateChange(Date(currentDate.time + 24 * 60 * 60 * 1000)) },
                    modifier = Modifier.weight(0.3f)
                ) {
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = "Следующий день"
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsList(stats: List<Pair<String, Int>>) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Популярные позиции",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(stats) { (name, count) ->
                StatItemCard(
                    itemName = name,
                    count = count,
                    position = stats.indexOf(name to count) + 1
                )
            }
        }
    }
}

@Composable
private fun StatItemCard(
    itemName: String,
    count: Int,
    position: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when(position) {
                1 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                2 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                3 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "#$position",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    itemName,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Продано: $count",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
private fun EmptyStatsView() {
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
                Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Нет данных",
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "За выбранный день не было продаж",
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
                "Ошибка загрузки",
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