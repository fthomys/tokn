package me.diamondforge.tokn.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LockManager @Inject constructor() {
    // null = auth check not yet completed, true = locked, false = unlocked
    private val _isLocked = MutableStateFlow<Boolean?>(null)
    val isLocked: StateFlow<Boolean?> = _isLocked.asStateFlow()

    @Volatile private var backgroundedAt: Long = -1L
    @Volatile private var skipNextForeground: Boolean = false

    /** Call before launching any system UI (file picker, share sheet, etc.) */
    fun suppressNextForeground() {
        skipNextForeground = true
    }

    fun onAppBackground() {
        backgroundedAt = System.currentTimeMillis()
    }

    fun onAppForeground(timeoutSeconds: Int) {
        if (backgroundedAt < 0) return
        if (skipNextForeground) {
            skipNextForeground = false
            backgroundedAt = -1L
            return
        }
        val elapsed = (System.currentTimeMillis() - backgroundedAt) / 1000
        if (timeoutSeconds == 0 || elapsed >= timeoutSeconds) {
            _isLocked.value = true
        }
    }

    fun unlock() {
        _isLocked.value = false
        backgroundedAt = -1L
    }

    fun lock() {
        _isLocked.value = true
    }
}
