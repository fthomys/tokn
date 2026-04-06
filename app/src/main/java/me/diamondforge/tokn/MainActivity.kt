package me.diamondforge.tokn

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.diamondforge.tokn.navigation.AppNavHost
import me.diamondforge.tokn.security.BiometricHelper
import me.diamondforge.tokn.security.LockManager
import me.diamondforge.tokn.security.VaultPasswordManager
import me.diamondforge.tokn.settings.ThemeMode
import me.diamondforge.tokn.settings.UserPreferencesRepository
import me.diamondforge.tokn.ui.theme.SimpleOTPTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var lockManager: LockManager
    @Inject lateinit var biometricHelper: BiometricHelper
    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository
    @Inject lateinit var vaultPasswordManager: VaultPasswordManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()

        setContent {
            val themeMode by userPreferencesRepository.themeMode.collectAsStateWithLifecycle(ThemeMode.SYSTEM)
            val isLocked by lockManager.isLocked.collectAsStateWithLifecycle()
            val screenshotsEnabled by userPreferencesRepository.screenshotsEnabled.collectAsStateWithLifecycle(false)
            val encryptionEnabled by userPreferencesRepository.encryptionEnabled.collectAsStateWithLifecycle(false)
            val biometricEnabled by userPreferencesRepository.biometricEnabled.collectAsStateWithLifecycle(true)

            LaunchedEffect(screenshotsEnabled) {
                if (screenshotsEnabled) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

            val hasVaultPassword = encryptionEnabled && vaultPasswordManager.hasPassword()

            SimpleOTPTheme(themeMode = themeMode) {
                AppNavHost(
                    isLocked = isLocked,
                    onUnlock = { requestBiometric() },
                    onUnlockWithPassword = { password ->
                        withContext(Dispatchers.IO) {
                            if (vaultPasswordManager.verify(password)) {
                                withContext(Dispatchers.Main) { lockManager.unlock() }
                                true
                            } else {
                                false
                            }
                        }
                    },
                    hasVaultPassword = hasVaultPassword,
                    biometricEnabled = biometricEnabled,
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            val encryptionEnabled = userPreferencesRepository.encryptionEnabled.first()
            if (!encryptionEnabled) {
                lockManager.unlock()
                return@launch
            }
            val timeout = userPreferencesRepository.autoLockTimeoutSeconds.first()
            lockManager.onAppForeground(timeout)
            if (lockManager.isLocked.value != false) {
                requestBiometric()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        lockManager.onAppBackground()
    }

    private fun requestBiometric() {
        lifecycleScope.launch {
            val biometricEnabled = userPreferencesRepository.biometricEnabled.first()
            if (!biometricEnabled || !biometricHelper.isAvailable()) {
                lockManager.lock()
                return@launch
            }
            lockManager.lock()
            biometricHelper.authenticate(
                activity = this@MainActivity,
                title = getString(R.string.biometric_prompt_title),
                subtitle = getString(R.string.biometric_prompt_subtitle),
                onSuccess = { lockManager.unlock() },
                onError = { _, _ -> },
                onFailed = { },
            )
        }
    }
}
