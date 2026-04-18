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
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.kurinna.hivesensemobile.network.ApiClient
import com.kurinna.hivesensemobile.network.SaveFcmTokenRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("FCM", "Notification permission granted: $isGranted")
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        askNotificationPermission()
        fetchFcmToken()

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

                    if (!savedToken.isNullOrBlank()) {
                        fetchFcmToken()
                    }
                }

                LaunchedEffect(loginSuccess, accessToken) {
                    if (loginSuccess && !accessToken.isNullOrBlank()) {
                        sessionManager.saveAccessToken(accessToken!!)
                        SessionHolder.accessToken = accessToken
                        fetchFcmToken()
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
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun fetchFcmToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("FCM", "Fetching FCM token failed", task.exception)
                    return@addOnCompleteListener
                }

                val token = task.result
                Log.d("FCM", "FCM token: $token")

                if (!SessionHolder.accessToken.isNullOrBlank()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            ApiClient.usersApi.saveMyFcmToken(
                                SaveFcmTokenRequest(fcmToken = token)
                            )
                            Log.d("FCM", "FCM token sent to backend successfully")
                        } catch (e: Exception) {
                            Log.e("FCM", "Failed to send FCM token to backend", e)
                        }
                    }
                } else {
                    Log.d("FCM", "JWT token is missing, FCM token was not sent yet")
                }
            }
    }
}