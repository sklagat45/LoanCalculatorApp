package com.srklagat.loancalculatorapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for loan calculations.
 * Provides reactive Flow-based queries for observing saved calculations.
 */
@Dao
interface LoanCalculationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(calculation: LoanCalculationEntity): Long

    @Query("SELECT * FROM loan_calculations ORDER BY createdAt DESC")
    fun getAllCalculations(): Flow<List<LoanCalculationEntity>>

    @Query("SELECT * FROM loan_calculations WHERE id = :id")
    suspend fun getCalculationById(id: Long): LoanCalculationEntity?

    @Delete
    suspend fun delete(calculation: LoanCalculationEntity)

    @Query("DELETE FROM loan_calculations")
    suspend fun deleteAll()

    @Query("SELECT * FROM loan_calculations ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestCalculation(): LoanCalculationEntity?

    @Query("SELECT COUNT(*) FROM loan_calculations")
    suspend fun getLoanCount(): Int
}
