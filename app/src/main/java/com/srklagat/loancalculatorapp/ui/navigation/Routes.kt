package com.srklagat.loancalculatorapp.ui.navigation

/**
 * Navigation route constants for the app.
 */
object Routes {
    const val DASHBOARD = "dashboard"
    const val APPLY_LOAN = "apply_loan/{loanTypeId}"
    const val CONFIRM_LOAN = "confirm_loan"
    const val LOAN_DETAIL = "loan_detail/{loanId}"

    fun applyLoan(loanTypeId: String) = "apply_loan/$loanTypeId"
    fun loanDetail(loanId: Long) = "loan_detail/$loanId"
}
