package com.github.aivanovski.testwithme.android.presentation.core.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.aivanovski.testwithme.android.R
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.AppTheme
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.LightTheme

@Composable
fun ErrorMessage(message: String) {
    Text(
        text = message,
        fontSize = 24.sp,
        textAlign = TextAlign.Center,
        color = AppTheme.theme.materialColors.error,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
    )
}

@Preview
@Composable
fun ErrorMessageLightPreview() {
    ThemedPreview(theme = LightTheme) {
        ErrorMessage(message = stringResource(R.string.error_has_been_occurred))
    }
}