package com.kurinna.hivesensemobile.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kurinna.hivesensemobile.network.ApiClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class HomeUiState(
    val temperature: String = "--",
    val humidity: String = "--",
    val activeAlertsCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var autoRefreshJob: Job? = null

    fun loadHomeData(currentWarehouseId: Int?, showLoading: Boolean = true) {
        if (currentWarehouseId == null) return

        viewModelScope.launch {
            if (showLoading) {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = null
                )
            }

            try {
                val measurements = ApiClient.measurementsApi.getMeasurements()
                val alerts = ApiClient.alertsApi.getAlerts()

                val latestMeasurement = measurements
                    .firstOrNull { it.sensor.warehouse_id == currentWarehouseId }

                val activeAlertsCount = alerts.count {
                    it.warehouse_id == currentWarehouseId && it.status != "RESOLVED"
                }

                _uiState.value = HomeUiState(
                    temperature = latestMeasurement?.temperature_c ?: "--",
                    humidity = latestMeasurement?.humidity_percent ?: "--",
                    activeAlertsCount = activeAlertsCount,
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Не вдалося завантажити дані головної сторінки"
                )
            }
        }
    }

    fun startAutoRefresh(currentWarehouseId: Int?) {
        if (currentWarehouseId == null) return

        stopAutoRefresh()

        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                loadHomeData(currentWarehouseId, showLoading = false)
                delay(5000)
            }
        }
    }

    fun stopAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
    }

    fun clear() {
        stopAutoRefresh()
        _uiState.value = HomeUiState()
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoRefresh()
    }
}