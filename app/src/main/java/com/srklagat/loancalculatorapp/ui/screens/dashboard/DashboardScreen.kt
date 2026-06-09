package com.srklagat.loancalculatorapp.ui.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.srklagat.loancalculatorapp.ui.components.ActiveLoanCard
import com.srklagat.loancalculatorapp.ui.components.LoanProductCard
import com.srklagat.loancalculatorapp.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Dashboard/Home screen showing active loans and available loan products.
 * Includes a custom top popup banner matching the exact design specification.
 */
@Composable
fun DashboardScreen(
    onNavigateToAvailableLoans: () -> Unit,
    onNavigateToApplyLoan: (String) -> Unit,
    onNavigateToSavedLoan: (Long) -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val savedCalculations by viewModel.savedCalculations.collectAsState()
    val loanProducts by viewModel.loanProducts.collectAsState()

    // State to control the popup banner visibility
    var showInfoBanner by remember { mutableStateOf(false) }

    // Auto-dismiss logic for the popup banner (acts like a snackbar)
    LaunchedEffect(showInfoBanner) {
        if (showInfoBanner) {
            delay(4000) // Display for 4 seconds
            showInfoBanner = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main Screen Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Dark green header with greeting
            DashboardHeader()

            // Scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Active Loans section (saved calculations)
                if (savedCalculations.isNotEmpty()) {
                    Text(
                        text = "Active Loans",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    savedCalculations.forEach { calc ->
                        ActiveLoanCard(
                            loanType = calc.loanType,
                            balance = calc.totalAmount,
                            monthlyPayment = calc.monthlyEmi,
                            interest = calc.totalInterest,
                            onClick = { onNavigateToSavedLoan(calc.id) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Other Loans Available",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    // No saved loans — show full available loans list
                    Text(
                        text = "Available Loans",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Loan product cards with gradients
                val gradients = listOf(
                    SalaryLoanStart to SalaryLoanEnd,
                    BuyNowPayLaterStart to BuyNowPayLaterEnd,
                    StockLoanStart to StockLoanEnd
                )

                loanProducts.forEachIndexed { index, product ->
                    val (start, end) = gradients[index % gradients.size]
                    LoanProductCard(
                        name = product.name,
                        description = product.description,
                        gradientStart = start,
                        gradientEnd = end,
                        imageRes = product.imageRes,
                        onClick = {
                            if (savedCalculations.isNotEmpty()) {
                                // Trigger the custom popup banner
                                showInfoBanner = true
                            } else {
                                onNavigateToApplyLoan(product.id)
                            }
                        },
                        enabled = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        AnimatedVisibility(
            visible = showInfoBanner,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding() // Ensures it sits perfectly below or near the system status bar
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF4FBF7) // Precise matching soft light-green background
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Green Information Icon
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info Icon",
                        tint = GreenText,
                        modifier = Modifier
                            .size(26.dp)
                            .padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        // Title
                        Text(
                            text = "Info",
                            color = GreenText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        // Message
                        Text(
                            text = "Please repay the current loan to apply for a new one.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(HeaderGradientStart, HeaderGradientEnd)
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Profile avatar placeholder
            Surface(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Hello There!",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Boost your income today!",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }
        }
    }
}