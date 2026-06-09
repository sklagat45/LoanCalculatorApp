package com.srklagat.loancalculatorapp.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.srklagat.loancalculatorapp.ui.components.AmortizationTable
import com.srklagat.loancalculatorapp.ui.components.LoanTopBar
import com.srklagat.loancalculatorapp.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen showing details of a saved loan calculation,
 * including the full amortization schedule.
 */
@Composable
fun LoanDetailScreen(
    loanId: Long,
    onBackClick: () -> Unit,
    viewModel: LoanDetailViewModel = viewModel()
) {
    val loanEntity by viewModel.loanEntity.collectAsState()
    val calculationResult by viewModel.calculationResult.collectAsState()

    val formatter = remember {
        NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }

    LaunchedEffect(loanId) {
        viewModel.loadLoan(loanId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar
        LoanTopBar(
            title = "Loan Details",
            onBackClick = onBackClick,
            onCloseClick = {
                viewModel.deleteLoan()
                onBackClick()
            }
        )

        val entity = loanEntity
        val result = calculationResult

        if (entity == null || result == null) {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Loan Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = entity.loanType,
                            color = GreenText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Detail rows
                        DetailRow("Loan Amount:", "${formatter.format(entity.principal)} KES", GreenText)
                        DetailRow("Interest Rate:", "${entity.annualInterestRate}% p.a")
                        DetailRow("Tenure:", "${entity.tenureMonths} months")
                        DetailRow("Monthly Payment:", "${formatter.format(entity.monthlyEmi)} KES")
                        DetailRow("Total Interest:", "${formatter.format(entity.totalInterest)} KES")
                        DetailRow("Total Amount:", "${formatter.format(entity.totalAmount)} KES")

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(8.dp))

                        DetailRow("Disbursement Account:", entity.disbursementAccount)

                        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
                        DetailRow("Applied:", dateFormat.format(Date(entity.createdAt)))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Amortization Schedule
                Text(
                    text = "Amortization Schedule",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                AmortizationTable(schedule = result.schedule)

                Spacer(modifier = Modifier.height(24.dp))

                // Delete button
                OutlinedButton(
                    onClick = {
                        viewModel.deleteLoan()
                        onBackClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ErrorRed
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Calculation")
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
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
            .padding(vertical = 4.dp),
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
