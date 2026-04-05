package me.diamondforge.simpleotp.domain.usecase

import me.diamondforge.simpleotp.domain.model.OtpAccount
import me.diamondforge.simpleotp.domain.repository.AccountRepository

class AddAccountUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(account: OtpAccount): Long = repository.addAccount(account)
}
