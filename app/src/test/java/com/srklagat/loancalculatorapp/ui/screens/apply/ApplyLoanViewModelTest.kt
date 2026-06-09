package com.srklagat.loancalculatorapp.ui.screens.apply

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.srklagat.loancalculatorapp.data.model.LoanProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

/**
 * Unit tests for ApplyLoanViewModel.
 * Tests form validation, calculation updates, and state management.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class ApplyLoanViewModelTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var application: Application

    private lateinit var viewModel: ApplyLoanViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = ApplyLoanViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== Form State Tests ====================

    @Test
    fun `initial state is empty`() = runTest {
        assertNull(viewModel.selectedProduct.first())
        assertEquals("", viewModel.loanAmount.first())
        assertEquals(1, viewModel.loanPeriod.first())
        assertNotNull(viewModel.disbursementAccount.first())
        assertNull(viewModel.calculationResult.first())
        assertNull(viewModel.amountError.first())
    }

    @Test
    fun `select product updates state`() = runTest {
        val product = LoanProduct.getSampleProducts().first()

        viewModel.selectProduct(product)

        assertEquals(product, viewModel.selectedProduct.first())
    }

    @Test
    fun `update loan amount with valid number`() = runTest {
        viewModel.updateLoanAmount("50000")

        assertEquals("50000", viewModel.loanAmount.first())
    }

    @Test
    fun `update loan amount with decimal`() = runTest {
        viewModel.updateLoanAmount("50000.50")

        assertEquals("50000.50", viewModel.loanAmount.first())
    }

    @Test
    fun `update loan amount rejects invalid input`() = runTest {
        viewModel.updateLoanAmount("abc")

        assertEquals("", viewModel.loanAmount.first())
    }

    @Test
    fun `update loan amount rejects multiple decimals`() = runTest {
        viewModel.updateLoanAmount("500.00.50")

        assertEquals("", viewModel.loanAmount.first())
    }

    @Test
    fun `update loan period changes state`() = runTest {
        viewModel.updateLoanPeriod(24)

        assertEquals(24, viewModel.loanPeriod.first())
    }

    @Test
    fun `update disbursement account changes state`() = runTest {
        val newAccount = "0123456789"

        viewModel.updateDisbursementAccount(newAccount)

        assertEquals(newAccount, viewModel.disbursementAccount.first())
    }

    // ==================== Validation Tests ====================

    @Test
    fun `amount below minimum shows error`() = runTest {
        val product = LoanProduct.getSampleProducts().first() // Min amount is 5000
        viewModel.selectProduct(product)
        viewModel.updateLoanAmount("1000")

        val error = viewModel.amountError.first()
        assertNotNull(error)
        assertTrue(error!!.contains("Minimum"))
    }

    @Test
    fun `amount above maximum shows error`() = runTest {
        val product = LoanProduct.getSampleProducts().first() // Max amount is 300000
        viewModel.selectProduct(product)
        viewModel.updateLoanAmount("500000")

        val error = viewModel.amountError.first()
        assertNotNull(error)
        assertTrue(error!!.contains("Maximum"))
    }

    @Test
    fun `valid amount clears error`() = runTest {
        val product = LoanProduct.getSampleProducts().first()
        viewModel.selectProduct(product)
        viewModel.updateLoanAmount("50000") // Within range

        val error = viewModel.amountError.first()
        assertNull(error)
    }

    @Test
    fun `isFormValid returns false when no product selected`() {
        viewModel.updateLoanAmount("50000")

        assertFalse(viewModel.isFormValid())
    }

    @Test
    fun `isFormValid returns false when amount is empty`() = runTest {
        val product = LoanProduct.getSampleProducts().first()
        viewModel.selectProduct(product)

        assertFalse(viewModel.isFormValid())
    }

    @Test
    fun `isFormValid returns false when amount has error`() = runTest {
        val product = LoanProduct.getSampleProducts().first()
        viewModel.selectProduct(product)
        viewModel.updateLoanAmount("1000") // Below minimum

        assertFalse(viewModel.isFormValid())
    }

    // ==================== Calculation Tests ====================

    @Test
    fun `calculation result updates when amount and period set`() = runTest {
        val product = LoanProduct.getSampleProducts().first()
        viewModel.selectProduct(product)
        viewModel.updateLoanAmount("10000")
        viewModel.updateLoanPeriod(6)

        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.calculationResult.first()
        assertNotNull(result)
        assertEquals(10000.0, result!!.principal, 0.01)
        assertEquals(6, result.tenureMonths)
        assertTrue(result.monthlyEmi > 0)
    }

    // ==================== Reset Tests ====================

    @Test
    fun `resetForm clears all state`() = runTest {
        val product = LoanProduct.getSampleProducts().first()
        viewModel.selectProduct(product)
        viewModel.updateLoanAmount("50000")
        viewModel.updateLoanPeriod(12)

        viewModel.resetForm()

        assertNull(viewModel.selectedProduct.first())
        assertEquals("", viewModel.loanAmount.first())
        assertEquals(1, viewModel.loanPeriod.first())
        assertNull(viewModel.calculationResult.first())
        assertNull(viewModel.amountError.first())
    }

    @Test
    fun `select product by ID finds correct product`() = runTest {
        val products = LoanProduct.getSampleProducts()
        val targetProduct = products[1] // Buy Now Pay Later

        viewModel.selectProductById(targetProduct.id)

        assertEquals(targetProduct, viewModel.selectedProduct.first())
    }

    @Test
    fun `select product by nonexistent ID does nothing`() = runTest {
        viewModel.selectProductById("nonexistent")

        assertNull(viewModel.selectedProduct.first())
    }

    // ==================== Disbursement Accounts ====================

    @Test
    fun `disbursement accounts list is not empty`() {
        assertTrue(viewModel.disbursementAccounts.isNotEmpty())
    }

    @Test
    fun `loan products list is not empty`() {
        assertTrue(viewModel.loanProducts.isNotEmpty())
    }
}
