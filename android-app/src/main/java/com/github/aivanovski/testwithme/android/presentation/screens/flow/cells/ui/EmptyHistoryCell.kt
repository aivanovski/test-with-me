package com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.aivanovski.testwithme.android.R
import com.github.aivanovski.testwithme.android.presentation.core.compose.ThemedPreview
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.AppTheme
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.LightTheme
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.model.EmptyHistoryCellModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.viewModel.EmptyHistoryCellViewModel

@Composable
fun EmptyHistoryCell(viewModel: EmptyHistoryCellViewModel) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 300.dp)
    ) {
        Text(
            text = viewModel.model.message,
            color = AppTheme.theme.colors.primaryText,
            style = AppTheme.theme.typography.titleLarge
        )
    }
}

@Composable
@Preview
fun EmptyHistoryCellPreview() {
    ThemedPreview(
        theme = LightTheme,
        background = LightTheme.colors.secondaryBackground
    ) {
        EmptyHistoryCell(newEmptyHistoryCellViewModel())
    }
}

@Composable
fun newEmptyHistoryCellViewModel(): EmptyHistoryCellViewModel {
    return EmptyHistoryCellViewModel(
        model = EmptyHistoryCellModel(
            message = stringResource(R.string.no_runs)
        )
    )
}