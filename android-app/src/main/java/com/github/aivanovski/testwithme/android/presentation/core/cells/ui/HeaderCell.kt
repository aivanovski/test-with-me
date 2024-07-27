package com.github.aivanovski.testwithme.android.presentation.core.cells.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.aivanovski.testwithme.android.presentation.core.cells.model.HeaderCellIntent
import com.github.aivanovski.testwithme.android.presentation.core.cells.model.HeaderCellModel
import com.github.aivanovski.testwithme.android.presentation.core.cells.viewModel.HeaderCellViewModel
import com.github.aivanovski.testwithme.android.presentation.core.compose.AppIcons
import com.github.aivanovski.testwithme.android.presentation.core.compose.PreviewIntentProvider
import com.github.aivanovski.testwithme.android.presentation.core.compose.ThemedPreview
import com.github.aivanovski.testwithme.android.presentation.core.compose.rememberOnClickedCallback
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.AppTheme
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.ElementMargin
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.LightTheme
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.MediumMargin
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.SmallIconSize
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.SmallMargin

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
            .defaultMinSize(minHeight = 48.dp) // TODO: dimen
    ) {
        Text(
            text = model.title,
            color = AppTheme.theme.colors.primaryText,
            style = AppTheme.theme.typography.titleMedium,
            modifier = Modifier
                .weight(weight = 1f)
        )

        if (model.icon != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(onClick = onIconClick)
                    .padding(
                        start = ElementMargin,
                        end = ElementMargin,
                        top = MediumMargin,
                        bottom = MediumMargin
                    )
            ) {
                if (model.iconText != null) {
                    Text(
                        text = model.iconText,
                        color = AppTheme.theme.colors.primaryText,
                        style = AppTheme.theme.typography.titleMedium,
                        modifier = Modifier
                            .padding(
                                end = SmallMargin
                            )
                    )
                }

                Icon(
                    imageVector = model.icon,
                    contentDescription = null,
                    tint = AppTheme.theme.colors.primaryText,
                    modifier = Modifier
                        .size(SmallIconSize)
                )
            }
        }
    }
}

@Composable
@Preview
fun HeaderCellPreview() {
    ThemedPreview(theme = LightTheme) {
        Column {
            HeaderCell(newHeaderCellViewModel())
            Spacer(modifier = Modifier.height(ElementMargin))
            HeaderCell(newHeaderWithIconCellViewModel())
        }
    }
}

fun newHeaderCellViewModel(title: String = "Header") =
    HeaderCellViewModel(
        model = HeaderCellModel(
            id = "id",
            title = title,
            iconText = null,
            icon = null
        ),
        intentProvider = PreviewIntentProvider
    )

fun newHeaderWithIconCellViewModel(
    title: String = "Header",
    iconText: String = "All"
) = HeaderCellViewModel(
    model = HeaderCellModel(
        id = "id",
        title = title,
        iconText = iconText,
        icon = AppIcons.ArrowForward
    ),
    intentProvider = PreviewIntentProvider
)