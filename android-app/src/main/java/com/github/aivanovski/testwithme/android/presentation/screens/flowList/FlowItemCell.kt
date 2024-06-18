package com.github.aivanovski.testwithme.android.presentation.screens.flowList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.aivanovski.testwithme.android.presentation.core.compose.ThemedPreview
import com.github.aivanovski.testwithme.android.presentation.core.compose.rememberOnClickedCallback
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.LightTheme
import com.github.aivanovski.testwithme.android.presentation.screens.flowList.model.FlowItem
import com.github.aivanovski.testwithme.android.presentation.screens.flowList.model.FlowListIntent

@Composable
fun FlowItemCell(
    item: FlowItem,
    onIntent: (intent: FlowListIntent) -> Unit
) {
    val onClick = rememberOnClickedCallback {
        onIntent.invoke(FlowListIntent.OnFlowClicked(item.uid))
    }

    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
    ) {
        Text(
            text = item.name,
        )
    }
}

@Preview
@Composable
fun FlowItemLightPreview() {
    ThemedPreview(theme = LightTheme) {
        FlowItemCell(
            item = newItem(),
            onIntent = {}
        )
    }
}

private fun newItem(): FlowItem {
    return FlowItem(
        uid = "uid",
        name = "login-flow.yaml"
    )
}