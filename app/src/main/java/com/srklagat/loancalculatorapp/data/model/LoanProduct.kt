package com.srklagat.loancalculatorapp.data.model

import androidx.annotation.DrawableRes
import com.srklagat.loancalculatorapp.R

/**
 * Represents a loan product available for application.
 * Contains metadata about the loan type, interest rates, and limits.
 */
data class LoanProduct(
    val id: String,
    val name: String,
    val description: String,
    val annualInterestRate: Double,
    val maxAmount: Double,
    val minAmount: Double,
    val maxTenureMonths: Int,
    val minTenureMonths: Int,
    @param:DrawableRes val imageRes: Int
) {
    companion object {
        /**
         * Sample loan products matching the design mockups.
         */
        fun getSampleProducts(): List<LoanProduct> = listOf(
            LoanProduct(
                id = "salary_e_loan",
                name = "Salary E-Loan",
                description = "Get quick loans to boost your income",
                annualInterestRate = 15.0,
                maxAmount = 500000.0,
                minAmount = 1000.0,
                maxTenureMonths = 36,
                minTenureMonths = 1,
                imageRes = R.drawable.loans
            ),
            LoanProduct(
                id = "buy_now_pay_later",
                name = "Buy Now Pay Later",
                description = "Buy goods today, pay later",
                annualInterestRate = 12.0,
                maxAmount = 200000.0,
                minAmount = 500.0,
                maxTenureMonths = 12,
                minTenureMonths = 1,
                imageRes = R.drawable.bnml
            ),
            LoanProduct(
                id = "stock_loan",
                name = "Stock Loan",
                description = "Boost your business stock today",
                annualInterestRate = 18.0,
                maxAmount = 1000000.0,
                minAmount = 5000.0,
                maxTenureMonths = 60,
                minTenureMonths = 3,
                imageRes = R.drawable.stock_finance
            )
        )
    }
}
