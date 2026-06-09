package com.srklagat.loancalculatorapp.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Instrumented tests for Room database operations.
 * Tests CRUD operations for loan calculations.
 */
@RunWith(AndroidJUnit4::class)
class LoanDatabaseTest {

    private lateinit var db: LoanDatabase
    private lateinit var dao: LoanCalculationDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            LoanDatabase::class.java
        ).build()
        dao = db.loanCalculationDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndRetrieveCalculation() = runBlocking {
        // Given
        val entity = LoanCalculationEntity(
            loanType = "Salary E-Loan",
            principal = 50000.0,
            annualInterestRate = 15.0,
            tenureMonths = 12,
            monthlyEmi = 4512.92,
            totalInterest = 4155.04,
            totalAmount = 54155.04,
            disbursementAccount = "0110901452461OO"
        )

        // When
        val id = dao.insert(entity)

        // Then
        val saved = dao.getById(id)
        assertNotNull(saved)
        assertEquals("Salary E-Loan", saved!!.loanType)
        assertEquals(50000.0, saved.principal, 0.01)
        assertEquals(15.0, saved.annualInterestRate, 0.01)
    }

    @Test
    fun getAllCalculationsReturnsList() = runBlocking {
        // Given
        val entity1 = LoanCalculationEntity(
            loanType = "Salary E-Loan",
            principal = 50000.0,
            annualInterestRate = 15.0,
            tenureMonths = 12,
            monthlyEmi = 4512.92,
            totalInterest = 4155.04,
            totalAmount = 54155.04,
            disbursementAccount = "0110901452461OO"
        )
        val entity2 = LoanCalculationEntity(
            loanType = "Stock Loan",
            principal = 100000.0,
            annualInterestRate = 12.0,
            tenureMonths = 24,
            monthlyEmi = 4707.35,
            totalInterest = 12976.40,
            totalAmount = 112976.40,
            disbursementAccount = "0110901452462OO"
        )

        // When
        dao.insert(entity1)
        dao.insert(entity2)

        // Then
        val all = dao.getAllCalculations().first()
        assertEquals(2, all.size)
    }

    @Test
    fun deleteCalculationRemovesFromDb() = runBlocking {
        // Given
        val entity = LoanCalculationEntity(
            loanType = "Buy Now Pay Later",
            principal = 25000.0,
            annualInterestRate = 18.0,
            tenureMonths = 6,
            monthlyEmi = 4375.0,
            totalInterest = 1250.0,
            totalAmount = 26250.0,
            disbursementAccount = "0110901452463OO"
        )
        val id = dao.insert(entity)

        // When
        val saved = dao.getById(id)!!
        dao.delete(saved)

        // Then
        val deleted = dao.getById(id)
        assertNull(deleted)
    }

    @Test
    fun calculationsOrderedByDateDescending() = runBlocking {
        // Given - insert in order
        val entity1 = LoanCalculationEntity(
            loanType = "Loan 1",
            principal = 10000.0,
            annualInterestRate = 10.0,
            tenureMonths = 6,
            monthlyEmi = 1725.0,
            totalInterest = 350.0,
            totalAmount = 10350.0,
            disbursementAccount = "0110901452461OO"
        )
        val entity2 = LoanCalculationEntity(
            loanType = "Loan 2",
            principal = 20000.0,
            annualInterestRate = 10.0,
            tenureMonths = 6,
            monthlyEmi = 3450.0,
            totalInterest = 700.0,
            totalAmount = 20700.0,
            disbursementAccount = "0110901452461OO"
        )

        // When
        dao.insert(entity1)
        Thread.sleep(100) // Ensure different timestamps
        dao.insert(entity2)

        // Then
        val all = dao.getAllCalculations().first()
        assertTrue(all[0].createdAt >= all[1].createdAt)
    }

    @Test
    fun getNonExistentCalculationReturnsNull() = runBlocking {
        val result = dao.getById(9999L)
        assertNull(result)
    }

    @Test
    fun insertMultipleCalculationsGeneratesUniqueIds() = runBlocking {
        val entity = LoanCalculationEntity(
            loanType = "Test Loan",
            principal = 10000.0,
            annualInterestRate = 10.0,
            tenureMonths = 6,
            monthlyEmi = 1725.0,
            totalInterest = 350.0,
            totalAmount = 10350.0,
            disbursementAccount = "0110901452461OO"
        )

        val id1 = dao.insert(entity)
        val id2 = dao.insert(entity)
        val id3 = dao.insert(entity)

        assertNotEquals(id1, id2)
        assertNotEquals(id2, id3)
        assertNotEquals(id1, id3)
    }
}
