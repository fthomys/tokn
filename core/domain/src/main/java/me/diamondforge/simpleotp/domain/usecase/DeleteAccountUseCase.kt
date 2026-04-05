package me.diamondforge.simpleotp.domain.usecase

import me.diamondforge.simpleotp.domain.repository.AccountRepository

class DeleteAccountUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteAccount(id)
}
