package com.credenceai.app.presentation.ui.screens.smart_budget

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// ─── Brand Colors ─────────────────────────────────────────────────────────────
private val BrandBlue      = Color(0xFF2A1DC4)
private val BrandBlueDark  = Color(0xFF1A0F9C)
private val BackgroundPage = Color(0xFFF5F5FA)
private val SurfaceWhite   = Color(0xFFFFFFFF)
private val TextPrimary    = Color(0xFF0D0D1A)
private val TextSecondary  = Color(0xFF6B6B80)
private val BorderColor    = Color(0xFFE2E2EE)
private val GreenGood      = Color(0xFF2E7D32)
private val OrangeWarning  = Color(0xFFE65100)
private val RedExceeded    = Color(0xFFC62828)
private val GreenTrack     = Color(0xFF4CAF50)
private val OrangeTrack    = Color(0xFFFF6D00)
private val RedTrack       = Color(0xFFE53935)
private val AiBannerBg     = Color(0xFF2A1DC4)

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun ViewBudgetScreen(
    viewModel: ViewBudgetViewModel = viewModel(),
    onEditBudget: () -> Unit = {},
    onViewAnalytics: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    // Entrance animation
    val bodyAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        bodyAlpha.animateTo(1f, animationSpec = tween(450, easing = FastOutSlowInEasing))
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.onErrorDismissed()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPage)
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    color = BrandBlue,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(bodyAlpha.value)
                        .verticalScroll(scrollState)
                        .navigationBarsPadding()
                ) {
                    // ── Budget Summary Card ───────────────────────────────────
                    BudgetSummaryCard(
                        totalBudget = uiState.totalBudget,
                        remainingBudget = uiState.remainingBudget,
                        usagePercent = uiState.usagePercent,
                        usagePercentDisplay = uiState.usagePercentDisplay,
                        onEditClick = onEditBudget,
                    )

                    Spacer(Modifier.height(16.dp))

                    // ── Categories Section ────────────────────────────────────
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Categories",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            TextButton(onClick = onViewAnalytics) {
                                Text(
                                    text = "View Analytics",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrandBlue
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Category cards
                        Surface(
                            color = SurfaceWhite,
                            shape = RoundedCornerShape(16.dp),
                            tonalElevation = 0.dp,
                        ) {
                            Column {
                                uiState.categories.forEachIndexed { index, category ->
                                    CategoryProgressRow(category = category)
                                    if (index < uiState.categories.lastIndex) {
                                        HorizontalDivider(
                                            color = BorderColor,
                                            thickness = 0.5.dp,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // ── AI Advice Banner ──────────────────────────────────
                        AiAdviceBanner(advice = uiState.aiAdvice)

                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ─── Budget Summary Card ──────────────────────────────────────────────────────

@Composable
private fun BudgetSummaryCard(
    totalBudget: Double,
    remainingBudget: Double,
    usagePercent: Float,
    usagePercentDisplay: String,
    onEditClick: () -> Unit,
) {
    // Animate progress bar on first composition
    val animatedProgress by animateFloatAsState(
        targetValue = usagePercent,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "budgetProgress"
    )

    Surface(
        color = SurfaceWhite,
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly Budget",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )

                // Remaining pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFEEEDFE))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Remaining: ₹${"%.0f".format(remainingBudget).addCommas()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandBlue
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Total budget + edit button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "₹${"%.0f".format(totalBudget).addCommas()}",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit budget",
                        tint = BrandBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Usage overview row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Usage overview",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = usagePercentDisplay,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            Spacer(Modifier.height(6.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = BrandBlue,
                trackColor = Color(0xFFE2E2EE),
                strokeCap = StrokeCap.Round,
            )
        }
    }
}

// ─── Category Progress Row ────────────────────────────────────────────────────

@Composable
private fun CategoryProgressRow(category: BudgetCategoryProgress) {
    val animatedProgress by animateFloatAsState(
        targetValue = category.usagePercent,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "cat_${category.id}_progress"
    )

    val trackColor = when (category.status) {
        SpendingStatus.GOOD     -> GreenTrack
        SpendingStatus.WARNING  -> OrangeTrack
        SpendingStatus.EXCEEDED -> RedTrack
    }
    val remainingTextColor = when (category.status) {
        SpendingStatus.GOOD     -> GreenGood
        SpendingStatus.WARNING  -> OrangeWarning
        SpendingStatus.EXCEEDED -> RedExceeded
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0F0F8)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = category.iconEmoji, fontSize = 16.sp)
            }

            Spacer(Modifier.width(12.dp))

            // Name + spent label
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = category.spentLabel,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            // Remaining / Exceeded label
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = category.remainingLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = remainingTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
                if (category.status == SpendingStatus.EXCEEDED) {
                    Text(
                        text = "Exceeded",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = RedExceeded
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = { animatedProgress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = trackColor,
            trackColor = BorderColor,
            strokeCap = StrokeCap.Round,
        )
    }
}

// ─── AI Advice Banner ─────────────────────────────────────────────────────────

@Composable
private fun AiAdviceBanner(advice: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AiBannerBg)
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "AI Advice",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = advice,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f),
                lineHeight = 18.sp
            )
        }
    }
}

// ─── Helper: comma-format a number string ─────────────────────────────────────

private fun String.addCommas(): String {
    val num = this.toLongOrNull() ?: return this
    return "%,d".format(num)
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun ViewBudgetScreenPreview() {
    ViewBudgetScreen()
}