package com.kurinna.hivesensemobile.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kurinna.hivesensemobile.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UsersViewModel : ViewModel() {

    private val _user = MutableStateFlow<UserDto?>(null)
    val user: StateFlow<UserDto?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadCurrentUser() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                _user.value = ApiClient.usersApi.getCurrentUser()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Не вдалося завантажити дані користувача"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearUser() {
        _user.value = null
    }
}