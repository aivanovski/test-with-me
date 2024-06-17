package com.github.aivanovski.testwithme.android.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.aivanovski.testwithme.android.di.GlobalInjector
import com.github.aivanovski.testwithme.android.domain.ErrorInteractor
import com.github.aivanovski.testwithme.android.extensions.asFlow
import com.github.aivanovski.testwithme.android.presentation.screens.login.model.LoginIntent
import com.github.aivanovski.testwithme.android.presentation.screens.login.model.LoginState
import com.github.aivanovski.testwithme.extensions.unwrapError
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class LoginViewModel(
    private val interactor: LoginInteractor,
    private val errorInteractor: ErrorInteractor
) : ViewModel() {

    // TODO: check if onCleared() is called

    val state = MutableStateFlow<LoginState>(LoginState.NotInitialized)

    private val intents = Channel<LoginIntent>()

    fun start() {
        if (state.value != LoginState.NotInitialized) {
            return
        }

        viewModelScope.launch {
            intents.receiveAsFlow()
                .onStart { emit(LoginIntent.Initialize) }
                .flatMapLatest { intent -> handleIntent(intent, state.value) }
                .collect { newState ->
                    state.value = newState
                }
        }
    }

    fun sendIntent(intent: LoginIntent) {
        viewModelScope.launch {
            intents.send(intent)
        }
    }

    private fun handleIntent(intent: LoginIntent, state: LoginState): Flow<LoginState> {
        return when (intent) {
            is LoginIntent.Initialize -> createInitialState().asFlow()
            is LoginIntent.OnLoginButtonClicked -> onLoginButtonClicked(state)
            is LoginIntent.OnUsernameChanged -> onUsernameChanged(intent, state).asFlow()
            is LoginIntent.OnPasswordChanged -> onPasswordChanged(intent, state).asFlow()
            is LoginIntent.OnPasswordVisibilityChanged -> onPasswordVisibilityChanged(
                intent = intent,
                currentState = state
            ).asFlow()
        }
    }

    private fun createInitialState(): LoginState {
        return LoginState.Data(
            username = "admin",
            password = "abc123",
            isPasswordVisible = false,
            errorMessage = null
        )
    }

    private fun onUsernameChanged(
        intent: LoginIntent.OnUsernameChanged,
        currentState: LoginState
    ): LoginState {
        val state = (currentState as? LoginState.Data) ?: return currentState

        return state.copy(
            username = intent.username
        )
    }

    private fun onPasswordChanged(
        intent: LoginIntent.OnPasswordChanged,
        currentState: LoginState
    ): LoginState {
        val state = (currentState as? LoginState.Data) ?: return currentState

        return state.copy(
            password = intent.password
        )
    }

    private fun onPasswordVisibilityChanged(
        intent: LoginIntent.OnPasswordVisibilityChanged,
        currentState: LoginState
    ): LoginState {
        val state = (currentState as? LoginState.Data) ?: return currentState

        return state.copy(
            isPasswordVisible = intent.isVisible
        )
    }

    private fun onLoginButtonClicked(
        currentState: LoginState
    ): Flow<LoginState> {
        val state = (currentState as? LoginState.Data) ?: return flowOf(currentState)

        return flow {
            emit(LoginState.Loading)

            val response = interactor.login(state.username, state.password)
            Timber.d("response=$response")

            if (response.isRight()) {
                Timber.d("Success!") // TODO: show next screen
                emit(currentState)
            } else {
                emit(
                    currentState.copy(
                        errorMessage = errorInteractor.getMessage(response.unwrapError())
                    )
                )
            }
        }
    }

    class Factory : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GlobalInjector.get<LoginViewModel>() as T
        }
    }
}