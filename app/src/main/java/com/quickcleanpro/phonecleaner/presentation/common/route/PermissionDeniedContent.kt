package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quickcleanpro.phonecleaner.R

@Composable
internal fun FilePermissionDeniedContent(
    @StringRes titleRes: Int,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    PermissionDeniedContent(
        titleRes = titleRes,
        onBack = onBack,
        onRetry = onRetry,
    )
}

@Composable
internal fun PermissionDeniedContent(
    @StringRes titleRes: Int,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F3F7))
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = Color(0xFF1D2959),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.permission_storage_desc),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Color(0xFF8190A5),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.grant))
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(onClick = onBack) {
            Text(text = stringResource(R.string.back))
        }
    }
}
