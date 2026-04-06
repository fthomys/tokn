package me.diamondforge.tokn.domain.usecase

import me.diamondforge.tokn.domain.model.OtpAccount
import me.diamondforge.tokn.domain.repository.AccountRepository

class UpdateAccountUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(account: OtpAccount) = repository.updateAccount(account)
}
