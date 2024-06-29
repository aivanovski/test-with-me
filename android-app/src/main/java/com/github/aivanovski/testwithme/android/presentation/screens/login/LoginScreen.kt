package com.github.aivanovski.testwithme.android.presentation.screens.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.aivanovski.testwithme.android.R
import com.github.aivanovski.testwithme.android.presentation.core.compose.AppTextField
import com.github.aivanovski.testwithme.android.presentation.core.compose.ProgressIndicator
import com.github.aivanovski.testwithme.android.presentation.core.compose.ThemedScreenPreview
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.AppTheme
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.LightTheme
import com.github.aivanovski.testwithme.android.presentation.screens.login.model.LoginIntent
import com.github.aivanovski.testwithme.android.presentation.screens.login.model.LoginState

@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    val state by viewModel.state.collectAsState()

    LoginScreen(
        state = state,
        onIntent = viewModel::sendIntent
    )
}

@Composable
private fun LoginScreen(
    state: LoginState,
    onIntent: (event: LoginIntent) -> Unit
) {
    when (state) {
        LoginState.NotInitialized -> {}

        LoginState.Loading -> {
            ProgressIndicator()
        }

        is LoginState.Data -> {
            if (state.errorMessage != null) {
                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.errorMessage,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        color = AppTheme.theme.materialColors.error,
                        modifier = Modifier
                            .padding(top = 64.dp)
                            .fillMaxWidth(fraction = 0.8f)
                    )
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth(fraction = 0.8f)
                ) {
                    AppTextField(
                        value = state.username,
                        label = stringResource(R.string.username),
                        onValueChange = { newUsername ->
                            onIntent.invoke(LoginIntent.OnUsernameChanged(newUsername))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    AppTextField(
                        value = state.password,
                        label = stringResource(R.string.password),
                        onValueChange = { newPassword ->
                            onIntent.invoke(LoginIntent.OnPasswordChanged(newPassword))
                        },
                        isPasswordToggleEnabled = true,
                        isPasswordVisible = state.isPasswordVisible,
                        onPasswordToggleClicked = { isPasswordVisible ->
                            onIntent.invoke(
                                LoginIntent.OnPasswordVisibilityChanged(
                                    isPasswordVisible
                                )
                            )
                        },
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            onIntent.invoke(LoginIntent.OnLoginButtonClicked)
                        },
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.login)
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun LoginScreenLightPreview() {
    ThemedScreenPreview(theme = LightTheme) {
        LoginScreen(
            state = newDataState(),
            onIntent = {}
        )
    }
}

private fun newDataState(): LoginState.Data =
    LoginState.Data(
        username = "john.doe",
        password = "abc123",
        isPasswordVisible = false,
        errorMessage = "Error has been occurred, please try again"
    )