package me.diamondforge.simpleotp

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LockManager @Inject constructor() {
    private val _isLocked = MutableStateFlow(true)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    @Volatile private var backgroundedAt: Long = -1L

    fun onAppBackground() {
        backgroundedAt = System.currentTimeMillis()
    }

    fun onAppForeground(timeoutSeconds: Int) {
        if (backgroundedAt < 0) return
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
