package com.coffeeshopmanager.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ExitToApp
import kotlinx.coroutines.launch
import com.coffeeshopmanager.data.AuthRepository

@Composable
fun ProfileScreen(userId: String, userRole: String, onSignOut: () -> Unit) {
    val authRepository = remember { AuthRepository() }
    var userName by remember { mutableStateOf("Загрузка...") }
    val scope = rememberCoroutineScope()
    
    // Загрузка имени пользователя
    LaunchedEffect(userId) {
        scope.launch {
            val nameResult = authRepository.getUserName(userId)
            if (nameResult.isSuccess) {
                userName = nameResult.getOrNull() ?: "Пользователь"
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Профиль",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 24.dp)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(Modifier.height(8.dp))
                
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(Modifier.height(16.dp))
                
                ProfileInfoItem(
                    label = "Роль", 
                    value = when(userRole) {
                        "ADMIN" -> "Администратор"
                        "OWNER" -> "Владелец"
                        else -> "Пользователь"
                    },
                    showBadge = true
                )
                
                Spacer(Modifier.height(8.dp))
                
                ProfileInfoItem("Имя", userName)
            }
        }
        
        Spacer(Modifier.weight(1f))
        
        Button(
            onClick = onSignOut,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Icon(
                Icons.Default.ExitToApp,
                contentDescription = "Выйти",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                "Выйти из аккаунта",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun ProfileInfoItem(label: String, value: String, showBadge: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (showBadge) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        } else {
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
} 