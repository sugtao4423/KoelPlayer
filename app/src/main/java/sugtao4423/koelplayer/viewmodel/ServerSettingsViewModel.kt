package sugtao4423.koelplayer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.koel4j.Koel4j
import sugtao4423.koelplayer.App

class ServerSettingsViewModel(application: Application) : AndroidViewModel(application) {

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object Success : AuthState()
        object AuthError : AuthState()
    }

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    private val _serverHost = MutableLiveData<String>()
    val serverHost: LiveData<String> = _serverHost

    private val app = getApplication<App>()

    fun initialize(isReAuth: Boolean) {
        if (isReAuth) {
            _serverHost.value = app.koelServer
        }
    }

    fun authenticate(host: String, email: String, password: String) {
        if (_authState.value == AuthState.Loading) {
            return
        }

        val normalizedHost = if (host.endsWith("/")) {
            host.removeSuffix("/")
        } else {
            host
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val token = withContext(Dispatchers.IO) {
                runCatching {
                    Koel4j(normalizedHost).auth(email, password)
                }.getOrNull()
            }
            if (token == null) {
                _authState.value = AuthState.AuthError
                return@launch
            }

            app.koelServer = normalizedHost
            app.koelToken = token

            _authState.value = AuthState.Success
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

}
