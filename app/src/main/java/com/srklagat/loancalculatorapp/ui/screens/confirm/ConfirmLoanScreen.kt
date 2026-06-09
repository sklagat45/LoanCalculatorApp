package com.srklagat.loancalculatorapp.ui.screens.confirm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srklagat.loancalculatorapp.ui.components.LoanTopBar
import com.srklagat.loancalculatorapp.ui.screens.apply.ApplyLoanViewModel
import com.srklagat.loancalculatorapp.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Loan confirmation screen showing summary of loan details,
 * disbursement details, and repayment details before final submission.
 */
@Composable
fun ConfirmLoanScreen(
    viewModel: ApplyLoanViewModel,
    onBackClick: () -> Unit,
    onCloseClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val calculationResult by viewModel.calculationResult.collectAsState()
    val disbursementAccount by viewModel.disbursementAccount.collectAsState()
    val loanPeriod by viewModel.loanPeriod.collectAsState()

    val formatter = remember {
        NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }

    val result = calculationResult ?: return
    val product = selectedProduct ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar
        LoanTopBar(
            title = "Apply Loan",
            onBackClick = onBackClick,
            onCloseClick = onCloseClick
        )

        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // === Loan Details ===
            Text(
                text = "Loan Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            DetailRow(
                label = "Loan Amount:",
                value = "${formatter.format(result.principal)} KES",
                valueColor = GreenText
            )
            DetailRow(
                label = "Interest:",
                value = "${formatter.format(result.totalInterest)} KES"
            )
            DetailRow(
                label = "Total Charges:",
                value = "${formatter.format(result.totalAmount)} KES"
            )
            DetailRow(
                label = "Period:",
                value = "$loanPeriod Months"
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(20.dp))

            // === Disbursement Details ===
            Text(
                text = "Disbursement Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            DetailRow(
                label = "Account:",
                value = disbursementAccount
            )
            DetailRow(
                label = "Amount:",
                value = "${formatter.format(result.principal)} KES"
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(20.dp))

            // === Repayment Details ===
            Text(
                text = "Repayment Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            DetailRow(
                label = "Amount:",
                value = "${formatter.format(result.totalAmount)} KES"
            )
            DetailRow(
                label = "Installments:",
                value = "$loanPeriod"
            )

            // Next repayment date
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, 1)

            DetailRow(
                label = "Next Repayment Date:",
                value = dateFormat.format(calendar.time)
            )
        }

        // Confirm button at the bottom
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = onConfirmClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonGreen,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Confirm",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(modifier = Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()))
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}
