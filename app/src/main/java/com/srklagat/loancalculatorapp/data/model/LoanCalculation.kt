package com.srklagat.loancalculatorapp.data.model

/**
 * Represents a single row in the amortization schedule.
 */
data class AmortizationEntry(
    val paymentNumber: Int,
    val emi: Double,
    val interestComponent: Double,
    val principalComponent: Double,
    val remainingBalance: Double
)

/**
 * Holds the complete result of a loan EMI calculation,
 * including the amortization schedule breakdown.
 */
data class LoanCalculationResult(
    val principal: Double,
    val annualInterestRate: Double,
    val tenureMonths: Int,
    val monthlyEmi: Double,
    val totalInterest: Double,
    val totalAmount: Double,
    val schedule: List<AmortizationEntry>
)
