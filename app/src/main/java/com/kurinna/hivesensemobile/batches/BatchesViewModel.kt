package com.kurinna.hivesensemobile.batches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kurinna.hivesensemobile.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BatchesViewModel : ViewModel() {

    private val _batches = MutableStateFlow<List<HoneyBatchDto>>(emptyList())
    val batches: StateFlow<List<HoneyBatchDto>> = _batches.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadBatches() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                _batches.value = ApiClient.honeyBatchesApi.getHoneyBatches()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Не вдалося завантажити партії меду"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clear() {
        _batches.value = emptyList()
        _errorMessage.value = null
    }
}