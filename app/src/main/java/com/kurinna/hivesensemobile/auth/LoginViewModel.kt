package com.kurinna.hivesensemobile.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kurinna.hivesensemobile.network.ApiClient
import com.kurinna.hivesensemobile.network.LoginRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess.asStateFlow()

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    private val _userId = MutableStateFlow<Int?>(null)
    val userId: StateFlow<Int?> = _userId.asStateFlow()

    fun onEmailChanged(value: String) {
        _email.value = value
    }

    fun onPasswordChanged(value: String) {
        _password.value = value
    }

    fun resetLoginState() {
        _loginSuccess.value = false
        _errorMessage.value = null
        _accessToken.value = null
        _userId.value = null
    }

    fun login() {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _errorMessage.value = "Введіть email і пароль"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _loginSuccess.value = false

            try {
                val response = ApiClient.authApi.login(
                    LoginRequest(
                        email = _email.value.trim(),
                        password = _password.value
                    )
                )

                if (response.access_token.isNotBlank()) {
                    _accessToken.value = response.access_token
                    _userId.value = response.user.id
                    _loginSuccess.value = true
                } else {
                    _errorMessage.value = "Не вдалося отримати токен"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Помилка авторизації"
            } finally {
                _isLoading.value = false
            }
        }
    }
}