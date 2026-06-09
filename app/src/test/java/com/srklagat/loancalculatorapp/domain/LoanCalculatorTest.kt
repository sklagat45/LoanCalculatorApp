package com.srklagat.loancalculatorapp.domain

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Unit tests for the LoanCalculator domain logic.
 * Tests cover EMI calculation, amortization schedule generation,
 * edge cases (0% interest), and mathematical accuracy.
 */
@RunWith(JUnit4::class)
class LoanCalculatorTest {

    private companion object {
        const val DELTA = 0.01 // Tolerance for floating-point comparisons
    }

    @Before
    fun setup() {
        // Any setup if needed
    }

    // ==================== EMI Calculation Tests ====================

    @Test
    fun `calculate EMI for standard loan`() {
        // Given: 100,000 principal, 15% annual rate, 12 months
        val result = LoanCalculator.calculate(
            principal = 100000.0,
            annualInterestRate = 15.0,
            tenureMonths = 12
        )

        // Then: EMI should be approximately 9025.83
        assertEquals(100000.0, result.principal, DELTA)
        assertEquals(15.0, result.annualInterestRate, DELTA)
        assertEquals(12, result.tenureMonths)
        assertEquals(9025.83, result.monthlyEmi, DELTA)
        assertTrue(result.totalInterest > 0)
        assertTrue(result.totalAmount > result.principal)
    }

    @Test
    fun `calculate EMI for 30 year mortgage`() {
        // Given: 500,000 principal, 7% annual rate, 360 months
        val result = LoanCalculator.calculate(
            principal = 500000.0,
            annualInterestRate = 7.0,
            tenureMonths = 360
        )

        // Then: Schedule should have 360 entries
        assertEquals(360, result.schedule.size)
        assertEquals(3326.51, result.monthlyEmi, DELTA)
        assertEquals(697543.6, result.totalInterest, 1.0)
    }

    @Test
    fun `zero interest rate divides principal equally`() {
        // Given: 12000 principal, 0% rate, 12 months
        val result = LoanCalculator.calculate(
            principal = 12000.0,
            annualInterestRate = 0.0,
            tenureMonths = 12
        )

        // Then: EMI should be exactly 1000, zero interest
        assertEquals(1000.0, result.monthlyEmi, DELTA)
        assertEquals(0.0, result.totalInterest, DELTA)
        assertEquals(12000.0, result.totalAmount, DELTA)
        assertEquals(12, result.schedule.size)

        // All payments go to principal
        result.schedule.forEach { entry ->
            assertEquals(0.0, entry.interestComponent, DELTA)
            assertEquals(1000.0, entry.principalComponent, DELTA)
        }
    }

    @Test
    fun `very small principal with high interest`() {
        val result = LoanCalculator.calculate(
            principal = 1000.0,
            annualInterestRate = 24.0,
            tenureMonths = 3
        )

        assertTrue(result.monthlyEmi > 0)
        assertEquals(3, result.schedule.size)
        assertTrue(result.totalInterest > 0)
    }

    // ==================== Amortization Schedule Tests ====================

    @Test
    fun `schedule first payment has highest interest`() {
        val result = LoanCalculator.calculate(
            principal = 100000.0,
            annualInterestRate = 12.0,
            tenureMonths = 12
        )

        val firstPayment = result.schedule.first()
        val lastPayment = result.schedule.last()

        // Interest decreases over time
        assertTrue(firstPayment.interestComponent > lastPayment.interestComponent)
        // Principal increases over time
        assertTrue(firstPayment.principalComponent < lastPayment.principalComponent)
    }

    @Test
    fun `remaining balance decreases each month`() {
        val result = LoanCalculator.calculate(
            principal = 50000.0,
            annualInterestRate = 10.0,
            tenureMonths = 6
        )

        var previousBalance = result.principal
        result.schedule.forEachIndexed { index, entry ->
            assertTrue(
                "Payment ${index + 1}: Balance should decrease",
                entry.remainingBalance < previousBalance || index == result.schedule.size - 1
            )
            previousBalance = entry.remainingBalance
        }
    }

    @Test
    fun `final balance is zero or near zero`() {
        val result = LoanCalculator.calculate(
            principal = 75000.0,
            annualInterestRate = 8.5,
            tenureMonths = 24
        )

        val finalBalance = result.schedule.last().remainingBalance
        assertTrue(
            "Final balance $finalBalance should be approximately zero",
            finalBalance <= 0.01
        )
    }

    @Test
    fun `emi is constant across all payments`() {
        val result = LoanCalculator.calculate(
            principal = 30000.0,
            annualInterestRate = 6.0,
            tenureMonths = 12
        )

        result.schedule.forEach { entry ->
            assertEquals(result.monthlyEmi, entry.emi, DELTA)
        }
    }

    @Test
    fun `sum of principal components equals original principal`() {
        val principal = 25000.0
        val result = LoanCalculator.calculate(
            principal = principal,
            annualInterestRate = 9.0,
            tenureMonths = 12
        )

        val totalPrincipalPaid = result.schedule.sumOf { it.principalComponent }
        assertEquals(principal, totalPrincipalPaid, 0.1)
    }

    // ==================== Simple Interest Tests ====================

    @Test
    fun `simple interest calculation produces equal payments`() {
        val result = LoanCalculator.calculateSimpleInterest(
            principal = 10000.0,
            annualInterestRate = 15.0,
            tenureMonths = 12
        )

        // Simple interest: 10000 * 15% = 1500 interest, total = 11500
        // Monthly = 11500 / 12 = 958.33
        assertEquals(958.33, result.monthlyEmi, DELTA)
        assertEquals(1500.0, result.totalInterest, DELTA)
        assertEquals(11500.0, result.totalAmount, DELTA)
    }

    @Test
    fun `simple interest with zero rate equals principal divided by months`() {
        val result = LoanCalculator.calculateSimpleInterest(
            principal = 12000.0,
            annualInterestRate = 0.0,
            tenureMonths = 6
        )

        assertEquals(2000.0, result.monthlyEmi, DELTA)
        assertEquals(0.0, result.totalInterest, DELTA)
    }

    // ==================== Validation & Edge Cases ====================

    @Test(expected = IllegalArgumentException::class)
    fun `negative principal throws exception`() {
        LoanCalculator.calculate(
            principal = -1000.0,
            annualInterestRate = 10.0,
            tenureMonths = 12
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `zero tenure throws exception`() {
        LoanCalculator.calculate(
            principal = 10000.0,
            annualInterestRate = 10.0,
            tenureMonths = 0
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative interest rate throws exception`() {
        LoanCalculator.calculate(
            principal = 10000.0,
            annualInterestRate = -5.0,
            tenureMonths = 12
        )
    }

    @Test
    fun `single month tenure calculates correctly`() {
        val result = LoanCalculator.calculate(
            principal = 10000.0,
            annualInterestRate = 12.0,
            tenureMonths = 1
        )

        assertEquals(1, result.schedule.size)
        assertTrue(result.monthlyEmi > result.principal) // Includes one month interest
    }

    @Test
    fun `very long tenure calculates efficiently`() {
        val startTime = System.currentTimeMillis()

        val result = LoanCalculator.calculate(
            principal = 100000.0,
            annualInterestRate = 5.0,
            tenureMonths = 360
        )

        val duration = System.currentTimeMillis() - startTime

        assertEquals(360, result.schedule.size)
        assertTrue("Calculation took ${duration}ms, should be under 100ms", duration < 100)
    }

    @Test
    fun `decimal precision with large numbers`() {
        val result = LoanCalculator.calculate(
            principal = 9999999.99,
            annualInterestRate = 13.57,
            tenureMonths = 36
        )

        // Verify no overflow and proper precision
        assertTrue(result.monthlyEmi > 0)
        assertTrue(result.totalInterest > 0)
        assertEquals(36, result.schedule.size)
    }

    @Test
    fun `emi plus total interest equals total amount`() {
        val result = LoanCalculator.calculate(
            principal = 50000.0,
            annualInterestRate = 11.0,
            tenureMonths = 18
        )

        val calculatedTotal = result.monthlyEmi * result.tenureMonths
        assertEquals(result.totalAmount, calculatedTotal, DELTA)
        assertEquals(result.principal + result.totalInterest, result.totalAmount, DELTA)
    }
}
