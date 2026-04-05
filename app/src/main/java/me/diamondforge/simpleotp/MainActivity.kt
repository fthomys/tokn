package me.diamondforge.simpleotp

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import me.diamondforge.simpleotp.navigation.AppNavHost
import me.diamondforge.simpleotp.security.BiometricHelper
import me.diamondforge.simpleotp.settings.ThemeMode
import me.diamondforge.simpleotp.settings.UserPreferencesRepository
import me.diamondforge.simpleotp.ui.theme.SimpleOTPTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var lockManager: LockManager
    @Inject lateinit var biometricHelper: BiometricHelper
    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()

        setContent {
            val themeMode by userPreferencesRepository.themeMode.collectAsStateWithLifecycle(ThemeMode.SYSTEM)
            val isLocked by lockManager.isLocked.collectAsStateWithLifecycle()
            val screenshotsEnabled by userPreferencesRepository.screenshotsEnabled.collectAsStateWithLifecycle(false)

            LaunchedEffect(screenshotsEnabled) {
                if (screenshotsEnabled) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

            SimpleOTPTheme(themeMode = themeMode) {
                AppNavHost(
                    isLocked = isLocked,
                    onUnlock = { requestBiometric() },
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            val timeout = userPreferencesRepository.autoLockTimeoutSeconds.first()
            lockManager.onAppForeground(timeout)
            if (lockManager.isLocked.value) {
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
                lockManager.unlock()
                return@launch
            }
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
