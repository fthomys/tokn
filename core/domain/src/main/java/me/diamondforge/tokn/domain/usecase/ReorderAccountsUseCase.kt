package me.diamondforge.tokn.domain.usecase

import me.diamondforge.tokn.domain.model.OtpAccount
import me.diamondforge.tokn.domain.repository.AccountRepository

class ReorderAccountsUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(accounts: List<OtpAccount>) = repository.reorderAccounts(accounts)
}
