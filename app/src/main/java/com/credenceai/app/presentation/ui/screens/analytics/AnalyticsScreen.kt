package com.credenceai.app.presentation.ui.screens.analytics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.credenceai.app.R

// ─── Colors ───────────────────────────────────────────────────────────────────

private val PrimaryBlue    = Color(0xFF1A3A8F)
private val BrightBlue     = Color(0xFF2D5BE3)
private val LightBlue      = Color(0xFFE8EEF9)
private val BackgroundGray = Color(0xFFF5F6FA)
private val TextPrimary    = Color(0xFF1A1D2E)
private val TextSecondary  = Color(0xFF8A90A2)
private val DividerGray    = Color(0xFFECEEF3)
private val CreditGreen    = Color(0xFF27AE60)
private val DebitRed       = Color(0xFFE05252)
private val InsightGold    = Color(0xFFFFD700)

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun AnalyticsScreen(
    onNavigateToUncategorized: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Page Header (Filters) ──────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Period selector row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = ButtonDefaults.outlinedButtonBorder,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(uiState.selectedPeriod, fontSize = 13.sp,
                                fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(PrimaryBlue, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter",
                            tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // ── Uncategorized Banner ──────────────────────────────────────────
            if (uiState.showUncategorizedBanner) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = LightBlue,
                    onClick = onNavigateToUncategorized
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(BrightBlue.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null,
                                tint = BrightBlue, modifier = Modifier.size(20.dp))
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Uncategorized Activity", fontSize = 13.sp,
                                fontWeight = FontWeight.Bold, color = PrimaryBlue)
                            Text(
                                "You have ${uiState.uncategorizedCount} new transactions that need classification.",
                                fontSize = 12.sp, color = PrimaryBlue.copy(alpha = 0.75f), lineHeight = 16.sp
                            )
                        }
                        Button(
                            onClick = onNavigateToUncategorized,
                            colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Review\nNow", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                                color = Color.White, lineHeight = 14.sp)
                        }
                    }
                }
            }

            // ── Summary Card ──────────────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SummaryRow(
                        label       = "INCOME",
                        amount      = uiState.income,
                        change      = uiState.incomeChange,
                        isPositive  = uiState.incomePositive,
                        amountColor = CreditGreen
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SummaryRow(
                        label       = "EXPENSES",
                        amount      = uiState.expenses,
                        change      = uiState.expensesChange,
                        isPositive  = uiState.expensesPositive,
                        amountColor = DebitRed
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SummaryRow(
                        label       = "SAVINGS",
                        amount      = uiState.savings,
                        change      = uiState.savingsRate,
                        isPositive  = true,
                        amountColor = BrightBlue
                    )
                }
            }

            // ── Financial Insight Card ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF2D5BE3), Color(0xFF1A3A8F)))
                    )
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null,
                            tint = InsightGold, modifier = Modifier.size(16.dp))
                        Text("Financial Insight", fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.85f))
                    }
                    Text(uiState.insightTitle, fontSize = 18.sp,
                        fontWeight = FontWeight.Bold, color = Color.White, lineHeight = 24.sp)
                    Text(uiState.insightBody, fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.75f), lineHeight = 18.sp)
                    Spacer(Modifier.height(2.dp))
                    Button(
                        onClick = viewModel::onAnalyzeInsight,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.20f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(uiState.insightAction, fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }

            // ── Spending Trend ────────────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Spending Trend", fontSize = 16.sp,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(Modifier.size(8.dp).background(BrightBlue, CircleShape))
                            Text("Expenses", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    SpendingTrendChart(
                        points = uiState.spendingPoints,
                        modifier = Modifier.fillMaxWidth().height(140.dp)
                    )
                    // X-axis labels
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        uiState.spendingPoints.forEach { pt ->
                            Text(pt.label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // ── Category Breakdown ────────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Category Breakdown", fontSize = 16.sp,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        DonutChart(
                            slices = uiState.categorySlices,
                            centerLabel = "Total\n${uiState.totalSpendLabel}",
                            modifier = Modifier.size(130.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.categorySlices.forEach { slice ->
                                LegendItem(slice)
                            }
                        }
                    }
                }
            }

            // ── Top Merchants ─────────────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Top Merchants", fontSize = 16.sp,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        TextButton(
                            onClick = viewModel::onViewAllMerchants,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("View List ", fontSize = 13.sp, color = BrightBlue,
                                fontWeight = FontWeight.SemiBold)
                            Icon(Icons.Default.ChevronRight, contentDescription = null,
                                tint = BrightBlue, modifier = Modifier.size(16.dp))
                        }
                    }
                    uiState.topMerchants.forEachIndexed { index, merchant ->
                        if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        MerchantRow(merchant)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─── Summary Row ─────────────────────────────────────────────────────────────

@Composable
private fun SummaryRow(
    label: String,
    amount: String,
    change: String,
    isPositive: Boolean,
    amountColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
        Text(amount, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = amountColor)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(
                if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                contentDescription = null,
                tint = if (isPositive) CreditGreen else DebitRed,
                modifier = Modifier.size(14.dp)
            )
            Text(change, fontSize = 12.sp,
                color = if (isPositive) CreditGreen else DebitRed,
                fontWeight = FontWeight.Medium)
        }
    }
}

// ─── Spending Trend Chart ────────────────────────────────────────────────────

@Composable
private fun SpendingTrendChart(points: List<SpendingPoint>, modifier: Modifier = Modifier) {
    val lineColor = Color(0xFF2D5BE3)
    val fillStart = Color(0xFF2D5BE3).copy(alpha = 0.20f)
    val fillEnd   = Color(0xFF2D5BE3).copy(alpha = 0.00f)

    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas
        val w = size.width
        val h = size.height
        val pad = 16f

        val xs = points.indices.map { i -> pad + i * (w - pad * 2) / (points.size - 1) }
        val ys = points.map { pt -> h - pad - pt.value * (h - pad * 2) }

        // Build smooth path via cubic bezier
        val linePath = Path().apply {
            moveTo(xs[0], ys[0])
            for (i in 1 until xs.size) {
                val cpX1 = (xs[i - 1] + xs[i]) / 2f
                cubicTo(cpX1, ys[i - 1], cpX1, ys[i], xs[i], ys[i])
            }
        }

        // Fill under curve
        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(xs.last(), h)
            lineTo(xs.first(), h)
            close()
        }
        drawPath(
            fillPath,
            brush = Brush.verticalGradient(listOf(fillStart, fillEnd), startY = 0f, endY = h)
        )

        // Line
        drawPath(linePath, color = lineColor, style = Stroke(width = 2.5f, cap = StrokeCap.Round))

        // Dot at peak
        val maxIdx = points.indexOfFirst { it.value == points.maxOf { p -> p.value } }
        drawCircle(Color.White, radius = 7f, center = Offset(xs[maxIdx], ys[maxIdx]))
        drawCircle(lineColor, radius = 5f, center = Offset(xs[maxIdx], ys[maxIdx]))
    }
}

// ─── Donut Chart ─────────────────────────────────────────────────────────────

@Composable
private fun DonutChart(
    slices: List<CategorySlice>,
    centerLabel: String,
    modifier: Modifier = Modifier
) {
    val centerTextColor = MaterialTheme.colorScheme.onSurface
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.18f
            val inset  = stroke / 2f
            val rect   = androidx.compose.ui.geometry.Rect(inset, inset,
                size.width - inset, size.height - inset)
            var startAngle = -90f

            slices.forEach { slice ->
                val sweep = slice.percent * 360f
                drawArc(
                    color      = Color(slice.color),
                    startAngle = startAngle,
                    sweepAngle = sweep - 2f,    // small gap between slices
                    useCenter  = false,
                    style      = Stroke(width = stroke, cap = StrokeCap.Butt),
                    topLeft    = rect.topLeft,
                    size       = Size(rect.width, rect.height)
                )
                startAngle += sweep
            }
        }
        // Center text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Total", fontSize = 10.sp, color = labelColor)
            Text(
                centerLabel.lines().last(),
                fontSize = 14.sp, fontWeight = FontWeight.Bold, color = centerTextColor
            )
        }
    }
}

// ─── Legend Item ─────────────────────────────────────────────────────────────

@Composable
private fun LegendItem(slice: CategorySlice) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(10.dp).background(Color(slice.color), CircleShape))
        Text(slice.name, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(72.dp))
        Text(slice.displayPercent, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
    }
}

// ─── Merchant Row ─────────────────────────────────────────────────────────────

@Composable
private fun MerchantRow(merchant: MerchantItem) {
    val animatedBar by animateFloatAsState(
        targetValue = merchant.barPercent,
        animationSpec = tween(800),
        label = "bar_${merchant.name}"
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon placeholder
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(merchant.iconLabel, fontSize = 16.sp)
            }
            Text(
                merchant.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(merchant.amount, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedBar)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(BrightBlue)
            )
        }
    }
}
