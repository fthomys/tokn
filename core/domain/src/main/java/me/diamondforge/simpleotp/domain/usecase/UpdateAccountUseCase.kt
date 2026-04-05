package me.diamondforge.simpleotp.domain.usecase

import me.diamondforge.simpleotp.domain.model.OtpAccount
import me.diamondforge.simpleotp.domain.repository.AccountRepository

class UpdateAccountUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(account: OtpAccount) = repository.updateAccount(account)
}
