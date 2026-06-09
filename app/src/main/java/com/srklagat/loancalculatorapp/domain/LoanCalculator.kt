package com.srklagat.loancalculatorapp.domain

import com.srklagat.loancalculatorapp.data.model.AmortizationEntry
import com.srklagat.loancalculatorapp.data.model.LoanCalculationResult
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.pow

/**
 * Core loan calculation engine providing EMI computation and
 * amortization schedule generation.
 *
 * Uses BigDecimal for financial precision with proper rounding
 * to avoid floating-point errors in monetary calculations.
 */
object LoanCalculator {

    private val mc = MathContext(10, RoundingMode.HALF_UP)
    private const val SCALE = 2

    /**
     * Calculates the Equated Monthly Installment (EMI) using the standard formula:
     *
     * EMI = P × r × (1+r)^n / ((1+r)^n - 1)
     *
     * Where:
     * - P = Principal loan amount
     * - r = Monthly interest rate (annual rate / 12 / 100)
     * - n = Number of monthly installments (tenure in months)
     *
     * Edge case: When interest rate is 0%, EMI = P / n
     *
     * @param principal The loan amount
     * @param annualInterestRate Annual interest rate as percentage (e.g. 15.0 for 15%)
     * @param tenureMonths Number of months for repayment
     * @return Complete [LoanCalculationResult] with EMI, totals, and amortization schedule
     */
    fun calculate(
        principal: Double,
        annualInterestRate: Double,
        tenureMonths: Int
    ): LoanCalculationResult {
        require(principal > 0) { "Principal must be positive" }
        require(tenureMonths > 0) { "Tenure must be at least 1 month" }
        require(annualInterestRate >= 0) { "Interest rate cannot be negative" }

        // Handle 0% interest edge case
        if (annualInterestRate == 0.0) {
            return calculateZeroInterest(principal, tenureMonths)
        }

        val p = BigDecimal(principal.toString())
        val monthlyRate = BigDecimal(annualInterestRate.toString())
            .divide(BigDecimal("1200"), mc) // annual% -> monthly decimal

        // (1 + r)^n
        val onePlusR = BigDecimal.ONE.add(monthlyRate)
        val onePlusRPowN = BigDecimal(onePlusR.toDouble().pow(tenureMonths).toString())

        // EMI = P × r × (1+r)^n / ((1+r)^n - 1)
        val numerator = p.multiply(monthlyRate, mc).multiply(onePlusRPowN, mc)
        val denominator = onePlusRPowN.subtract(BigDecimal.ONE)
        val emi = numerator.divide(denominator, SCALE, RoundingMode.HALF_UP)

        // Generate amortization schedule
        val schedule = generateSchedule(p, monthlyRate, emi, tenureMonths)

        val totalAmount = emi.multiply(BigDecimal(tenureMonths))
            .setScale(SCALE, RoundingMode.HALF_UP)
        val totalInterest = totalAmount.subtract(p)
            .setScale(SCALE, RoundingMode.HALF_UP)

        return LoanCalculationResult(
            principal = principal,
            annualInterestRate = annualInterestRate,
            tenureMonths = tenureMonths,
            monthlyEmi = emi.toDouble(),
            totalInterest = totalInterest.toDouble(),
            totalAmount = totalAmount.toDouble(),
            schedule = schedule
        )
    }

    /**
     * Calculates simple interest for display purposes (matching design).
     * Simple Interest = P × R × T / 100
     * Where T is in years.
     */
    fun calculateSimpleInterest(
        principal: Double,
        annualInterestRate: Double,
        tenureMonths: Int
    ): LoanCalculationResult {
        require(principal > 0) { "Principal must be positive" }
        require(tenureMonths > 0) { "Tenure must be at least 1 month" }
        require(annualInterestRate >= 0) { "Interest rate cannot be negative" }

        if (annualInterestRate == 0.0) {
            return calculateZeroInterest(principal, tenureMonths)
        }

        val p = BigDecimal(principal.toString())
        val rate = BigDecimal(annualInterestRate.toString())
        val years = BigDecimal(tenureMonths.toString())
            .divide(BigDecimal("12"), mc)

        // Simple Interest = P × R × T / 100
        val totalInterest = p.multiply(rate, mc)
            .multiply(years, mc)
            .divide(BigDecimal("100"), SCALE, RoundingMode.HALF_UP)

        val totalAmount = p.add(totalInterest).setScale(SCALE, RoundingMode.HALF_UP)
        val monthlyPayment = totalAmount.divide(
            BigDecimal(tenureMonths), SCALE, RoundingMode.HALF_UP
        )

        // Generate simple schedule (equal payments)
        val schedule = mutableListOf<AmortizationEntry>()
        var remaining = totalAmount
        val monthlyInterest = totalInterest.divide(
            BigDecimal(tenureMonths), SCALE, RoundingMode.HALF_UP
        )
        val monthlyPrincipal = p.divide(
            BigDecimal(tenureMonths), SCALE, RoundingMode.HALF_UP
        )

        for (i in 1..tenureMonths) {
            remaining = remaining.subtract(monthlyPayment)
            if (remaining < BigDecimal.ZERO) remaining = BigDecimal.ZERO

            schedule.add(
                AmortizationEntry(
                    paymentNumber = i,
                    emi = monthlyPayment.toDouble(),
                    interestComponent = monthlyInterest.toDouble(),
                    principalComponent = monthlyPrincipal.toDouble(),
                    remainingBalance = remaining.setScale(SCALE, RoundingMode.HALF_UP).toDouble()
                )
            )
        }

        return LoanCalculationResult(
            principal = principal,
            annualInterestRate = annualInterestRate,
            tenureMonths = tenureMonths,
            monthlyEmi = monthlyPayment.toDouble(),
            totalInterest = totalInterest.toDouble(),
            totalAmount = totalAmount.toDouble(),
            schedule = schedule
        )
    }

    private fun calculateZeroInterest(
        principal: Double,
        tenureMonths: Int
    ): LoanCalculationResult {
        val p = BigDecimal(principal.toString())
        val emi = p.divide(BigDecimal(tenureMonths), SCALE, RoundingMode.HALF_UP)

        val schedule = mutableListOf<AmortizationEntry>()
        var remaining = p

        for (i in 1..tenureMonths) {
            remaining = remaining.subtract(emi)
            if (remaining < BigDecimal.ZERO) remaining = BigDecimal.ZERO

            schedule.add(
                AmortizationEntry(
                    paymentNumber = i,
                    emi = emi.toDouble(),
                    interestComponent = 0.0,
                    principalComponent = emi.toDouble(),
                    remainingBalance = remaining.setScale(SCALE, RoundingMode.HALF_UP).toDouble()
                )
            )
        }

        return LoanCalculationResult(
            principal = principal,
            annualInterestRate = 0.0,
            tenureMonths = tenureMonths,
            monthlyEmi = emi.toDouble(),
            totalInterest = 0.0,
            totalAmount = principal,
            schedule = schedule
        )
    }

    /**
     * Generates a detailed amortization schedule showing the breakdown
     * of each monthly payment into interest and principal components.
     */
    private fun generateSchedule(
        principal: BigDecimal,
        monthlyRate: BigDecimal,
        emi: BigDecimal,
        tenureMonths: Int
    ): List<AmortizationEntry> {
        val schedule = mutableListOf<AmortizationEntry>()
        var remaining = principal

        for (i in 1..tenureMonths) {
            val interestComponent = remaining.multiply(monthlyRate, mc)
                .setScale(SCALE, RoundingMode.HALF_UP)

            var principalComponent = emi.subtract(interestComponent)
                .setScale(SCALE, RoundingMode.HALF_UP)

            remaining = remaining.subtract(principalComponent)
                .setScale(SCALE, RoundingMode.HALF_UP)

            // Handle final payment rounding adjustment
            if (i == tenureMonths && remaining.abs() < BigDecimal("1.00")) {
                principalComponent = principalComponent.add(remaining)
                remaining = BigDecimal.ZERO
            }

            if (remaining < BigDecimal.ZERO) remaining = BigDecimal.ZERO

            schedule.add(
                AmortizationEntry(
                    paymentNumber = i,
                    emi = emi.toDouble(),
                    interestComponent = interestComponent.toDouble(),
                    principalComponent = principalComponent.toDouble(),
                    remainingBalance = remaining.toDouble()
                )
            )
        }

        return schedule
    }
}
