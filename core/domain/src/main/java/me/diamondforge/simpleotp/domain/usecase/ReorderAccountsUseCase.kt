package me.diamondforge.simpleotp.domain.usecase

import me.diamondforge.simpleotp.domain.model.OtpAccount
import me.diamondforge.simpleotp.domain.repository.AccountRepository

class ReorderAccountsUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(accounts: List<OtpAccount>) = repository.reorderAccounts(accounts)
}
