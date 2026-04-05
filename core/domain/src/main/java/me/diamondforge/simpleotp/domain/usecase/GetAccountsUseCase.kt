package me.diamondforge.simpleotp.domain.usecase

import me.diamondforge.simpleotp.domain.model.OtpAccount
import me.diamondforge.simpleotp.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow

class GetAccountsUseCase(private val repository: AccountRepository) {
    operator fun invoke(): Flow<List<OtpAccount>> = repository.getAccounts()
}
