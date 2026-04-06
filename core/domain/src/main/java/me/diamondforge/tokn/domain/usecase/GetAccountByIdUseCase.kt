package me.diamondforge.tokn.domain.usecase

import me.diamondforge.tokn.domain.repository.AccountRepository

class GetAccountByIdUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(id: Long) = repository.getAccountById(id)
}
