package com.quickcleanpro.phonecleaner.presentation.screen.applock.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXPillShape
import com.quickcleanpro.phonecleaner.presentation.screen.applock.AppLockUiState

@Composable
internal fun AppLockSearchView(
    uiState: AppLockUiState,
    onSearch: (String) -> Unit,
    onTogglePackage: (String) -> Unit
) {
    val query = uiState.searchQuery.trim()
    val visibleApps = remember(uiState.apps, query) {
        if (query.isBlank()) {
            emptyList()
        } else {
            uiState.apps.filter { app -> app.appName.contains(query, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        SearchInput(
            query = uiState.searchQuery,
            onSearch = onSearch
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            when {
                query.isBlank() -> Unit
                visibleApps.isEmpty() -> item { EmptyCard(text = stringResource(R.string.app_lock_no_search_results)) }
                else -> item {
                    AppLockCard {
                        AppLockRows(
                            apps = visibleApps,
                            onTogglePackage = onTogglePackage,
                            showDividers = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchInput(
    query: String,
    onSearch: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onSearch,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        singleLine = true,
        leadingIcon = {
            Image(
                painter = painterResource(R.mipmap.ic_ser),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                Image(
                    painter = painterResource(R.mipmap.ic_close),
                    contentDescription = stringResource(R.string.delete),
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CleanXPillShape)
                        .clickable { onSearch("") }
                )
            }
        },
        placeholder = {
            Text(
                text = stringResource(R.string.please_enter_app_name),
                color = AppLockPlaceholderText,
                fontSize = 18.sp
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        textStyle = androidx.compose.ui.text.TextStyle(
            color = AppLockNavy,
            fontSize = 18.sp
        ),
        shape = RoundedCornerShape(AppLockCardRadius),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = AppLockCardColor,
            unfocusedContainerColor = AppLockCardColor,
            disabledContainerColor = AppLockCardColor,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = AppLockNavy,
            unfocusedTextColor = AppLockNavy,
            cursorColor = CleanXBlue
        )
    )
}
