package com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.aivanovski.testwithme.android.presentation.core.compose.ThemedPreview
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.AppTheme
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.CardCornerSize
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.ElementMargin
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.HalfMargin
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.LightTheme
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.QuarterMargin
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.SmallMargin
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.TinyMargin
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.model.FlowStatsCellModel
import com.github.aivanovski.testwithme.android.presentation.screens.flow.cells.viewModel.FlowStatsCellViewModel

@Composable
fun FlowStatsCell(viewModel: FlowStatsCellViewModel) {
    Card(
        shape = RoundedCornerShape(size = CardCornerSize),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.theme.colors.primaryCardBackground
        ),
        modifier = Modifier
            .padding(
                top = QuarterMargin,
                end = ElementMargin,
                start = ElementMargin
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = ElementMargin,
                    start = ElementMargin,
                    end = ElementMargin,
                    bottom = ElementMargin
                )
        ) {
            Text(
                text = "Total runs: ${viewModel.model.total}", // TODO: string
                color = AppTheme.theme.colors.primaryText,
                style = AppTheme.theme.typography.bodyLarge
            )

            Text(
                text = "Success rate: ${viewModel.model.rate}%", // TODO: string
                color = AppTheme.theme.colors.primaryText,
                style = AppTheme.theme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(HalfMargin))

            Row {
                Chip(
                    icon = Icons.Outlined.CheckCircle,
                    iconTint = AppTheme.theme.colors.testGreen,
                    text = viewModel.model.passed.toString()
                )

                Spacer(modifier = Modifier.width(SmallMargin))

                Chip(
                    icon = Icons.Outlined.ErrorOutline,
                    iconTint = AppTheme.theme.colors.testRed,
                    text = viewModel.model.failed.toString()
                )
            }
        }
    }
}

@Composable
private fun Chip(
    icon: ImageVector,
    iconTint: Color,
    text: String
) {
    Card(
        shape = RoundedCornerShape(size = CardCornerSize),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.theme.colors.secondaryCardBackground
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = QuarterMargin, vertical = TinyMargin)
        ) {
            Icon(
                imageVector = icon,
                tint = iconTint,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
            )

            Text(
                text = text,
                style = AppTheme.theme.typography.bodyMedium
            )
        }
    }
}

@Composable
@Preview
fun FlowStatCellLightPreview() {
    ThemedPreview(theme = LightTheme) {
        FlowStatsCell(newFlowStatCellViewModel())
    }
}

fun newFlowStatCellViewModel(): FlowStatsCellViewModel {
    return FlowStatsCellViewModel(
        model = FlowStatsCellModel(
            id = "id",
            total = 98,
            passed = 40,
            failed = 48,
            rate = 40
        )
    )
}