package com.credenceai.app.presentation.ui.screens.smart_budget

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// ─── Brand Colors ────────────────────────────────────────────────────────────
private val BrandBlue = Color(0xFF2A1DC4)
private val BrandBlueDark = Color(0xFF1A0F9C)
private val BackgroundLight = Color(0xFFF5F5FA)
private val SurfaceWhite = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF0D0D1A)
private val TextSecondary = Color(0xFF6B6B80)

// ─── Hero Gradient ────────────────────────────────────────────────────────────
private val HeroGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF0D1B6E),
        Color(0xFF1A2FA8),
        Color(0xFF0077B6),
        Color(0xFF00B4D8),
    )
)

@Composable
fun SmartBudgetIntroScreen(
    onSetMonthlyBudget: () -> Unit = {},
    onLearnHowItWorks: () -> Unit = {},
    // Replace with your actual coil/glide image composable; using a placeholder here
    heroPainter: Painter? = null,
) {
    val scrollState = rememberScrollState()

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
        // ── Top Bar ──────────────────────────────────────────────────────────
        TopBar()

        // ── Hero Section ─────────────────────────────────────────────────────
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

            // Hero image — swap painterResource(R.drawable.img_piggy_bank) for your actual res
            if (heroPainter != null) {
                androidx.compose.foundation.Image(
                    painter = heroPainter,
                    contentDescription = "Smart Budget Illustration",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Placeholder when no image provided (dev preview)
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

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────
@Composable
private fun TopBar() {
    Surface(
        color = SurfaceWhite,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar — replace with AsyncImage / coil for real user photo
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8EAF6)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "C",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlue
                    )
                }

                Spacer(Modifier.width(10.dp))

                Text(
                    text = "CredenceAI",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue
                )
            }

            IconButton(onClick = { /* navigate to notifications */ }) {
                Icon(
                    imageVector = Icons.Outlined.NotificationsNone,
                    contentDescription = "Notifications",
                    tint = BrandBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
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
            // Simple decorative piggy bank stand-in using shapes
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