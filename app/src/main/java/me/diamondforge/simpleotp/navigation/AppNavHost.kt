package me.diamondforge.simpleotp.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import me.diamondforge.simpleotp.add.AddAccountViewModel
import me.diamondforge.simpleotp.add.FromImageScreen
import me.diamondforge.simpleotp.add.ManualEntryScreen
import me.diamondforge.simpleotp.add.QrScannerScreen
import me.diamondforge.simpleotp.backup.BackupScreen
import me.diamondforge.simpleotp.home.HomeScreen
import me.diamondforge.simpleotp.settings.SettingsScreen

@Composable
fun AppNavHost(
    isLocked: Boolean,
    onUnlock: () -> Unit,
) {
    if (isLocked) {
        LockScreen(onUnlock = onUnlock)
        return
    }

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onScanQr = { navController.navigate(Screen.AddFlow.route) },
                onFromImage = { navController.navigate(Screen.FromImage.route) },
                onManualEntry = { navController.navigate(Screen.ManualEntry.route) },
                onSettings = { navController.navigate(Screen.Settings.route) },
                onBackup = { navController.navigate(Screen.Backup.route) },
            )
        }

        navigation(
            startDestination = Screen.QrScanner.route,
            route = Screen.AddFlow.route,
        ) {
            composable(Screen.QrScanner.route) { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry(Screen.AddFlow.route)
                }
                val viewModel: AddAccountViewModel = hiltViewModel(parentEntry)
                QrScannerScreen(
                    onScanned = { rawValue ->
                        viewModel.onQrScanned(rawValue)
                        navController.navigate(Screen.ManualEntry.route)
                    },
                    onManualEntry = { navController.navigate(Screen.ManualEntry.route) },
                    onBack = { navController.popBackStack(Screen.Home.route, inclusive = false) },
                )
            }
            composable(Screen.FromImage.route) { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry(Screen.AddFlow.route)
                }
                val viewModel: AddAccountViewModel = hiltViewModel(parentEntry)
                FromImageScreen(
                    onScanned = { rawValue ->
                        viewModel.onQrScanned(rawValue)
                        navController.navigate(Screen.ManualEntry.route)
                    },
                    onManualEntry = { navController.navigate(Screen.ManualEntry.route) },
                    onBack = { navController.popBackStack(Screen.Home.route, inclusive = false) },
                )
            }
            composable(Screen.ManualEntry.route) { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry(Screen.AddFlow.route)
                }
                val viewModel: AddAccountViewModel = hiltViewModel(parentEntry)
                ManualEntryScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = {
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                    },
                    viewModel = viewModel,
                )
            }
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onBackup = { navController.navigate(Screen.Backup.route) },
            )
        }
        composable(Screen.Backup.route) {
            BackupScreen(onBack = { navController.popBackStack() })
        }
    }
}

@Composable
private fun LockScreen(onUnlock: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "SimpleOTP",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object AddFlow : Screen("add_flow")
    data object QrScanner : Screen("qr_scanner")
    data object FromImage : Screen("from_image")
    data object ManualEntry : Screen("manual_entry")
    data object Settings : Screen("settings")
    data object Backup : Screen("backup")
}
