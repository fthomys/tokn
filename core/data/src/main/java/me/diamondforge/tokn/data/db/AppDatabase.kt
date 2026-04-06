package me.diamondforge.tokn.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import me.diamondforge.tokn.data.db.dao.OtpAccountDao
import me.diamondforge.tokn.data.db.entity.OtpAccountEntity

@Database(
    entities = [OtpAccountEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun otpAccountDao(): OtpAccountDao
}
