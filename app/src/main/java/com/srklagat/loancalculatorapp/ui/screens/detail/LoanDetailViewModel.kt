package com.srklagat.loancalculatorapp.ui.screens.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.srklagat.loancalculatorapp.data.local.LoanCalculationEntity
import com.srklagat.loancalculatorapp.data.local.LoanDatabase
import com.srklagat.loancalculatorapp.data.model.LoanCalculationResult
import com.srklagat.loancalculatorapp.domain.LoanCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for viewing a saved loan calculation detail.
 * Loads the entity from Room and regenerates the amortization schedule.
 */
class LoanDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = LoanDatabase.getDatabase(application).loanCalculationDao()

    private val _loanEntity = MutableStateFlow<LoanCalculationEntity?>(null)
    val loanEntity: StateFlow<LoanCalculationEntity?> = _loanEntity.asStateFlow()

    private val _calculationResult = MutableStateFlow<LoanCalculationResult?>(null)
    val calculationResult: StateFlow<LoanCalculationResult?> = _calculationResult.asStateFlow()

    fun loadLoan(loanId: Long) {
        viewModelScope.launch {
            val entity = dao.getCalculationById(loanId)
            _loanEntity.value = entity

            // Regenerate the full amortization schedule from saved parameters
            if (entity != null) {
                _calculationResult.value = LoanCalculator.calculateSimpleInterest(
                    principal = entity.principal,
                    annualInterestRate = entity.annualInterestRate,
                    tenureMonths = entity.tenureMonths
                )
            }
        }
    }

    fun deleteLoan() {
        viewModelScope.launch {
            _loanEntity.value?.let { dao.delete(it) }
        }
    }
}
