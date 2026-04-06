package me.diamondforge.tokn.domain.usecase

import me.diamondforge.tokn.domain.repository.AccountRepository

class IncrementHotpCounterUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(id: Long) = repository.incrementCounter(id)
}
