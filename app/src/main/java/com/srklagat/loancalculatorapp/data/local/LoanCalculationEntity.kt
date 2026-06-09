package com.srklagat.loancalculatorapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a saved loan calculation.
 * Stores all inputs and computed results for retrieval.
 */
@Entity(tableName = "loan_calculations")
data class LoanCalculationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val loanType: String,
    val principal: Double,
    val annualInterestRate: Double,
    val tenureMonths: Int,
    val monthlyEmi: Double,
    val totalInterest: Double,
    val totalAmount: Double,
    val disbursementAccount: String,
    val createdAt: Long = System.currentTimeMillis()
)
