package com.coffeeshopmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.coffeeshopmanager.ui.theme.CofeeTheme
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.List

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.coffeeshopmanager.ui.MenuScreen
import com.coffeeshopmanager.ui.EmployeesScreen
import com.coffeeshopmanager.ui.SalesScreen
import com.coffeeshopmanager.ui.StatisticsScreen
import com.coffeeshopmanager.ui.IncomeScreen
import com.coffeeshopmanager.ui.CartScreen
import com.coffeeshopmanager.ui.OrderHistoryScreen
import com.coffeeshopmanager.ui.SignInScreen
import com.coffeeshopmanager.data.AuthRepository
import kotlinx.coroutines.launch
import com.coffeeshopmanager.data.MenuItem
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.rememberCoroutineScope
import com.coffeeshopmanager.data.LocalCart
import com.coffeeshopmanager.ui.ProfileScreen
import androidx.compose.ui.unit.dp
import com.coffeeshopmanager.data.UserRole

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CofeeTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot() {
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage = remember { mutableStateOf<String?>(null) }
    val showSnackbar: (String) -> Unit = { msg -> snackbarMessage.value = msg }
    val scope = rememberCoroutineScope()
    LaunchedEffect(snackbarMessage.value) {
        snackbarMessage.value?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage.value = null
        }
    }
    CompositionLocalProvider(LocalShowSnackbar provides showSnackbar) {
        var isSignedIn by remember { mutableStateOf(false) }
        var userRole by remember { mutableStateOf("USER") }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var userId by remember { mutableStateOf("") }
        val authRepository = remember { AuthRepository() }
        val cartState = remember { mutableStateOf(listOf<Pair<MenuItem, Int>>()) }
        
        fun signOut() {
            isSignedIn = false
            userRole = "USER"
            userId = ""
            cartState.value = emptyList()
            errorMessage = null
        }
        
        if (!isSignedIn) {
            SignInScreen(
                onSignIn = { email, password ->
                    scope.launch {
                        val result = authRepository.signIn(email, password)
                        if (result.isSuccess) {
                            val userIdVal = result.getOrNull() ?: ""
                            userId = userIdVal
                            authRepository.createUserIfNotExists(userId, email)
                            val roleResult = authRepository.getUserRole(userId)
                            if (roleResult.isSuccess) {
                                userRole = roleResult.getOrNull() ?: "USER"
                                isSignedIn = true
                                errorMessage = null
                            } else {
                                errorMessage = roleResult.exceptionOrNull()?.localizedMessage ?: "Ошибка получения роли"
                            }
                        } else {
                            errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Ошибка входа"
                        }
                    }
                },
                onRegister = { email, password, name ->
                    scope.launch {
                        val result = authRepository.register(email, password)
                        if (result.isSuccess) {
                            val userIdVal = result.getOrNull() ?: ""
                            userId = userIdVal
                            authRepository.createUserIfNotExists(userId, email, name)
                            val roleResult = authRepository.getUserRole(userId)
                            if (roleResult.isSuccess) {
                                userRole = roleResult.getOrNull() ?: "USER"
                                isSignedIn = true
                                errorMessage = null
                            } else {
                                errorMessage = roleResult.exceptionOrNull()?.localizedMessage ?: "Ошибка получения роли"
                            }
                        } else {
                            errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Ошибка регистрации"
                        }
                    }
                },
                errorMessage = errorMessage,
                scope = scope
            )
        } else {
            var selectedTab by remember { mutableStateOf(0) }
            val allTabs = listOf(
                "Меню" to Icons.Default.Home,
                "Сотрудники" to Icons.Default.Person,
                "Продажи" to Icons.AutoMirrored.Filled.List,
                "Статистика" to Icons.Default.Info,
                "Доходы" to Icons.Default.Star,
                "Корзина" to Icons.Default.ShoppingCart,
                "История" to Icons.Default.Favorite,
                "Профиль" to Icons.Default.AccountCircle
            )
            val tabs = when (userRole) {
                "ADMIN" -> listOf(
                    allTabs[0], // Меню
                    allTabs[1], // Сотрудники
                    allTabs[2], // Продажи
                    allTabs[3], // Статистика
                    allTabs[7]  // Профиль
                )
                "OWNER" -> listOf(
                    allTabs[0], // Меню
                    allTabs[1], // Сотрудники
                    allTabs[2], // Продажи
                    allTabs[3], // Статистика
                    allTabs[4], // Доходы
                    allTabs[7]  // Профиль
                )
                else -> listOf(
                    allTabs[0], // Меню
                    allTabs[5], // Корзина
                    allTabs[6], // История
                    allTabs[7]  // Профиль
                )
            }
            Box(Modifier.fillMaxSize()) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            tabs.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    icon = { Icon(item.second, contentDescription = item.first) },
                                    label = { Text(item.first) },
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index }
                                )
                            }
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState, Modifier.padding(bottom = 80.dp)) }
                ) { innerPadding ->
                    Box(Modifier
                        .fillMaxSize()
                        .padding(innerPadding)) {
                        val currentTab = tabs[selectedTab]
                        val screenName = currentTab.first
                        
                        when (screenName) {
                            "Меню" -> CompositionLocalProvider(LocalCart provides cartState) { 
                                MenuScreen(userRole, showAddToCartButton = userRole == "USER") 
                            }
                            "Сотрудники" -> EmployeesScreen(userRole)
                            "Продажи" -> SalesScreen()
                            "Статистика" -> StatisticsScreen()
                            "Доходы" -> IncomeScreen()
                            "Корзина" -> CompositionLocalProvider(LocalCart provides cartState) { CartScreen(userId) }
                            "История" -> OrderHistoryScreen(userId)
                            "Профиль" -> ProfileScreen(userId, userRole, onSignOut = { signOut() })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CofeeTheme {
        Greeting("Android")
    }
}

val LocalShowSnackbar = staticCompositionLocalOf<(String) -> Unit> { { } }