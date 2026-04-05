package me.diamondforge.simpleotp.data.di

import android.content.Context
import androidx.room.Room
import me.diamondforge.simpleotp.data.db.AppDatabase
import me.diamondforge.simpleotp.data.db.dao.OtpAccountDao
import me.diamondforge.simpleotp.data.repository.AccountRepositoryImpl
import me.diamondforge.simpleotp.domain.repository.AccountRepository
import me.diamondforge.simpleotp.domain.usecase.AddAccountUseCase
import me.diamondforge.simpleotp.domain.usecase.DeleteAccountUseCase
import me.diamondforge.simpleotp.domain.usecase.GenerateOtpUseCase
import me.diamondforge.simpleotp.domain.usecase.GetAccountsUseCase
import me.diamondforge.simpleotp.domain.usecase.ReorderAccountsUseCase
import me.diamondforge.simpleotp.domain.usecase.UpdateAccountUseCase
import me.diamondforge.simpleotp.security.KeystoreManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        keystoreManager: KeystoreManager,
    ): AppDatabase {
        System.loadLibrary("sqlcipher")
        val passphrase = keystoreManager.getDatabasePassphrase()
        val factory = SupportOpenHelperFactory(passphrase)
        return Room.databaseBuilder(context, AppDatabase::class.java, "otp_database")
            .openHelperFactory(factory)
            .build()
    }

    @Provides
    fun provideOtpAccountDao(db: AppDatabase): OtpAccountDao = db.otpAccountDao()

    @Provides
    fun provideGetAccountsUseCase(repo: AccountRepository) = GetAccountsUseCase(repo)

    @Provides
    fun provideAddAccountUseCase(repo: AccountRepository) = AddAccountUseCase(repo)

    @Provides
    fun provideDeleteAccountUseCase(repo: AccountRepository) = DeleteAccountUseCase(repo)

    @Provides
    fun provideUpdateAccountUseCase(repo: AccountRepository) = UpdateAccountUseCase(repo)

    @Provides
    fun provideReorderAccountsUseCase(repo: AccountRepository) = ReorderAccountsUseCase(repo)

    @Provides
    fun provideGenerateOtpUseCase() = GenerateOtpUseCase()
}

@Module
@InstallIn(SingletonComponent::class)
interface DataBindsModule {
    @Binds
    @Singleton
    fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository
}
