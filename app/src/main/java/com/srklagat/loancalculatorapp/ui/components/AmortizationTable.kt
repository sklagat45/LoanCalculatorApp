package com.srklagat.loancalculatorapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srklagat.loancalculatorapp.data.model.AmortizationEntry
import com.srklagat.loancalculatorapp.ui.theme.GreenText
import com.srklagat.loancalculatorapp.ui.theme.PrimaryGreen
import java.text.NumberFormat
import java.util.Locale

/**
 * Displays the amortization schedule as a performant LazyColumn table.
 * Handles large tenures (e.g. 360 months) efficiently.
 */
@Composable
fun AmortizationTable(
    schedule: List<AmortizationEntry>,
    modifier: Modifier = Modifier
) {
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Table header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(PrimaryGreen)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TableHeaderCell("#", weight = 0.5f)
            TableHeaderCell("EMI", weight = 1f)
            TableHeaderCell("Interest", weight = 1f)
            TableHeaderCell("Principal", weight = 1f)
            TableHeaderCell("Balance", weight = 1f)
        }

        // Table rows using LazyColumn for performance with large schedules
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
        ) {
            items(
                items = schedule,
                key = { it.paymentNumber }
            ) { entry ->
                val bgColor = if (entry.paymentNumber % 2 == 0) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bgColor)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TableDataCell(
                        text = entry.paymentNumber.toString(),
                        weight = 0.5f
                    )
                    TableDataCell(
                        text = formatter.format(entry.emi),
                        weight = 1f
                    )
                    TableDataCell(
                        text = formatter.format(entry.interestComponent),
                        weight = 1f
                    )
                    TableDataCell(
                        text = formatter.format(entry.principalComponent),
                        weight = 1f
                    )
                    TableDataCell(
                        text = formatter.format(entry.remainingBalance),
                        weight = 1f
                    )
                }

                if (entry.paymentNumber < schedule.size) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.TableHeaderCell(text: String, weight: Float) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = Modifier.weight(weight)
    )
}

@Composable
private fun RowScope.TableDataCell(text: String, weight: Float) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 11.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.weight(weight)
    )
}
