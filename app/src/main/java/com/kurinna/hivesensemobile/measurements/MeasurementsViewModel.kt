package com.kurinna.hivesensemobile.measurements

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

class MeasurementsViewModel : ViewModel() {

    private val _measurements = MutableStateFlow<List<MeasurementDto>>(emptyList())
    val measurements: StateFlow<List<MeasurementDto>> = _measurements.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var autoRefreshJob: Job? = null

    fun loadMeasurements(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _isLoading.value = true
            }
            _errorMessage.value = null

            try {
                _measurements.value = ApiClient.measurementsApi.getMeasurements()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Не вдалося завантажити вимірювання"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun startAutoRefresh() {
        stopAutoRefresh()

        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                loadMeasurements(showLoading = false)
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
        _measurements.value = emptyList()
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoRefresh()
    }
}