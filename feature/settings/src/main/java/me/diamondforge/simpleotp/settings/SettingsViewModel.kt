package me.diamondforge.simpleotp.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: UserPreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferences.themeMode,
        preferences.autoLockTimeoutSeconds,
        preferences.biometricEnabled,
        preferences.screenshotsEnabled,
    ) { theme, timeout, biometric, screenshots ->
        SettingsUiState(
            themeMode = theme,
            autoLockTimeoutSeconds = timeout,
            biometricEnabled = biometric,
            screenshotsEnabled = screenshots,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferences.setThemeMode(mode) }
    }

    fun setAutoLockTimeout(seconds: Int) {
        viewModelScope.launch { preferences.setAutoLockTimeout(seconds) }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setBiometricEnabled(enabled) }
    }

    fun setScreenshotsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setScreenshotsEnabled(enabled) }
    }
}

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val autoLockTimeoutSeconds: Int = 60,
    val biometricEnabled: Boolean = true,
    val screenshotsEnabled: Boolean = false,
)
