package com.credenceai.app.presentation.ui.screens.smart_budget

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

// ─── Brand Colors ─────────────────────────────────────────────────────────────
private val BrandBlue      = Color(0xFF2A1DC4)
private val BackgroundLight = Color(0xFFF5F5FA)
private val SurfaceWhite   = Color(0xFFFFFFFF)
private val TextPrimary    = Color(0xFF0D0D1A)
private val TextSecondary  = Color(0xFF6B6B80)
private val HeroGradient   = Brush.verticalGradient(
    colors = listOf(Color(0xFFE0E0FF), Color(0xFFF5F5FA))
)

@Composable
fun SmartBudgetIntroScreen(
    viewModel: SmartBudgetIntroViewModel = hiltViewModel(),
    onSetMonthlyBudget: () -> Unit = {},
    onViewBudget: (String) -> Unit = {},
    onLearnHowItWorks: () -> Unit = {},
    heroPainter: Painter? = null,
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Trigger AI Creation logic when landing here if budgets exist or just as a helper?
    // Actually, the plan mentions "Verify AI distribution logic... aligns with user expectations for 'AI create'".
    // Let's assume 'AI create' is a feature we want to highlight.

    // Entrance animations
    val heroAlpha = remember { Animatable(0f) }
    val contentOffset = remember { Animatable(40f) }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            heroAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        }
        launch {
            kotlinx.coroutines.delay(300)
            contentOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
            contentAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(scrollState)
    ) {
        // ── Hero Section ─────────────────────────────────────────────────────
        // Note: TopBar removed — shared TopAppBar from MainScreen's Scaffold is used instead.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .alpha(heroAlpha.value)
        ) {
            // Gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(HeroGradient)
            )

            // Hero image
            if (heroPainter != null) {
                androidx.compose.foundation.Image(
                    painter = heroPainter,
                    contentDescription = "Smart Budget Illustration",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                HeroPlaceholderContent()
            }

            // AI Insights badge — top right
            FloatingBadge(
                label = "AI Insights",
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(14.dp)
                    )
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp)
            )

            // Analytics badge — bottom left
            FloatingBadge(
                label = "Analytics",
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Analytics,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(14.dp)
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp)
            )
        }

        // ── Body Content ─────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, contentOffset.value.dp.roundToPx()) }
                .alpha(contentAlpha.value)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.existingBudgets.isEmpty()) {
                EmptyBudgetContent(
                    onSetMonthlyBudget = onSetMonthlyBudget,
                    onLearnHowItWorks = onLearnHowItWorks
                )
            } else {
                ExistingBudgetsContent(
                    budgets = uiState.existingBudgets,
                    onViewBudget = onViewBudget,
                    onAddNewBudget = onSetMonthlyBudget
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun EmptyBudgetContent(
    onSetMonthlyBudget: () -> Unit,
    onLearnHowItWorks: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(36.dp))

        Text(
            text = "Track and control your spending with a smart budget",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Take the first step towards financial freedom. Connect your accounts and let our AI help you optimize your monthly savings.",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
        )

        Spacer(Modifier.height(36.dp))

        // Primary CTA
        Button(
            onClick = onSetMonthlyBudget,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandBlue,
                contentColor = SurfaceWhite
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text(
                text = "Start Smart Budget",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp
            )
        }

        Spacer(Modifier.height(20.dp))

        // Secondary CTA
        Text(
            text = "Learn how it works",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = BrandBlue,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onLearnHowItWorks
            )
        )
    }
}

@Composable
private fun ExistingBudgetsContent(
    budgets: List<ExistingBudgetSummary>,
    onViewBudget: (String) -> Unit,
    onAddNewBudget: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Budgets",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Add New Budget Icon
            Surface(
                onClick = onAddNewBudget,
                color = BrandBlue.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Add Budget",
                        tint = BrandBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        budgets.forEach { budget ->
            BudgetSummaryCard(
                budget = budget,
                onClick = { onViewBudget(budget.id) }
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun BudgetSummaryCard(
    budget: ExistingBudgetSummary,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = SurfaceWhite,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(BrandBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "💰", fontSize = 24.sp)
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = budget.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "${budget.categoryCount} categories",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            Text(
                text = "₹${"%,.0f".format(budget.totalBudget)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = BrandBlue
            )
        }
    }
}

// ─── Floating Badge ───────────────────────────────────────────────────────────
@Composable
private fun FloatingBadge(
    label: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = SurfaceWhite,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            icon()
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

// ─── Hero Placeholder (used when no image painter is supplied) ────────────────
@Composable
private fun HeroPlaceholderContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00B4D8).copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🐷", fontSize = 48.sp)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD700).copy(alpha = 0.55f))
                    )
                }
            }
        }
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun SmartBudgetIntroScreenPreview() {
    SmartBudgetIntroScreen()
}