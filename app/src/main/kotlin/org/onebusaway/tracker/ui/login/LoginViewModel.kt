package org.onebusaway.tracker.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.onebusaway.tracker.api.ApiService
import org.onebusaway.tracker.api.LoginRequest
import org.onebusaway.tracker.data.SessionManager
import javax.inject.Inject

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(phone: String, pin: String) {
        if (phone.isBlank() || pin.isBlank()) {
            _loginState.value = LoginState.Error("Phone and PIN are required")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = apiService.login(LoginRequest(phone, pin))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.token != null) {
                        sessionManager.saveAuthToken(body.token)
                        _loginState.value = LoginState.Success
                    } else {
                        _loginState.value = LoginState.Error(body?.error ?: "Unknown error")
                    }
                } else {
                    _loginState.value = LoginState.Error("Login failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Network error: ${e.message}")
            }
        }
    }
}
