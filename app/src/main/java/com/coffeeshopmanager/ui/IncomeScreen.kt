package com.coffeeshopmanager.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coffeeshopmanager.data.MenuRepository
import com.coffeeshopmanager.data.SalesRepository
import com.coffeeshopmanager.data.EmployeesRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import android.app.DatePickerDialog
import java.util.Calendar

@Composable
fun IncomeScreen() {
    val salesRepo = remember { SalesRepository() }
    val menuRepo = remember { MenuRepository() }
    val empRepo = remember { EmployeesRepository() }
    var date by remember { mutableStateOf(Date()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var incomeData by remember { mutableStateOf(IncomeData()) }
    val scope = rememberCoroutineScope()
    val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val showDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date)

    fun loadIncome() {
        isLoading = true
        scope.launch {
            try {
                val sales = salesRepo.getSales()
                val menu = menuRepo.getMenu()
                val emps = empRepo.getEmployees()
                val todaySales = sales.filter {
                    dateFormat.format(Date(it.date)) == dateFormat.format(date)
                }
                val totalRevenue = todaySales.sumOf { it.total }
                val costById = menu.associateBy({ it.id }, { it.cost })
                var totalCost = 0.0
                todaySales.forEach { sale ->
                    sale.items.forEach { item ->
                        totalCost += (costById[item.menuItemId] ?: 0.0) * item.quantity
                    }
                }
                val salaryPerDay = emps.sumOf { it.salaryPerDay }
                val income = totalRevenue - totalCost - salaryPerDay
                incomeData = IncomeData(totalRevenue, totalCost, salaryPerDay, income)
                error = null
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(date) { loadIncome() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            "Финансовый отчет",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        DateSelector(
            currentDate = date,
            displayDate = showDate,
            onDateChange = { newDate -> date = newDate }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (isLoading) {
            LoadingIndicator()
        } else if (error != null) {
            ErrorView(errorMessage = error!!)
        } else {
            IncomeDataCard(incomeData = incomeData)
        }
    }
}

@Composable
private fun DateSelector(
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
private fun IncomeDataCard(incomeData: IncomeData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Финансовый отчет",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            FinancialItem(
                label = "Выручка",
                value = incomeData.revenue,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            FinancialItem(
                label = "Себестоимость",
                value = incomeData.cost,
                color = MaterialTheme.colorScheme.error,
                isExpense = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            FinancialItem(
                label = "Зарплаты",
                value = incomeData.salary,
                color = MaterialTheme.colorScheme.error,
                isExpense = true
            )
            
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Итоговый доход",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Surface(
                    color = if (incomeData.income >= 0) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "${incomeData.income}₽",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (incomeData.income >= 0)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FinancialItem(
    label: String,
    value: Double,
    color: Color,
    isExpense: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge
        )
        
        Text(
            (if (isExpense && value > 0) "- " else "") + "${value}₽",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
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
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

data class IncomeData(
    val revenue: Double = 0.0,
    val cost: Double = 0.0,
    val salary: Double = 0.0,
    val income: Double = 0.0
) 