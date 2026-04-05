package me.diamondforge.simpleotp.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import me.diamondforge.simpleotp.domain.model.OtpAccount
import me.diamondforge.simpleotp.domain.usecase.DeleteAccountUseCase
import me.diamondforge.simpleotp.domain.usecase.GenerateOtpUseCase
import me.diamondforge.simpleotp.domain.usecase.GetAccountsUseCase
import me.diamondforge.simpleotp.domain.usecase.OtpResult
import me.diamondforge.simpleotp.domain.usecase.ReorderAccountsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val reorderAccountsUseCase: ReorderAccountsUseCase,
    private val generateOtpUseCase: GenerateOtpUseCase,
) : ViewModel() {

    private val _currentTimeMillis = MutableStateFlow(System.currentTimeMillis())
    private val _accounts = getAccountsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<HomeUiState> = combine(_accounts, _currentTimeMillis) { accounts, time ->
        HomeUiState(
            items = accounts.map { account ->
                val result = generateOtpUseCase(account, time)
                AccountItem(account = account, otpResult = result)
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init {
        viewModelScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                _currentTimeMillis.value = now
                delay(1_000 - (now % 1_000))
            }
        }
    }

    fun deleteAccount(id: Long) {
        viewModelScope.launch { deleteAccountUseCase(id) }
    }

    fun reorderAccounts(accounts: List<OtpAccount>) {
        viewModelScope.launch { reorderAccountsUseCase(accounts) }
    }
}

data class HomeUiState(
    val items: List<AccountItem> = emptyList(),
)

data class AccountItem(
    val account: OtpAccount,
    val otpResult: OtpResult,
)
