package com.credenceai.app.presentation.ui.screens.history

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.credenceai.app.R

// ─── Colors ───────────────────────────────────────────────────────────────────

private val PrimaryBlue    = Color(0xFF1A3A8F)
private val BackgroundGray = Color(0xFFF5F6FA)
private val TextPrimary    = Color(0xFF1A1D2E)
private val TextSecondary  = Color(0xFF8A90A2)
private val DividerGray    = Color(0xFFECEEF3)
private val UncatRed       = Color(0xFFE05252)
private val CreditGreen    = Color(0xFF27AE60)

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onAddExpense: () -> Unit = {},
    onAddIncome: () -> Unit = {},
    onEditTransaction: (String, Double, String, Long, String) -> Unit = { _, _, _, _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            // Add Expense FAB
            FloatingActionButton(
                onClick            = onAddExpense,
                containerColor     = PrimaryBlue,
                contentColor       = Color.White,
                shape              = CircleShape,
                elevation          = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Add Expense", modifier = Modifier.size(26.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // ── Header ────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Transactions",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Managing your financial flow",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Search bar
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        BasicSearchField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            placeholder = "Search transactions, merchants, or tags"
                        )
                    }
                }

                // Filter chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransactionFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = uiState.activeFilter == filter,
                            onClick  = { viewModel.onFilterChange(filter) },
                            label    = { Text(filter.label, fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor    = MaterialTheme.colorScheme.primary,
                                selectedLabelColor        = MaterialTheme.colorScheme.onPrimary,
                                containerColor            = MaterialTheme.colorScheme.surface,
                                labelColor                = MaterialTheme.colorScheme.onSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled               = true,
                                selected              = uiState.activeFilter == filter,
                                borderColor           = MaterialTheme.colorScheme.outlineVariant,
                                selectedBorderColor   = MaterialTheme.colorScheme.primary,
                                borderWidth           = 1.dp,
                                selectedBorderWidth   = 0.dp
                            ),
                            shape = RoundedCornerShape(50)
                        )
                    }
                }
            }

            // ── Transaction List ──────────────────────────────────────────
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                if (uiState.filteredGroups.isEmpty()) {
                    item {
                        EmptyState()
                    }
                } else {
                    uiState.filteredGroups.forEach { group ->
                        item(key = "header_${group.dateLabel}") {
                            DateHeader(group.dateLabel)
                        }
                        items(
                            items = group.transactions,
                            key   = { it.id }
                        ) { tx ->
                            TransactionRow(tx, onClick = {
                                val cleanAmount = tx.amount.replace("₹", "").replace("+", "").replace("-", "").trim().toDoubleOrNull() ?: 0.0
                                onEditTransaction(tx.id, cleanAmount, tx.merchantName, tx.timestamp, if(tx.isCredit) "credit" else "debit")
                            })
                        }
                    }
                }
            }
        }
    }
}

// ─── Date Header ─────────────────────────────────────────────────────────────

@Composable
private fun DateHeader(label: String) {
    Text(
        text     = label,
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    )
}

// ─── Transaction Row ──────────────────────────────────────────────────────────

@Composable
private fun TransactionRow(tx: TransactionItem, onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape  = RoundedCornerShape(14.dp),
        color  = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        onClick = onClick
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(tx.iconBackground, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(tx.icon, contentDescription = null, tint = tx.iconTint, modifier = Modifier.size(22.dp))
            }

            // Info
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    tx.merchantName,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Category badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (tx.isUncategorized) UncatRed.copy(alpha = 0.10f)
                        else tx.iconBackground
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            if (tx.isUncategorized) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = UncatRed,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                            Text(
                                tx.category,
                                color = if (tx.isUncategorized) UncatRed else tx.iconTint,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Text(
                        tx.time,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            // Amount
            Text(
                tx.amount,
                color = if (tx.isCredit) CreditGreen else MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─── Empty State ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            Icons.Default.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Text("No transactions found", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Text("Try adjusting your search or filters", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
    }
}

// ─── Basic Search TextField (no border/box decoration) ───────────────────────

@Composable
private fun BasicSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp
        ),
        decorationBox = { inner ->
            if (value.isEmpty()) {
                Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
            inner()
        },
        modifier = Modifier.fillMaxWidth()
    )
}
