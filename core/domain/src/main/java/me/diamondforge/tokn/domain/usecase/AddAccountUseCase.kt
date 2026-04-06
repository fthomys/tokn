package me.diamondforge.tokn.domain.usecase

import me.diamondforge.tokn.domain.model.OtpAccount
import me.diamondforge.tokn.domain.repository.AccountRepository

class AddAccountUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(account: OtpAccount): Long = repository.addAccount(account)
}
