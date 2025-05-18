package com.coffeeshopmanager.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coffeeshopmanager.data.Employee
import com.coffeeshopmanager.data.EmployeesRepository
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign

@Composable
fun EmployeesScreen(userRole: String = "USER") {
    val repo = remember { EmployeesRepository() }
    var employees by remember { mutableStateOf(listOf<Employee>()) }
    var showDialog by remember { mutableStateOf(false) }
    var editEmployee by remember { mutableStateOf<Employee?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var filterPosition by remember { mutableStateOf("") }
    var sortAsc by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            employees = repo.getEmployees()
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    val filtered = employees.filter { it.position.contains(filterPosition, ignoreCase = true) }
    val sorted = if (sortAsc) filtered.sortedBy { it.name } else filtered.sortedByDescending { it.name }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        HeaderSection(
            title = "Сотрудники",
            canAddEmployee = userRole == "ADMIN" || userRole == "OWNER",
            onAddClick = { editEmployee = null; showDialog = true }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        FilterSection(
            filterPosition = filterPosition,
            onFilterChange = { filterPosition = it },
            positionOptions = employees.map { it.position }.distinct().filter { it.isNotBlank() },
            sortAsc = sortAsc,
            onSortToggle = { sortAsc = !sortAsc }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            LoadingIndicator()
        } else if (error != null) {
            ErrorView(error!!)
        } else if (sorted.isEmpty()) {
            EmptyStateView()
        } else {
            EmployeesList(
                employees = sorted,
                userRole = userRole,
                onEditClick = { editEmployee = it; showDialog = true },
                onDeleteClick = { emp ->
                    isLoading = true
                    scope.launch {
                        try {
                            repo.deleteEmployee(emp.id)
                            employees = repo.getEmployees()
                        } catch (e: Exception) {
                            error = e.message
                        } finally {
                            isLoading = false
                        }
                    }
                }
            )
        }
    }
    
    if (showDialog) {
        EmployeeEditDialog(
            initial = editEmployee,
            onDismiss = { showDialog = false },
            onSave = { emp ->
                isLoading = true
                scope.launch {
                    try {
                        if (emp.id.isEmpty()) repo.addEmployee(emp)
                        else repo.updateEmployee(emp)
                        employees = repo.getEmployees()
                        showDialog = false
                    } catch (e: Exception) {
                        error = e.message
                    } finally {
                        isLoading = false
                    }
                }
            }
        )
    }
}

@Composable
private fun HeaderSection(
    title: String,
    canAddEmployee: Boolean,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge
        )
        
        if (canAddEmployee) {
            FilledTonalButton(
                onClick = onAddClick,
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Добавить")
            }
        }
    }
}

@Composable
private fun FilterSection(
    filterPosition: String,
    onFilterChange: (String) -> Unit,
    positionOptions: List<String>,
    sortAsc: Boolean,
    onSortToggle: () -> Unit
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
                "Фильтры и сортировка",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var expanded by remember { mutableStateOf(false) }
                
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(24.dp)
                )
                
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = filterPosition,
                        onValueChange = onFilterChange,
                        label = { Text("Фильтр по должности") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Показать варианты")
                            }
                        }
                    )
                    
                    DropdownMenu(
                        expanded = expanded && positionOptions.isNotEmpty(),
                        onDismissRequest = { expanded = false }
                    ) {
                        positionOptions.forEach { position ->
                            DropdownMenuItem(
                                text = { Text(position) },
                                onClick = {
                                    onFilterChange(position)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                FilledIconButton(
                    onClick = onSortToggle,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (sortAsc)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        Icons.Default.List,
                        contentDescription = "Сортировка ${if (sortAsc) "по возрастанию" else "по убыванию"}",
                        tint = if (sortAsc)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun EmployeesList(
    employees: List<Employee>,
    userRole: String,
    onEditClick: (Employee) -> Unit,
    onDeleteClick: (Employee) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(employees) { employee ->
            EmployeeCard(
                employee = employee,
                canEdit = userRole == "ADMIN" || userRole == "OWNER",
                onEditClick = { onEditClick(employee) },
                onDeleteClick = { onDeleteClick(employee) }
            )
        }
    }
}

@Composable
private fun EmployeeCard(
    employee: Employee,
    canEdit: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
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
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = employee.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = employee.position,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Ставка: ${employee.salaryPerDay}₽/день",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (canEdit) {
                    Row {
                        IconButton(onClick = onEditClick) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Редактировать",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        IconButton(onClick = onDeleteClick) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Удалить",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
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
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorView(errorMessage: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun EmptyStateView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Сотрудники не найдены",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Попробуйте изменить критерии фильтрации или добавьте нового сотрудника",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EmployeeEditDialog(initial: Employee?, onDismiss: () -> Unit, onSave: (Employee) -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var position by remember { mutableStateOf(initial?.position ?: "") }
    var salary by remember { mutableStateOf(initial?.salaryPerDay?.toString() ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial == null) "Добавить сотрудника" else "Редактировать сотрудника",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ФИО") },
                    placeholder = { Text("Иванов Иван Иванович") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = position,
                    onValueChange = { position = it },
                    label = { Text("Должность") },
                    placeholder = { Text("Бариста") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = salary,
                    onValueChange = { salary = it },
                    label = { Text("Ставка (₽/день)") },
                    placeholder = { Text("1000") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val emp = Employee(
                        id = initial?.id ?: "",
                        name = name,
                        position = position,
                        salaryPerDay = salary.toDoubleOrNull() ?: 0.0
                    )
                    onSave(emp)
                },
                enabled = name.isNotBlank() && position.isNotBlank() && salary.isNotBlank()
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