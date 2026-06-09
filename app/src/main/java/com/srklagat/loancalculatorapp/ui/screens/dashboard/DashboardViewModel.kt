package com.srklagat.loancalculatorapp.ui.screens.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.srklagat.loancalculatorapp.data.local.LoanCalculationEntity
import com.srklagat.loancalculatorapp.data.local.LoanDatabase
import com.srklagat.loancalculatorapp.data.model.LoanProduct
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Dashboard screen.
 * Manages saved loan calculations and loan product data.
 */
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = LoanDatabase.getDatabase(application).loanCalculationDao()

    private val _savedCalculations = MutableStateFlow<List<LoanCalculationEntity>>(emptyList())
    val savedCalculations: StateFlow<List<LoanCalculationEntity>> = _savedCalculations.asStateFlow()

    private val _loanProducts = MutableStateFlow(LoanProduct.getSampleProducts())
    val loanProducts: StateFlow<List<LoanProduct>> = _loanProducts.asStateFlow()

    init {
        loadSavedCalculations()
    }

    private fun loadSavedCalculations() {
        viewModelScope.launch {
            dao.getAllCalculations().collect { calculations ->
                _savedCalculations.value = calculations
            }
        }
    }

    fun deleteCalculation(calculation: LoanCalculationEntity) {
        viewModelScope.launch {
            dao.delete(calculation)
        }
    }
}
