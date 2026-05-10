package com.credenceai.app.presentation.ui.screens.smart_budget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

// ─── Brand Colors (shared; extract to theme file in production) ───────────────
private val BrandBlue     = Color(0xFF2A1DC4)
private val BackgroundPage = Color(0xFFF5F5FA)
private val SurfaceWhite  = Color(0xFFFFFFFF)
private val TextPrimary   = Color(0xFF0D0D1A)
private val TextSecondary = Color(0xFF6B6B80)
private val TextHint      = Color(0xFFAAAAAA)
private val BorderColor   = Color(0xFFE2E2EE)
private val SummaryBg     = Color(0xFFF0F0F8)
private val RemainingBlue = Color(0xFF2A1DC4)

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun EditBudgetScreen(
    viewModel: EditBudgetViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onBudgetSaved: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    // Entrance animation
    val bodyAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        bodyAlpha.animateTo(1f, animationSpec = tween(400, easing = FastOutSlowInEasing))
    }

    // Side effects
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onBudgetSaved()
            viewModel.onSaveHandled()
        }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(bodyAlpha.value)
        ) {
            // ── Top Bar ──────────────────────────────────────────────────────
            EditBudgetTopBar(onNavigateBack = onNavigateBack)

            // ── Scrollable Body ───────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .imePadding()
            ) {
                Spacer(Modifier.height(4.dp))

                // Sub-title
                Text(
                    text = "Plan your finances with precision for the upcoming month.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp,
                )

                Spacer(Modifier.height(20.dp))

                // ── Budget Name Input ──────────────────────────────────────────
                SectionLabel("Budget Name")
                Spacer(Modifier.height(8.dp))
                BudgetNameInput(
                    value = uiState.budgetName,
                    onValueChange = viewModel::onBudgetNameChanged
                )

                Spacer(Modifier.height(20.dp))

                // ── Total Budget Input ────────────────────────────────────────
                SectionLabel("Total Budget")
                Spacer(Modifier.height(8.dp))
                BudgetAmountInput(
                    value = uiState.totalBudget,
                    onValueChange = viewModel::onTotalBudgetChanged
                )

                Spacer(Modifier.height(20.dp))

                // ── Auto Distribute Toggle ────────────────────────────────────
                ToggleRow(
                    title = "Auto distribute budget",
                    subtitle = "Divide equally across categories",
                    checked = uiState.autoDistribute,
                    onCheckedChange = viewModel::onAutoDistributeToggled
                )

                Spacer(Modifier.height(20.dp))

                // ── Assigned / Remaining Summary ─────────────────────────────
                AssignedRemainingSummary(
                    assigned = uiState.assignedAmount,
                    remaining = uiState.remainingAmount
                )

                Spacer(Modifier.height(24.dp))

                // ── Categories ────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionLabel("Categories")
                    AddCategoryButton(onClick = {
                        showAddCategoryDialog = true
                    })
                }

                if (showAddCategoryDialog) {
                    AlertDialog(
                        onDismissRequest = { showAddCategoryDialog = false },
                        title = { Text("Add Category") },
                        text = {
                            Column {
                                Text("Enter a name for the new category:", fontSize = 14.sp)
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = newCategoryName,
                                    onValueChange = { newCategoryName = it },
                                    placeholder = { Text("e.g. Entertainment") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    if (newCategoryName.isNotBlank()) {
                                        viewModel.onAddCategory(newCategoryName)
                                        newCategoryName = ""
                                        showAddCategoryDialog = false
                                    }
                                }
                            ) {
                                Text("Add")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAddCategoryDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                Spacer(Modifier.height(12.dp))

                CategoryList(
                    categories = uiState.categories,
                    autoDistribute = uiState.autoDistribute,
                    onAmountChanged = viewModel::onCategoryAmountChanged
                )

                Spacer(Modifier.height(24.dp))

                // ── Repeat Every Month Toggle ─────────────────────────────────
                ToggleRow(
                    title = "Repeat every month",
                    subtitle = "Automatically reset budget on the 1st",
                    checked = uiState.repeatEveryMonth,
                    onCheckedChange = viewModel::onRepeatEveryMonthToggled
                )

                Spacer(Modifier.height(32.dp))
            }

            // ── Save Button ───────────────────────────────────────────────────
            SaveBudgetButton(
                isLoading = uiState.isLoading,
                onClick = viewModel::onSaveBudget
            )
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────

@Composable
private fun EditBudgetTopBar(onNavigateBack: () -> Unit) {
    Surface(color = SurfaceWhite, shadowElevation = 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBackIosNew,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = "Set Budget",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

// ─── Budget Name Input ────────────────────────────────────────────────────────

@Composable
private fun BudgetNameInput(
    value: String,
    onValueChange: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .background(SurfaceWhite)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            ),
            cursorBrush = SolidColor(BrandBlue),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text("e.g. Monthly Budget", fontSize = 16.sp, color = TextHint)
                }
                inner()
            }
        )
    }
}

// ─── Budget Amount Input ──────────────────────────────────────────────────────

@Composable
private fun BudgetAmountInput(
    value: String,
    onValueChange: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .background(SurfaceWhite)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "₹",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(Modifier.width(8.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                cursorBrush = SolidColor(BrandBlue),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text("0", fontSize = 18.sp, color = TextHint)
                    }
                    inner()
                }
            )
        }
    }
}

// ─── Toggle Row ───────────────────────────────────────────────────────────────

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SurfaceWhite,
                checkedTrackColor = BrandBlue,
                uncheckedThumbColor = SurfaceWhite,
                uncheckedTrackColor = BorderColor,
                uncheckedBorderColor = BorderColor,
            )
        )
    }
}

// ─── Assigned / Remaining Summary ─────────────────────────────────────────────

@Composable
private fun AssignedRemainingSummary(assigned: Double, remaining: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SummaryBg)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SummaryItem(label = "Assigned", amount = assigned, amountColor = TextPrimary)
        SummaryItem(
            label = "Remaining",
            amount = remaining,
            amountColor = if (remaining < 0) Color(0xFFE53935) else RemainingBlue,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun SummaryItem(
    label: String,
    amount: Double,
    amountColor: Color,
    textAlign: TextAlign = TextAlign.Start,
) {
    Column(horizontalAlignment = if (textAlign == TextAlign.End) Alignment.End else Alignment.Start) {
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
        Spacer(Modifier.height(2.dp))
        Text(
            text = "₹${"%,.0f".format(amount)}",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = amountColor
        )
    }
}

// ─── Category List ────────────────────────────────────────────────────────────

@Composable
private fun CategoryList(
    categories: List<BudgetCategory>,
    autoDistribute: Boolean,
    onAmountChanged: (String, String) -> Unit,
) {
    Surface(
        color = SurfaceWhite,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp,
    ) {
        Column {
            categories.forEachIndexed { index, category ->
                CategoryRow(
                    category = category,
                    readOnly = autoDistribute,
                    onAmountChanged = { newAmount ->
                        onAmountChanged(category.id, newAmount)
                    }
                )
                if (index < categories.lastIndex) {
                    HorizontalDivider(
                        color = BorderColor,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(
    category: BudgetCategory,
    readOnly: Boolean,
    onAmountChanged: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon circle
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SummaryBg),
            contentAlignment = Alignment.Center
        ) {
            Text(text = category.iconEmoji, fontSize = 16.sp)
        }

        Spacer(Modifier.width(12.dp))

        // Name
        Text(
            text = category.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )

        // ₹ prefix
        Text(
            text = "₹",
            fontSize = 14.sp,
            color = TextSecondary
        )
        Spacer(Modifier.width(4.dp))

        // Amount input
        val amountStr = if (category.allocatedAmount == 0.0) "" else "%.0f".format(category.allocatedAmount)
        BasicTextField(
            value = amountStr,
            onValueChange = { onAmountChanged(it) },
            enabled = !readOnly,
            textStyle = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (readOnly) TextSecondary else TextPrimary,
                textAlign = TextAlign.End
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            cursorBrush = SolidColor(BrandBlue),
            singleLine = true,
            modifier = Modifier.width(80.dp),
            decorationBox = { inner ->
                if (amountStr.isEmpty()) {
                    Text(
                        "0",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextHint,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                inner()
            }
        )
    }
}

// ─── Add Category Button ──────────────────────────────────────────────────────

@Composable
private fun AddCategoryButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color(0xFFEEEDFE),
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                tint = BrandBlue,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Add Category",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = BrandBlue
            )
        }
    }
}

// ─── Section Label ────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = TextSecondary
    )
}

// ─── Save Button ──────────────────────────────────────────────────────────────

@Composable
private fun SaveBudgetButton(isLoading: Boolean, onClick: () -> Unit) {
    Surface(color = SurfaceWhite, shadowElevation = 8.dp) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = { if (!isLoading) onClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandBlue,
                    contentColor = SurfaceWhite,
                    disabledContainerColor = BrandBlue.copy(alpha = 0.6f)
                ),
                enabled = !isLoading,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = SurfaceWhite,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        text = "Save Budget",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun EditBudgetScreenPreview() {
    EditBudgetScreen()
}