package com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.github.aivanovski.testwithme.android.R
import com.github.aivanovski.testwithme.android.presentation.core.compose.PreviewIntentProvider
import com.github.aivanovski.testwithme.android.presentation.core.compose.ThemedPreview
import com.github.aivanovski.testwithme.android.presentation.core.compose.rememberOnClickedCallback
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.AppTheme
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.CardCornerSize
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.ElementMargin
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.HalfMargin
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.LightTheme
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.model.FlowTitleCellIntent
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.model.FlowTitleCellModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.viewModel.FlowTitleCellViewModel

@Composable
fun FlowTitleCell(viewModel: FlowTitleCellViewModel) {
    val onRunClick = rememberOnClickedCallback {
        viewModel.sendIntent(
            FlowTitleCellIntent.OnRunButtonClick(
                id = viewModel.model.id
            )
        )
    }

    Card(
        shape = RoundedCornerShape(size = CardCornerSize),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.theme.colors.primaryCardBackground
        ),
        modifier = Modifier
            .padding(
                top = ElementMargin,
                start = ElementMargin,
                end = ElementMargin
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ElementMargin,
                    vertical = ElementMargin
                )
        ) {
            Text(
                text = viewModel.model.flowName,
                color = AppTheme.theme.colors.primaryText,
                style = AppTheme.theme.typography.bodyLarge
            )

            Text(
                text = viewModel.model.projectName,
                color = AppTheme.theme.colors.secondaryText,
                style = AppTheme.theme.typography.bodySmall
            )

            Button(
                onClick = onRunClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = HalfMargin)
            ) {
                Text(
                    text = stringResource(R.string.run_upper)
                )
            }
        }
    }
}

@Composable
@Preview
fun FlowTitleCellLightPreview() {
    ThemedPreview(theme = LightTheme) {
        FlowTitleCell(
            viewModel = newTitleCellViewModel()
        )
    }
}

fun newTitleCellViewModel(): FlowTitleCellViewModel {
    val model = FlowTitleCellModel(
        id = "uid",
        flowName = "Unlock database",
        projectName = "KeePassVault"
    )

    return FlowTitleCellViewModel(model, PreviewIntentProvider)
}
