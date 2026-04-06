package me.diamondforge.tokn.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import me.diamondforge.tokn.add.AddAccountViewModel
import me.diamondforge.tokn.add.FromImageScreen
import me.diamondforge.tokn.add.ManualEntryScreen
import me.diamondforge.tokn.add.QrScannerScreen
import me.diamondforge.tokn.backup.BackupScreen
import me.diamondforge.tokn.home.EditAccountScreen
import me.diamondforge.tokn.home.HomeScreen
import me.diamondforge.tokn.settings.AppearanceScreen
import me.diamondforge.tokn.settings.SecurityScreen
import me.diamondforge.tokn.settings.SettingsScreen

@Composable
fun AppNavHost(
    isLocked: Boolean?,
    onUnlock: () -> Unit,
) {
    if (isLocked == null) return  // auth check not done yet — show nothing
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
                onEditAccount = { id -> navController.navigate(Screen.EditAccount.createRoute(id)) },
            )
        }
        composable(
            route = Screen.EditAccount.route,
            arguments = listOf(navArgument("accountId") { type = NavType.LongType }),
        ) {
            EditAccountScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
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
                    suppressLock = viewModel::suppressLock,
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
                onAppearance = { navController.navigate(Screen.Appearance.route) },
                onSecurity = { navController.navigate(Screen.SecuritySettings.route) },
                onBackup = { navController.navigate(Screen.Backup.route) },
            )
        }
        composable(Screen.Appearance.route) {
            AppearanceScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.SecuritySettings.route) {
            SecurityScreen(onBack = { navController.popBackStack() })
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
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(88.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "Tokn",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "App is locked",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onUnlock) {
                Text("Unlock")
            }
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
    data object Appearance : Screen("appearance")
    data object SecuritySettings : Screen("security_settings")
    data object Backup : Screen("backup")
    data object EditAccount : Screen("edit/{accountId}") {
        fun createRoute(accountId: Long) = "edit/$accountId"
    }
}
