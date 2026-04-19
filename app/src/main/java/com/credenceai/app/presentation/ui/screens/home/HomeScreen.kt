package com.credenceai.app.presentation.ui.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// ─── Colors ───────────────────────────────────────────────────────────────────

private val CardBlueStart = Color(0xFF2D5BE3)
private val CardBlueEnd   = Color(0xFF1A3BAA)
private val CardBlueMid   = Color(0xFF3A6FEF)
private val AccentGreen   = Color(0xFF4CD964)
private val BackgroundGray= Color(0xFFF2F4F8)
private val TextWhite     = Color.White
private val TextWhite70   = Color.White.copy(alpha = 0.70f)
private val TextDark      = Color(0xFF1A1A2E)
private val TextGray      = Color(0xFF8A94A6)
private val ActionBorder  = Color(0xFFDDE3F0)

// ─── HomeScreen ───────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onAddExpense: () -> Unit = {}          // ← navigation lambda injected by NavGraph
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SummaryCard(uiState = uiState)
            QuickActionsRow(
                onAddExpense   = onAddExpense,               // ← pass nav lambda here
                onAddIncome    = viewModel::onAddIncome,
                onExportReport = viewModel::onExportReport
            )
            CategoriesSection(
                categories = uiState.categories,
                onViewAll  = viewModel::onViewAllCategories
            )
        }
    }
}

// ─── Summary Card ─────────────────────────────────────────────────────────────

@Composable
private fun SummaryCard(uiState: HomeUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(CardBlueMid, CardBlueStart, CardBlueEnd)
                )
            )
            .padding(24.dp)
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-40).dp)
                .background(color = Color.White.copy(alpha = 0.05f), shape = CircleShape)
        )

        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "THIS MONTH SUMMARY",
                    color = TextWhite70,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color = Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.AttachMoney, contentDescription = "Balance",
                        tint = TextWhite, modifier = Modifier.size(18.dp))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = uiState.netBalance, color = TextWhite,
                    fontSize = 36.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
                Surface(shape = RoundedCornerShape(20.dp), color = AccentGreen.copy(alpha = 0.20f)) {
                    Text(text = uiState.changeLabel, color = AccentGreen,
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                LabeledAmount(label = "TOTAL INCOME", amount = uiState.totalIncome)
                LabeledAmount(label = "TOTAL SPENT",  amount = uiState.totalSpent, align = TextAlign.End)
            }

            BudgetProgressSection(progress = uiState.budgetProgress)
        }
    }
}

@Composable
private fun LabeledAmount(label: String, amount: String, align: TextAlign = TextAlign.Start) {
    Column(horizontalAlignment = if (align == TextAlign.End) Alignment.End else Alignment.Start) {
        Text(text = label, color = TextWhite70, fontSize = 10.sp,
            fontWeight = FontWeight.Medium, letterSpacing = 0.8.sp)
        Spacer(Modifier.height(2.dp))
        Text(text = amount, color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BudgetProgressSection(progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "budget_progress"
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Budget progress", color = TextWhite,
                fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(text = "${(progress * 100).toInt()}% used", color = TextWhite70, fontSize = 13.sp)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth().height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.20f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress).fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp)).background(Color.White)
            )
        }
    }
}

// ─── Quick Actions ────────────────────────────────────────────────────────────

@Composable
private fun QuickActionsRow(
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onExportReport: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ActionButton(modifier = Modifier.weight(1f), icon = Icons.Default.Add,
            label = "ADD\nEXPENSE", iconTint = CardBlueStart,
            borderColor = CardBlueStart.copy(alpha = 0.4f), onClick = onAddExpense, isPrimary = true)
        ActionButton(modifier = Modifier.weight(1f), icon = Icons.Default.AttachMoney,
            label = "ADD INCOME", iconTint = Color(0xFF27AE60),
            borderColor = ActionBorder, onClick = onAddIncome)
        ActionButton(modifier = Modifier.weight(1f), icon = Icons.Default.Download,
            label = "EXPORT\nREPORT", iconTint = Color(0xFF2D5BE3),
            borderColor = ActionBorder, onClick = onExportReport)
    }
}

@Composable
private fun ActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    iconTint: Color,
    borderColor: Color,
    onClick: () -> Unit,
    isPrimary: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = if (isPrimary)
            androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
        else
            androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        tonalElevation = 0.dp,
        shadowElevation = if (isPrimary) 4.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 18.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color = iconTint.copy(alpha = 0.10f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = label,
                    tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Text(text = label, color = TextDark, fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center, lineHeight = 14.sp)
        }
    }
}

// ─── Categories Section ───────────────────────────────────────────────────────

@Composable
private fun CategoriesSection(
    categories: List<CategoryItem>,
    onViewAll: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Categories", color = TextDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onViewAll, contentPadding = PaddingValues(0.dp)) {
                Text(text = "View All", color = CardBlueStart, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        val rows = categories.chunked(2)
        rows.forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { item ->
                    CategoryCard(item = item, modifier = Modifier.weight(1f))
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CategoryCard(item: CategoryItem, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, ActionBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color = item.iconTint.copy(alpha = 0.12f), shape = RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = item.icon, contentDescription = item.name,
                    tint = item.iconTint, modifier = Modifier.size(20.dp))
            }
            Text(text = item.name, color = TextGray, fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = item.amount, color = TextDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = item.spendPercent, color = TextGray, fontSize = 12.sp)
            }
        }
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF2F4F8)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen()
    }
}