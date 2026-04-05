package me.diamondforge.simpleotp.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import me.diamondforge.simpleotp.domain.model.OtpAccount
import me.diamondforge.simpleotp.domain.model.OtpAlgorithm
import me.diamondforge.simpleotp.domain.model.OtpType
import me.diamondforge.simpleotp.domain.usecase.AddAccountUseCase
import me.diamondforge.simpleotp.domain.usecase.GetAccountsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val addAccountUseCase: AddAccountUseCase,
    private val encryptedBackupManager: EncryptedBackupManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun exportBackup(uri: Uri, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                withContext(Dispatchers.IO) {
                    val accounts = getAccountsUseCase().first()
                    val json = serializeAccounts(accounts)
                    encryptedBackupManager.exportToUri(context, uri, json, password)
                }
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, exportSuccess = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun importBackup(uri: Uri, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                withContext(Dispatchers.IO) {
                    val json = encryptedBackupManager.importFromUri(context, uri, password)
                    val accounts = deserializeAccounts(json)
                    accounts.forEach { addAccountUseCase(it) }
                    accounts.size
                }
            }.onSuccess { count ->
                _uiState.update { it.copy(isLoading = false, importedCount = count) }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = if (e is javax.crypto.AEADBadTagException || e is javax.crypto.BadPaddingException)
                            "Wrong password"
                        else
                            e.message,
                    )
                }
            }
        }
    }

    fun clearMessages() = _uiState.update { it.copy(error = null, exportSuccess = false, importedCount = null) }

    private fun serializeAccounts(accounts: List<OtpAccount>): String {
        val array = JSONArray()
        accounts.forEach { account ->
            array.put(
                JSONObject().apply {
                    put("issuer", account.issuer)
                    put("accountName", account.accountName)
                    put("secret", account.secret)
                    put("algorithm", account.algorithm.name)
                    put("digits", account.digits)
                    put("period", account.period)
                    put("counter", account.counter)
                    put("type", account.type.name)
                    put("sortOrder", account.sortOrder)
                },
            )
        }
        return JSONObject().apply { put("accounts", array); put("version", 1) }.toString()
    }

    private fun deserializeAccounts(json: String): List<OtpAccount> {
        val root = JSONObject(json)
        val array = root.getJSONArray("accounts")
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            OtpAccount(
                issuer = obj.getString("issuer"),
                accountName = obj.getString("accountName"),
                secret = obj.getString("secret"),
                algorithm = OtpAlgorithm.valueOf(obj.optString("algorithm", "SHA1")),
                digits = obj.optInt("digits", 6),
                period = obj.optInt("period", 30),
                counter = obj.optLong("counter", 0),
                type = OtpType.valueOf(obj.optString("type", "TOTP")),
                sortOrder = obj.optInt("sortOrder", 0),
            )
        }
    }
}

data class BackupUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val exportSuccess: Boolean = false,
    val importedCount: Int? = null,
)
