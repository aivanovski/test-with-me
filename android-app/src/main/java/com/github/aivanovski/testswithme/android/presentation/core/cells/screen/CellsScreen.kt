package com.github.aivanovski.testswithme.android.presentation.core.cells.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.aivanovski.testswithme.android.presentation.core.cells.BaseCellViewModel
import com.github.aivanovski.testswithme.android.presentation.core.compose.CenteredBox
import com.github.aivanovski.testswithme.android.presentation.core.compose.EmptyMessage
import com.github.aivanovski.testswithme.android.presentation.core.compose.ErrorMessage
import com.github.aivanovski.testswithme.android.presentation.core.compose.ProgressIndicator

typealias CellFactory = @Composable (viewModel: BaseCellViewModel) -> Unit

@Composable
fun CellsScreen(
    state: CellsScreenState,
    cellFactory: CellFactory
) {
    val terminalState = state.terminalState

    if (terminalState != null) {
        when (terminalState) {
            TerminalState.Loading -> {
                ProgressIndicator()
            }

            is TerminalState.Empty -> {
                CenteredBox {
                    EmptyMessage(message = terminalState.message)
                }
            }

            is TerminalState.Error -> {
                CenteredBox {
                    ErrorMessage(message = terminalState.message)
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(state.viewModels) { viewModel ->
                cellFactory.invoke(viewModel)
            }
        }
    }
}