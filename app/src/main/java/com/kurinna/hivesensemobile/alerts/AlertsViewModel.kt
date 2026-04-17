package com.kurinna.hivesensemobile.alerts

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

class AlertsViewModel : ViewModel() {

    private val _alerts = MutableStateFlow<List<AlertDto>>(emptyList())
    val alerts: StateFlow<List<AlertDto>> = _alerts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _actionInProgressId = MutableStateFlow<Int?>(null)
    val actionInProgressId: StateFlow<Int?> = _actionInProgressId.asStateFlow()

    private var autoRefreshJob: Job? = null

    fun loadAlerts(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _isLoading.value = true
            }
            _errorMessage.value = null

            try {
                _alerts.value = ApiClient.alertsApi.getAlerts()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Не вдалося завантажити тривоги"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun acknowledgeAlert(alertId: Int) {
        viewModelScope.launch {
            _actionInProgressId.value = alertId
            _errorMessage.value = null

            try {
                ApiClient.alertsApi.acknowledgeAlert(alertId)
                _alerts.value = ApiClient.alertsApi.getAlerts()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Не вдалося підтвердити тривогу"
            } finally {
                _actionInProgressId.value = null
            }
        }
    }

    fun startAutoRefresh() {
        stopAutoRefresh()

        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                loadAlerts(showLoading = false)
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
        _alerts.value = emptyList()
        _errorMessage.value = null
        _actionInProgressId.value = null
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoRefresh()
    }
}