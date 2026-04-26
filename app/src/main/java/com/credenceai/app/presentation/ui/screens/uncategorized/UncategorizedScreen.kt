package com.credenceai.app.presentation.ui.screens.uncategorized

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// ── Brand colours ──────────────────────────────────────────────────────────────
private val BrandBlue      = Color(0xFF1A3FBF)
private val BrandBlueDark  = Color(0xFF0F2A8A)
private val AccentGreen    = Color(0xFF22C55E)
private val DebitRed       = Color(0xFFDC2626)
private val SurfaceGray    = Color(0xFFF0F2F8)
private val BorderGray     = Color(0xFFE2E6F0)
private val SubtextGray    = Color(0xFF6B7280)
private val LeftAccent     = Color(0xFF2A52D4)

@Composable
fun UncategorizedScreen(
    onEditTransaction: (id: String, amount: Double, merchant: String, timestamp: Long, type: String) -> Unit = { _, _, _, _, _ -> },
    viewModel: UncategorizedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { CredenceTopBar() },
        containerColor = SurfaceGray
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item { HeroBanner() }
            item { Spacer(Modifier.height(20.dp)) }
            item {
                PendingReviewHeader(
                    count = uiState.transactions.size,
                    isAutoSyncActive = uiState.isAutoSyncActive
                )
            }
            item { Spacer(Modifier.height(12.dp)) }
            items(uiState.transactions, key = { it.id }) { transaction ->
                TransactionCard(
                    transaction = transaction,
                    onCategorySelected = { category ->
                        viewModel.setCategory(transaction.id, category)
                    },
                    onDelete = {
                        viewModel.deleteTransaction(transaction.id)
                    },
                    onEdit = {
                        onEditTransaction(
                            transaction.id,
                            transaction.amount,
                            transaction.merchantName,
                            transaction.rawTimestamp,
                            transaction.type
                        )
                    }
                )
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CredenceTopBar() {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD1D5DB)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocalMall,
                        contentDescription = "Profile",
                        tint = Color(0xFF4B5563),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Credence",
                    color = BrandBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = "Calendar",
                    tint = BrandBlue
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceGray)
    )
}

// ── Hero banner ───────────────────────────────────────────────────────────────

@Composable
private fun HeroBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(BrandBlue, BrandBlueDark)
                )
            )
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = "Uncategorized\nTransactions",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 32.sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Categorize transactions to include them in analytics and graphs.",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

// ── Pending review header ─────────────────────────────────────────────────────

@Composable
private fun PendingReviewHeader(count: Int, isAutoSyncActive: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "PENDING REVIEW ($count)",
            color = SubtextGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp
        )
        if (isAutoSyncActive) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(AccentGreen.copy(alpha = 0.15f))
                    .border(1.dp, AccentGreen.copy(alpha = 0.4f), RoundedCornerShape(50.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Auto-Sync Active",
                    color = AccentGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ── Transaction card ──────────────────────────────────────────────────────────

@Composable
private fun TransactionCard(
    transaction: Transaction,
    onCategorySelected: (TransactionCategory) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val cardBg by animateColorAsState(
        targetValue = if (transaction.category != null) Color.White else Color.White,
        animationSpec = tween(300),
        label = "cardBg"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(1.dp, BorderGray, RoundedCornerShape(14.dp))
            .clickable { expanded = !expanded }
    ) {
        Row {
            // Left accent bar
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(IntrinsicSize.Min)
                    .fillMaxHeight()
                    .background(
                        color = LeftAccent,
                        shape = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)
                    )
            )

            // Card content
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = transaction.iconType.toIcon(),
                            contentDescription = null,
                            tint = BrandBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    // Name + sub-label
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = transaction.merchantName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF111827),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = transaction.subLabel,
                            fontSize = 12.sp,
                            color = SubtextGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    // Amount + date/time
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${if (transaction.isCredit) "+" else "-"} ₹${
                                String.format(
                                    "%.2f",
                                    Math.abs(transaction.amount)
                                )
                            }",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = if (transaction.isCredit) AccentGreen else DebitRed
                        )
                        Text(
                            text = "${transaction.date}, ${transaction.time}",
                            fontSize = 11.sp,
                            color = SubtextGray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                    }
                }

                // Category chip row (expandable)
                if (expanded) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = BorderGray)
                    Spacer(Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select category",
                            fontSize = 12.sp,
                            color = SubtextGray,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = onEdit,
                                modifier = Modifier.size(32.dp).background(BrandBlue.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = BrandBlue, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier.size(32.dp).background(DebitRed.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = DebitRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    CategoryChipRow(
                        selectedCategory = transaction.category,
                        onCategorySelected = { category ->
                            onCategorySelected(category)
                            expanded = false
                        }
                    )
                }

                // Show selected category badge when collapsed
                if (!expanded && transaction.category != null) {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(BrandBlue.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = transaction.category.name,
                            color = BrandBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChipRow(
    selectedCategory: TransactionCategory?,
    onCategorySelected: (TransactionCategory) -> Unit
) {
    val categories = TransactionCategory.entries
    val chunked = categories.chunked(3)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        chunked.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { category ->
                    val isSelected = category == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) BrandBlue else SurfaceGray)
                            .border(
                                1.dp,
                                if (isSelected) BrandBlue else BorderGray,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onCategorySelected(category) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = if (isSelected) Color.White else Color(0xFF374151),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}



// ── Icon mapping ──────────────────────────────────────────────────────────────

private fun TransactionIconType.toIcon(): ImageVector = when (this) {
    TransactionIconType.SHOPPING      -> Icons.Outlined.LocalMall
    TransactionIconType.TRANSPORT     -> Icons.Outlined.DirectionsCar
    TransactionIconType.ENTERTAINMENT -> Icons.Outlined.PlayCircleOutline
    TransactionIconType.UTILITY       -> Icons.Outlined.Bolt
    TransactionIconType.OTHER         -> Icons.Outlined.CalendarMonth
}