package com.github.aivanovski.testwithme.android.presentation.core.cells.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.aivanovski.testwithme.android.presentation.core.cells.model.HeaderCellIntent
import com.github.aivanovski.testwithme.android.presentation.core.compose.PreviewIntentProvider
import com.github.aivanovski.testwithme.android.presentation.core.compose.ThemedPreview
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.AppTheme
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.ElementMargin
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.LightTheme
import com.github.aivanovski.testwithme.android.presentation.core.cells.model.HeaderCellModel
import com.github.aivanovski.testwithme.android.presentation.core.cells.viewModel.HeaderCellViewModel
import com.github.aivanovski.testwithme.android.presentation.core.compose.rememberOnClickedCallback

@Composable
fun HeaderCell(viewModel: HeaderCellViewModel) {
    val model = viewModel.model

    val onIconClick = rememberOnClickedCallback {
        viewModel.sendIntent(HeaderCellIntent.OnIconClick(model.id))
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = ElementMargin)
            .defaultMinSize(minHeight = 48.dp)
    ) {
        Text(
            text = model.title,
            color = AppTheme.theme.colors.primaryText,
            style = AppTheme.theme.typography.titleMedium,
            modifier = Modifier
                .weight(weight = 1f)
        )

        if (model.isIconVisible) {
            Box(
                modifier = Modifier
                    .padding(
                        end = ElementMargin
                    )
                    .clickable(onClick = onIconClick)
            ) {
                Image(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
@Preview
fun HeaderCellLightPreview() {
    ThemedPreview(theme = LightTheme) {
        HeaderCell(newHeaderCellViewModel())
    }
}

fun newHeaderCellViewModel(): HeaderCellViewModel {
    return HeaderCellViewModel(
        model = HeaderCellModel(
            id = "id",
            title = "Header",
            isIconVisible = true
        ),
        intentProvider = PreviewIntentProvider
    )
}