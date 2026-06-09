package com.srklagat.loancalculatorapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database for the Loan Calculator app.
 * Uses singleton pattern to prevent multiple database instances.
 */
@Database(
    entities = [LoanCalculationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LoanDatabase : RoomDatabase() {

    abstract fun loanCalculationDao(): LoanCalculationDao

    companion object {
        @Volatile
        private var INSTANCE: LoanDatabase? = null

        fun getDatabase(context: Context): LoanDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LoanDatabase::class.java,
                    "loan_calculator_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
