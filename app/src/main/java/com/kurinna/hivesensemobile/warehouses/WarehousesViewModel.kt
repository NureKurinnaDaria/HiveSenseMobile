package com.kurinna.hivesensemobile.warehouses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kurinna.hivesensemobile.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WarehousesViewModel : ViewModel() {

    private val _warehouses = MutableStateFlow<List<WarehouseDto>>(emptyList())
    val warehouses: StateFlow<List<WarehouseDto>> = _warehouses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadWarehouses() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                _warehouses.value = ApiClient.warehousesApi.getWarehouses()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Не вдалося завантажити склади"
            } finally {
                _isLoading.value = false
            }
        }
    }
}