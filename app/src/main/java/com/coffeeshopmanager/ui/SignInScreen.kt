package com.coffeeshopmanager.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background

@Composable
fun SignInScreen(
    onSignIn: (email: String, password: String) -> Unit,
    onRegister: (email: String, password: String, name: String) -> Unit,
    errorMessage: String? = null,
    scope: CoroutineScope
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isRegister by remember { mutableStateOf(false) }
    var showForgotPassword by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    
    // Темные цвета для формы входа
    val darkBackground = Color(0xFF121212)
    val darkSurface = Color(0xFF1E1E1E)
    val accentColor = Color(0xFFBB86FC)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 40.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Заголовок и иконка
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(64.dp)
            )
        
            Spacer(modifier = Modifier.height(16.dp))
        
            Text(
                text = "Коффе шоп",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
        
            Spacer(modifier = Modifier.height(8.dp))
        
            Text(
                text = if (isRegister) "Регистрация нового аккаунта" else "Вход в систему",
                style = MaterialTheme.typography.titleLarge,
                color = Color.LightGray
            )
        
            Spacer(modifier = Modifier.height(32.dp))
        
            // Форма входа/регистрации
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = darkSurface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isRegister) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Имя", color = Color.LightGray) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person, 
                                    contentDescription = "Имя",
                                    tint = accentColor
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email", color = Color.LightGray) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email, 
                                contentDescription = "Email",
                                tint = accentColor
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.LightGray
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Пароль", color = Color.LightGray) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock, 
                                contentDescription = "Пароль",
                                tint = accentColor
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.LightGray
                        )
                    )
                    
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFF4E1B00),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = translateErrorMessage(errorMessage),
                                color = Color(0xFFFF9A85),
                                modifier = Modifier.padding(8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            if (isRegister) onRegister(email, password, name) else onSignIn(email, password)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = MaterialTheme.shapes.medium,
                        enabled = email.isNotBlank() && password.length >= 6 && (!isRegister || name.isNotBlank()),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(
                            imageVector = if (isRegister) Icons.Default.Person else Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            if (isRegister) "Зарегистрироваться" else "Войти",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextButton(
                        onClick = { isRegister = !isRegister },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = accentColor
                        )
                    ) {
                        Text(
                            if (isRegister) "Уже есть аккаунт? Войти" else "Нет аккаунта? Зарегистрироваться"
                        )
                    }
                    
                    if (!isRegister) {
                        TextButton(
                            onClick = { showForgotPassword = true },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFF03DAC5)
                            )
                        ) {
                            Text("Забыли пароль?")
                        }
                    }
                }
            }
        }
    }
    
    // Диалог восстановления пароля
    if (showForgotPassword) {
        AlertDialog(
            onDismissRequest = { showForgotPassword = false },
            title = { Text("Восстановление пароля") },
            text = {
                Column {
                    Text("Введите email для восстановления пароля:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Реализация восстановления пароля будет добавлена позже
                        showForgotPassword = false
                        scope.launch {
                            // Показать сообщение об успешной отправке
                        }
                    },
                    enabled = resetEmail.isNotBlank()
                ) {
                    Text("Отправить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPassword = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

// Функция для перевода сообщений об ошибках
private fun translateErrorMessage(error: String): String {
    return when {
        error.contains("no user record", ignoreCase = true) -> 
            "Пользователь с таким email не найден"
        error.contains("password is invalid", ignoreCase = true) -> 
            "Неверный пароль"
        error.contains("badly formatted", ignoreCase = true) ->
            "Неверный формат email"
        error.contains("already in use", ignoreCase = true) ->
            "Email уже используется"
        error.contains("should be at least 6 characters", ignoreCase = true) ->
            "Пароль должен содержать не менее 6 символов"
        error.contains("too many unsuccessful login", ignoreCase = true) ->
            "Слишком много неудачных попыток входа. Попробуйте позже"
        error.contains("network error", ignoreCase = true) ->
            "Ошибка сети. Проверьте подключение к интернету"
        error.contains("blocked", ignoreCase = true) ->
            "Аккаунт заблокирован"
        error.contains("error", ignoreCase = true) ->
            "Произошла ошибка при входе"
        error.contains("weak password", ignoreCase = true) ->
            "Слишком простой пароль. Используйте более сложный пароль"
        error.contains("no internet", ignoreCase = true) ->
            "Нет подключения к интернету"
        error.contains("connection", ignoreCase = true) ->
            "Проблема с подключением к серверу"
        error.contains("timeout", ignoreCase = true) ->
            "Превышено время ожидания ответа от сервера"
        error.contains("permission", ignoreCase = true) ->
            "Нет прав доступа"
        else -> 
            "Ошибка: $error"
    }
} 