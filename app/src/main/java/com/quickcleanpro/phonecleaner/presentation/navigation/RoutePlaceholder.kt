package com.quickcleanpro.phonecleaner.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.quickcleanpro.phonecleaner.R

internal fun NavGraphBuilder.registerPlaceholderRoutes(
    navController: NavHostController,
    screens: List<Screen>,
) {
    screens.forEach { screen ->
        composable(screen.route) {
            RoutePlaceholderScreen(
                screen = screen,
                onBack = { navController.safePopBackStack() },
                onHome = { navController.navigateHomeClearingStack() },
            )
        }
    }
}

@Composable
private fun RoutePlaceholderScreen(
    screen: Screen,
    onBack: () -> Unit,
    onHome: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(screen.titleRes),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = screen.route,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack) {
                Text(text = stringResource(R.string.back))
            }
            Button(onClick = onHome) {
                Text(text = stringResource(R.string.nav_home))
            }
        }
    }
}
