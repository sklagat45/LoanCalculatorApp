package com.srklagat.loancalculatorapp.ui.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.srklagat.loancalculatorapp.ui.screens.apply.ApplyLoanScreen
import com.srklagat.loancalculatorapp.ui.screens.apply.ApplyLoanViewModel
import com.srklagat.loancalculatorapp.ui.screens.confirm.ConfirmLoanScreen
import com.srklagat.loancalculatorapp.ui.screens.dashboard.DashboardScreen
import com.srklagat.loancalculatorapp.ui.screens.detail.LoanDetailScreen
import com.srklagat.loancalculatorapp.ui.screens.success.SuccessDialog

/**
 * Main navigation graph for the app.
 * Uses Navigation Compose with shared ViewModel for the Apply → Confirm flow.
 */
@Composable
fun NavGraph(navController: NavHostController) {
    // Shared ViewModel for Apply Loan → Confirm flow
    val applyLoanViewModel: ApplyLoanViewModel = viewModel()

    // Success dialog state
    var showSuccessDialog by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD
    ) {
        // Dashboard / Home
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onNavigateToAvailableLoans = { /* Already showing on dashboard */ },
                onNavigateToApplyLoan = { loanTypeId ->
                    applyLoanViewModel.resetForm()
                    applyLoanViewModel.selectProductById(loanTypeId)
                    navController.navigate(Routes.applyLoan(loanTypeId))
                },
                onNavigateToSavedLoan = { loanId ->
                    navController.navigate(Routes.loanDetail(loanId))
                }
            )
        }

        // Apply Loan Form
        composable(
            route = Routes.APPLY_LOAN,
            arguments = listOf(
                navArgument("loanTypeId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val loanTypeId = backStackEntry.arguments?.getString("loanTypeId") ?: ""

            LaunchedEffect(loanTypeId) {
                if (applyLoanViewModel.selectedProduct.value == null) {
                    applyLoanViewModel.selectProductById(loanTypeId)
                }
            }

            ApplyLoanScreen(
                viewModel = applyLoanViewModel,
                onBackClick = { navController.popBackStack() },
                onCloseClick = {
                    navController.popBackStack(Routes.DASHBOARD, inclusive = false)
                },
                onApplyClick = {
                    navController.navigate(Routes.CONFIRM_LOAN)
                }
            )
        }

        // Confirm Loan
        composable(Routes.CONFIRM_LOAN) {
            ConfirmLoanScreen(
                viewModel = applyLoanViewModel,
                onBackClick = { navController.popBackStack() },
                onCloseClick = {
                    navController.popBackStack(Routes.DASHBOARD, inclusive = false)
                },
                onConfirmClick = {
                    applyLoanViewModel.saveLoanCalculation()
                    showSuccessDialog = true
                }
            )
        }

        // Loan Detail (saved calculation)
        composable(
            route = Routes.LOAN_DETAIL,
            arguments = listOf(
                navArgument("loanId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val loanId = backStackEntry.arguments?.getLong("loanId") ?: 0L

            LoanDetailScreen(
                loanId = loanId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }

    // Success dialog overlay
    if (showSuccessDialog) {
        SuccessDialog(
            onGoHome = {
                showSuccessDialog = false
                applyLoanViewModel.resetForm()
                navController.popBackStack(Routes.DASHBOARD, inclusive = false)
            }
        )
    }
}
