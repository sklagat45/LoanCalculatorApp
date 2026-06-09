package com.srklagat.loancalculatorapp.ui.screens.apply

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srklagat.loancalculatorapp.ui.components.LoanTopBar
import com.srklagat.loancalculatorapp.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Apply Loan form screen matching the design.
 * Shows loan type, amount, period, calculated totals,
 * disbursement account, and repayment schedule preview.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyLoanScreen(
    viewModel: ApplyLoanViewModel,
    onBackClick: () -> Unit,
    onCloseClick: () -> Unit,
    onApplyClick: () -> Unit
) {
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val loanAmount by viewModel.loanAmount.collectAsState()
    val loanPeriod by viewModel.loanPeriod.collectAsState()
    val disbursementAccount by viewModel.disbursementAccount.collectAsState()
    val calculationResult by viewModel.calculationResult.collectAsState()
    val amountError by viewModel.amountError.collectAsState()

    val formatter = remember {
        NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }

    // Dropdown expanded states
    var loanTypeExpanded by remember { mutableStateOf(false) }
    var periodExpanded by remember { mutableStateOf(false) }
    var accountExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Fixed Top Bar
        LoanTopBar(
            title = "Apply Loan",
            onBackClick = onBackClick,
            onCloseClick = onCloseClick
        )

        // 2. Scrollable Form Content (Fills remaining space above the button)
        Column(
            modifier = Modifier
                .weight(1f) // Takes available space, allowing the bottom button to stay pinned
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Loan Type Dropdown
            Text(
                text = "Loan Type",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            ExposedDropdownMenuBox(
                expanded = loanTypeExpanded,
                onExpandedChange = { loanTypeExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedProduct?.name ?: "Select loan type",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = loanTypeExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                ExposedDropdownMenu(
                    expanded = loanTypeExpanded,
                    onDismissRequest = { loanTypeExpanded = false }
                ) {
                    viewModel.loanProducts.forEach { product ->
                        DropdownMenuItem(
                            text = { Text(product.name) },
                            onClick = {
                                viewModel.selectProduct(product)
                                loanTypeExpanded = false
                            }
                        )
                    }
                }
            }

            // Interest rate display
            if (selectedProduct != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Interest: ${selectedProduct!!.annualInterestRate.toInt()}% p.a",
                    color = GreenText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Loan Amount
            Text(
                text = "Loan Amount",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = loanAmount,
                onValueChange = { viewModel.updateLoanAmount(it) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Text(
                            text = "KES",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.outline)
                        )
                    }
                },
                placeholder = {
                    Text("0.00", color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = amountError != null,
                supportingText = if (amountError != null) {
                    { Text(amountError!!, color = ErrorRed) }
                } else null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Available Loan Limit
            if (selectedProduct != null) {
                Text(
                    text = "Available Loan Limit: ${formatter.format(selectedProduct!!.maxAmount)} KES",
                    color = GreenText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Loan Period
            Text(
                text = "Loan Period (months)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            ExposedDropdownMenuBox(
                expanded = periodExpanded,
                onExpandedChange = { periodExpanded = it }
            ) {
                OutlinedTextField(
                    value = loanPeriod.toString(),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = periodExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                ExposedDropdownMenu(
                    expanded = periodExpanded,
                    onDismissRequest = { periodExpanded = false }
                ) {
                    val maxMonths = selectedProduct?.maxTenureMonths ?: 36
                    (1..maxMonths).forEach { month ->
                        DropdownMenuItem(
                            text = { Text("$month") },
                            onClick = {
                                viewModel.updateLoanPeriod(month)
                                periodExpanded = false
                            }
                        )
                    }
                }
            }

            // Total Amount Payable
            if (calculationResult != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Total Amount Payable: ${formatter.format(calculationResult!!.totalAmount)} KES",
                    color = GreenText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Disbursement Account
            Text(
                text = "Disbursement Account",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            ExposedDropdownMenuBox(
                expanded = accountExpanded,
                onExpandedChange = { accountExpanded = it }
            ) {
                OutlinedTextField(
                    value = disbursementAccount,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                ExposedDropdownMenu(
                    expanded = accountExpanded,
                    onDismissRequest = { accountExpanded = false }
                ) {
                    viewModel.disbursementAccounts.forEach { account ->
                        DropdownMenuItem(
                            text = { Text(account) },
                            onClick = {
                                viewModel.updateDisbursementAccount(account)
                                accountExpanded = false
                            }
                        )
                    }
                }
            }

            if (selectedProduct != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Available Loan Limit: ${formatter.format(selectedProduct!!.maxAmount)} KES",
                    color = GreenText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Repayment Schedule Preview
            if (calculationResult != null) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Repayment Schedule",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
                val calendar = Calendar.getInstance()

                calculationResult!!.schedule.forEach { entry ->
                    calendar.add(Calendar.MONTH, if (entry.paymentNumber == 1) 1 else 1)
                    val dateStr = dateFormat.format(calendar.time)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${getOrdinal(entry.paymentNumber)} instalment - $dateStr",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${formatter.format(entry.emi)} KES",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 3. Pinned Bottom Button Area (Stays static at the bottom of the screen)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Button(
                onClick = onApplyClick,
                enabled = viewModel.isFormValid(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonGreen,
                    contentColor = Color.White,
                    disabledContainerColor = ButtonGreen.copy(alpha = 0.4f)
                )
            ) {
                Text(
                    text = "Apply Loan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }


            // Handles system navigation bar height spacing
            Spacer(modifier = Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()))
        }
    }
}

private fun getOrdinal(n: Int): String {
    return when {
        n % 100 in 11..13 -> "${n}th"
        n % 10 == 1 -> "${n}st"
        n % 10 == 2 -> "${n}nd"
        n % 10 == 3 -> "${n}rd"
        else -> "${n}th"
    }
}