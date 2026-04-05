package me.diamondforge.simpleotp.data.repository

import me.diamondforge.simpleotp.data.db.dao.OtpAccountDao
import me.diamondforge.simpleotp.data.db.entity.toDomain
import me.diamondforge.simpleotp.data.db.entity.toEntity
import me.diamondforge.simpleotp.domain.model.OtpAccount
import me.diamondforge.simpleotp.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val dao: OtpAccountDao,
) : AccountRepository {

    override fun getAccounts(): Flow<List<OtpAccount>> =
        dao.getAllAccounts().map { list -> list.map { it.toDomain() } }

    override suspend fun addAccount(account: OtpAccount): Long =
        dao.insert(account.toEntity())

    override suspend fun updateAccount(account: OtpAccount) =
        dao.update(account.toEntity())

    override suspend fun deleteAccount(id: Long) =
        dao.deleteById(id)

    override suspend fun reorderAccounts(accounts: List<OtpAccount>) {
        accounts.forEachIndexed { index, account ->
            dao.updateSortOrder(account.id, index)
        }
    }

    override suspend fun incrementCounter(id: Long) =
        dao.incrementCounter(id)

    override suspend fun getAccountById(id: Long): OtpAccount? =
        dao.getAccountById(id)?.toDomain()
}
