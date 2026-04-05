package me.diamondforge.simpleotp.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onBackup: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showScreenshotWarning by remember { mutableStateOf(false) }

    if (showScreenshotWarning) {
        AlertDialog(
            onDismissRequest = { showScreenshotWarning = false },
            title = { Text(stringResource(R.string.screenshot_warning_title)) },
            text = { Text(stringResource(R.string.screenshot_warning_body)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setScreenshotsEnabled(true)
                    showScreenshotWarning = false
                }) {
                    Text(
                        text = stringResource(R.string.enable_anyway),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showScreenshotWarning = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionHeader(stringResource(R.string.appearance))

            ListItem(
                headlineContent = { Text(stringResource(R.string.theme)) },
                leadingContent = {
                    Icon(Icons.Default.Palette, contentDescription = null)
                },
                supportingContent = {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = uiState.themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                                label = {
                                    Text(
                                        when (mode) {
                                            ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                                            ThemeMode.DARK -> stringResource(R.string.theme_dark)
                                            ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                                        },
                                    )
                                },
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                    }
                },
            )

            HorizontalDivider()
            SectionHeader(stringResource(R.string.security))

            ListItem(
                headlineContent = { Text(stringResource(R.string.biometric_unlock)) },
                leadingContent = {
                    Icon(Icons.Default.Fingerprint, contentDescription = null)
                },
                trailingContent = {
                    Switch(
                        checked = uiState.biometricEnabled,
                        onCheckedChange = viewModel::setBiometricEnabled,
                    )
                },
            )

            ListItem(
                headlineContent = { Text(stringResource(R.string.screenshot_protection)) },
                supportingContent = { Text(stringResource(R.string.screenshot_protection_desc)) },
                leadingContent = {
                    Icon(Icons.Default.Screenshot, contentDescription = null)
                },
                trailingContent = {
                    Switch(
                        checked = !uiState.screenshotsEnabled,
                        onCheckedChange = { protectionOn ->
                            if (!protectionOn) {
                                showScreenshotWarning = true
                            } else {
                                viewModel.setScreenshotsEnabled(false)
                            }
                        },
                    )
                },
            )

            ListItem(
                headlineContent = { Text(stringResource(R.string.auto_lock)) },
                leadingContent = {
                    Icon(Icons.Default.Lock, contentDescription = null)
                },
                supportingContent = {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        listOf(
                            30 to stringResource(R.string.lock_30s),
                            60 to stringResource(R.string.lock_1m),
                            300 to stringResource(R.string.lock_5m),
                            0 to stringResource(R.string.lock_immediately),
                        ).forEach { (seconds, label) ->
                            FilterChip(
                                selected = uiState.autoLockTimeoutSeconds == seconds,
                                onClick = { viewModel.setAutoLockTimeout(seconds) },
                                label = { Text(label) },
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                    }
                },
            )

            HorizontalDivider()
            SectionHeader(stringResource(R.string.data))

            ListItem(
                headlineContent = { Text(stringResource(R.string.backup_restore)) },
                leadingContent = {
                    Icon(Icons.Default.Backup, contentDescription = null)
                },
                trailingContent = {
                    TextButton(onClick = onBackup) {
                        Text(stringResource(R.string.open))
                    }
                },
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
