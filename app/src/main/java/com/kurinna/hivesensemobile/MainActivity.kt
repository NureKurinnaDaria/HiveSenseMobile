package com.kurinna.hivesensemobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kurinna.hivesensemobile.alerts.AlertsScreen
import com.kurinna.hivesensemobile.auth.LoginScreen
import com.kurinna.hivesensemobile.auth.LoginViewModel
import com.kurinna.hivesensemobile.batches.BatchesScreen
import com.kurinna.hivesensemobile.core.SessionHolder
import com.kurinna.hivesensemobile.core.SessionManager
import com.kurinna.hivesensemobile.measurements.MeasurementsScreen
import com.kurinna.hivesensemobile.navigation.HomeScreen
import com.kurinna.hivesensemobile.navigation.HomeViewModel
import com.kurinna.hivesensemobile.ui.theme.HiveSenseMobileTheme
import com.kurinna.hivesensemobile.users.UsersViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(applicationContext)

        setContent {
            HiveSenseMobileTheme {
                val loginViewModel: LoginViewModel = viewModel()
                val usersViewModel: UsersViewModel = viewModel()
                val homeViewModel: HomeViewModel = viewModel()

                val loginSuccess by loginViewModel.loginSuccess.collectAsState()
                val accessToken by loginViewModel.accessToken.collectAsState()
                val savedToken by sessionManager.accessToken.collectAsState(initial = null)

                val user by usersViewModel.user.collectAsState()
                val homeUiState by homeViewModel.uiState.collectAsState()

                val scope = rememberCoroutineScope()

                var isLoggedIn by remember { mutableStateOf(false) }
                var currentScreen by remember { mutableStateOf("home") }

                LaunchedEffect(savedToken) {
                    isLoggedIn = !savedToken.isNullOrBlank()
                    SessionHolder.accessToken = savedToken
                }

                LaunchedEffect(loginSuccess, accessToken) {
                    if (loginSuccess && !accessToken.isNullOrBlank()) {
                        sessionManager.saveAccessToken(accessToken!!)
                        SessionHolder.accessToken = accessToken
                        isLoggedIn = true
                        currentScreen = "home"
                    }
                }

                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn) {
                        usersViewModel.loadCurrentUser()
                    }
                }

                LaunchedEffect(currentScreen, user?.warehouse_id) {
                    if (currentScreen == "home" && user?.warehouse_id != null) {
                        homeViewModel.loadHomeData(user!!.warehouse_id)
                        homeViewModel.startAutoRefresh(user!!.warehouse_id)
                    } else {
                        homeViewModel.stopAutoRefresh()
                    }
                }

                if (isLoggedIn) {
                    when (currentScreen) {
                        "alerts" -> {
                            AlertsScreen(
                                onBack = { currentScreen = "home" },
                                currentWarehouseId = user?.warehouse_id
                            )
                        }

                        "measurements" -> {
                            MeasurementsScreen(
                                onBack = { currentScreen = "home" },
                                currentWarehouseId = user?.warehouse_id
                            )
                        }

                        "batches" -> {
                            BatchesScreen(
                                onBack = { currentScreen = "home" },
                                currentWarehouseId = user?.warehouse_id
                            )
                        }

                        else -> {
                            HomeScreen(
                                warehouseName = user?.warehouse?.name ?: "Склад не знайдено",
                                warehouseLocation = user?.warehouse?.location ?: "Невідомо",
                                warehouseStatus = user?.warehouse?.status ?: "Невідомо",
                                temperature = homeUiState.temperature,
                                humidity = homeUiState.humidity,
                                activeAlertsCount = homeUiState.activeAlertsCount,
                                onOpenAlerts = { currentScreen = "alerts" },
                                onOpenMeasurements = { currentScreen = "measurements" },
                                onOpenBatches = { currentScreen = "batches" },
                                onLogout = {
                                    scope.launch {
                                        sessionManager.clearSession()
                                        SessionHolder.accessToken = null
                                        loginViewModel.resetLoginState()
                                        usersViewModel.clearUser()
                                        homeViewModel.clear()
                                        isLoggedIn = false
                                        currentScreen = "home"
                                    }
                                }
                            )
                        }
                    }
                } else {
                    LoginScreen(viewModel = loginViewModel)
                }
            }
        }
    }
}