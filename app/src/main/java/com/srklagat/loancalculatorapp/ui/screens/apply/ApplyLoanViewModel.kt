package com.srklagat.loancalculatorapp.ui.screens.apply

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.srklagat.loancalculatorapp.data.local.LoanCalculationEntity
import com.srklagat.loancalculatorapp.data.local.LoanDatabase
import com.srklagat.loancalculatorapp.data.model.LoanCalculationResult
import com.srklagat.loancalculatorapp.data.model.LoanProduct
import com.srklagat.loancalculatorapp.domain.LoanCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Apply Loan flow (form + confirmation).
 * Handles input state, live calculation, validation, and persistence.
 */
class ApplyLoanViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = LoanDatabase.getDatabase(application).loanCalculationDao()

    // Form state
    private val _selectedProduct = MutableStateFlow<LoanProduct?>(null)
    val selectedProduct: StateFlow<LoanProduct?> = _selectedProduct.asStateFlow()

    private val _loanAmount = MutableStateFlow("")
    val loanAmount: StateFlow<String> = _loanAmount.asStateFlow()

    private val _loanPeriod = MutableStateFlow(1)
    val loanPeriod: StateFlow<Int> = _loanPeriod.asStateFlow()

    private val _disbursementAccount = MutableStateFlow("0110901452461OO")
    val disbursementAccount: StateFlow<String> = _disbursementAccount.asStateFlow()

    // Calculation result
    private val _calculationResult = MutableStateFlow<LoanCalculationResult?>(null)
    val calculationResult: StateFlow<LoanCalculationResult?> = _calculationResult.asStateFlow()

    // Validation errors
    private val _amountError = MutableStateFlow<String?>(null)
    val amountError: StateFlow<String?> = _amountError.asStateFlow()

    // Save result
    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    // Available loan products
    val loanProducts = LoanProduct.getSampleProducts()

    // Sample disbursement accounts
    val disbursementAccounts = listOf(
        "0110901452461OO",
        "0110901452462OO",
        "0110901452463OO"
    )

    fun selectProduct(product: LoanProduct) {
        _selectedProduct.value = product
        recalculate()
    }

    fun selectProductById(productId: String) {
        val product = loanProducts.find { it.id == productId }
        if (product != null) {
            _selectedProduct.value = product
            recalculate()
        }
    }

    fun updateLoanAmount(amount: String) {
        // Only allow numeric input with optional decimal point
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _loanAmount.value = amount
            validateAmount()
            recalculate()
        }
    }

    fun updateLoanPeriod(months: Int) {
        _loanPeriod.value = months
        recalculate()
    }

    fun updateDisbursementAccount(account: String) {
        _disbursementAccount.value = account
    }

    private fun validateAmount(): Boolean {
        val product = _selectedProduct.value ?: return false
        val amount = _loanAmount.value.toDoubleOrNull()

        _amountError.value = when {
            amount == null && _loanAmount.value.isNotEmpty() -> "Invalid amount"
            amount != null && amount < product.minAmount ->
                "Minimum amount is ${product.minAmount.toLong()} KES"
            amount != null && amount > product.maxAmount ->
                "Maximum amount is ${product.maxAmount.toLong()} KES"
            else -> null
        }

        return _amountError.value == null
    }

    private fun recalculate() {
        val product = _selectedProduct.value ?: return
        val amount = _loanAmount.value.toDoubleOrNull() ?: return
        val period = _loanPeriod.value

        if (amount <= 0 || period <= 0) {
            _calculationResult.value = null
            return
        }

        // Use simple interest to match the design's calculation style
        _calculationResult.value = LoanCalculator.calculateSimpleInterest(
            principal = amount,
            annualInterestRate = product.annualInterestRate,
            tenureMonths = period
        )
    }

    fun isFormValid(): Boolean {
        val product = _selectedProduct.value ?: return false
        val amount = _loanAmount.value.toDoubleOrNull() ?: return false
        return amount > 0 && _loanPeriod.value > 0 && validateAmount()
    }

    /**
     * Saves the current loan calculation to the Room database.
     */
    fun saveLoanCalculation() {
        val product = _selectedProduct.value ?: return
        val result = _calculationResult.value ?: return

        viewModelScope.launch {
            val entity = LoanCalculationEntity(
                loanType = product.name,
                principal = result.principal,
                annualInterestRate = result.annualInterestRate,
                tenureMonths = result.tenureMonths,
                monthlyEmi = result.monthlyEmi,
                totalInterest = result.totalInterest,
                totalAmount = result.totalAmount,
                disbursementAccount = _disbursementAccount.value
            )
            dao.insert(entity)
            _isSaved.value = true
        }
    }

    fun resetSaveState() {
        _isSaved.value = false
    }

    fun resetForm() {
        _selectedProduct.value = null
        _loanAmount.value = ""
        _loanPeriod.value = 1
        _calculationResult.value = null
        _amountError.value = null
        _isSaved.value = false
    }
}
