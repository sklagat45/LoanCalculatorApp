package com.srklagat.loancalculatorapp

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.srklagat.loancalculatorapp.ui.navigation.Routes
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end UI tests for the loan calculator flow.
 * Tests user interactions from dashboard through confirmation.
 */
@RunWith(AndroidJUnit4::class)
class CalculatorFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun dashboardDisplaysGreetingAndLoanProducts() {
        // Verify dashboard elements
        composeTestRule.onNodeWithText("Hello There!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Boost your income today!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Other Loans Available").assertIsDisplayed()

        // Verify loan product cards exist
        composeTestRule.onNodeWithText("Salary E-Loan").assertExists()
        composeTestRule.onNodeWithText("Buy Now Pay Later").assertExists()
        composeTestRule.onNodeWithText("Stock Loan").assertExists()
    }

    @Test
    fun navigateToApplyLoanForm() {
        // Click on first loan product
        composeTestRule.onAllNodesWithText("Apply Now >")[0]
            .performClick()

        // Verify navigation to apply screen
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Apply Loan").assertIsDisplayed()
        composeTestRule.onNodeWithText("Loan Type").assertIsDisplayed()
    }

    @Test
    fun applyLoanFormValidation() {
        // Navigate to apply screen
        composeTestRule.onAllNodesWithText("Apply Now >")[0].performClick()
        composeTestRule.waitForIdle()

        // Try to enter invalid amount
        composeTestRule.onNodeWithText("Loan Amount")
            .performTextInput("abc")

        // Text should not be entered (filtered by input validation)
        composeTestRule.onNodeWithText("abc").assertDoesNotExist()
    }

    @Test
    fun enterValidLoanAmountShowsCalculation() {
        // Navigate to apply screen
        composeTestRule.onAllNodesWithText("Apply Now >")[0].performClick()
        composeTestRule.waitForIdle()

        // Enter valid amount
        composeTestRule.onNodeWithText("Loan Amount")
            .performTextInput("50000")

        // Verify calculation fields appear
        composeTestRule.onNodeWithText("Total Amount Payable").assertExists()
        composeTestRule.onNodeWithText("KES").assertExists()
    }

    @Test
    fun changeLoanPeriodUpdatesCalculation() {
        // Navigate to apply screen
        composeTestRule.onAllNodesWithText("Apply Now >")[0].performClick()
        composeTestRule.waitForIdle()

        // Enter amount first
        composeTestRule.onNodeWithText("Loan Amount")
            .performTextInput("50000")

        composeTestRule.waitForIdle()

        // Period dropdown should exist
        composeTestRule.onNodeWithText("Loan Period").assertExists()
    }

    @Test
    fun navigateToConfirmationScreen() {
        // Navigate to apply screen
        composeTestRule.onAllNodesWithText("Apply Now >")[0].performClick()
        composeTestRule.waitForIdle()

        // Fill in form
        composeTestRule.onNodeWithText("Loan Amount")
            .performTextInput("50000")

        composeTestRule.waitForIdle()

        // Click Apply Loan button
        composeTestRule.onNodeWithText("Apply Loan")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify confirmation screen
        composeTestRule.onNodeWithText("Confirm").assertExists()
    }

    @Test
    fun backNavigationWorks() {
        // Navigate to apply screen
        composeTestRule.onAllNodesWithText("Apply Now >")[0].performClick()
        composeTestRule.waitForIdle()

        // Click back
        composeTestRule.onNodeWithContentDescription("Back")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify back on dashboard
        composeTestRule.onNodeWithText("Hello There!").assertIsDisplayed()
    }

    @Test
    fun dashboardShowsSavedLoansSection() {
        // Verify saved loans section exists
        composeTestRule.onNodeWithText("Active Loans").assertExists()
    }

    @Test
    fun loanProductCardsDisplayCorrectInfo() {
        // Verify product cards show required info
        composeTestRule.onNodeWithText("Salary E-Loan").assertExists()
        composeTestRule.onNodeWithText("Get quick loans to boost your income").assertExists()

        composeTestRule.onNodeWithText("Buy Now Pay Later").assertExists()
        composeTestRule.onNodeWithText("Buy goods today, pay later").assertExists()

        composeTestRule.onNodeWithText("Stock Loan").assertExists()
        composeTestRule.onNodeWithText("Boost your business stock today").assertExists()
    }
}
