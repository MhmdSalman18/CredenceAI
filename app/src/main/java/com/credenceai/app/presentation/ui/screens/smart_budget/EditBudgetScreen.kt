package com.credenceai.app.presentation.ui.screens.smart_budget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import com.credenceai.app.ui.theme.BackgroundPage
import com.credenceai.app.ui.theme.BorderColor
import com.credenceai.app.ui.theme.BrandBlue
import com.credenceai.app.ui.theme.ErrorRed
import com.credenceai.app.ui.theme.SummaryBg
import com.credenceai.app.ui.theme.SurfaceWhite
import com.credenceai.app.ui.theme.TextHint
import com.credenceai.app.ui.theme.TextPrimary
import com.credenceai.app.ui.theme.TextSecondary
import com.credenceai.app.core.utils.CurrencyUtils
import kotlinx.coroutines.launch

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun EditBudgetScreen(
    viewModel: EditBudgetViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onBudgetSaved: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var isDeleteMode by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("📂") }
    val emojiList = listOf("🍴", "🚗", "🧾", "🛍", "🏠", "🎁", "💊", "🎮", "📚", "📂")

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
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .imePadding(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                item {
                    // Sub-title
                    Text(
                        text = "Plan your finances with precision for the upcoming month.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp,
                    )
                    Spacer(Modifier.height(20.dp))
                }

                item {
                    // ── Budget Name Input ──────────────────────────────────────────
                    SectionLabel("Budget Name")
                    Spacer(Modifier.height(8.dp))
                    BudgetNameInput(
                        value = uiState.budgetName,
                        onValueChange = viewModel::onBudgetNameChanged
                    )
                    Spacer(Modifier.height(20.dp))
                }

                item {
                    // ── Total Budget Input ────────────────────────────────────────
                    SectionLabel("Total Budget")
                    Spacer(Modifier.height(8.dp))
                    BudgetAmountInput(
                        value = uiState.totalBudget,
                        currency = uiState.currency,
                        onValueChange = viewModel::onTotalBudgetChanged
                    )
                    Spacer(Modifier.height(20.dp))
                }

                item {
                    // ── Auto Distribute Toggle ────────────────────────────────────
                    ToggleRow(
                        title = "Auto distribute budget",
                        subtitle = "Divide equally across categories",
                        checked = uiState.autoDistribute,
                        onCheckedChange = viewModel::onAutoDistributeToggled
                    )
                    Spacer(Modifier.height(20.dp))
                }

                item {
                    // ── Assigned / Remaining Summary ─────────────────────────────
                    AssignedRemainingSummary(
                        assigned = uiState.assignedAmount,
                        remaining = uiState.remainingAmount,
                        total = uiState.totalBudgetAsDouble,
                        currency = uiState.currency
                    )
                    Spacer(Modifier.height(24.dp))
                }

                item {
                    // ── Categories Header ────────────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionLabel("Categories")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RemoveCategoryButton(
                                isDeleteMode = isDeleteMode,
                                onClick = { isDeleteMode = !isDeleteMode }
                            )
                            Spacer(Modifier.width(8.dp))
                            AddCategoryButton(onClick = {
                                showAddCategoryDialog = true
                            })
                        }
                    }

                    if (showAddCategoryDialog) {
                        AlertDialog(
                            onDismissRequest = { showAddCategoryDialog = false },
                            title = { Text("Add Category") },
                            text = {
                                Column {
                                    Text("Select an icon:", fontSize = 14.sp)
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        emojiList.take(5).forEach { emoji ->
                                            EmojiSelectionItem(
                                                emoji = emoji,
                                                isSelected = selectedEmoji == emoji,
                                                onSelect = { selectedEmoji = emoji }
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        emojiList.drop(5).forEach { emoji ->
                                            EmojiSelectionItem(
                                                emoji = emoji,
                                                isSelected = selectedEmoji == emoji,
                                                onSelect = { selectedEmoji = emoji }
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(16.dp))
                                    Text("Enter category name:", fontSize = 14.sp)
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
                                            viewModel.onAddCategory(newCategoryName, selectedEmoji)
                                            newCategoryName = ""
                                            selectedEmoji = "📂"
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
                }

                // ── Category List Items ──────────────────────────────────────
                items(
                    items = uiState.categories,
                    key = { it.id }
                ) { category ->
                    val index = uiState.categories.indexOf(category)
                    val isLast = index == uiState.categories.lastIndex
                    
                    CategoryRowCard(
                        category = category,
                        readOnly = uiState.autoDistribute,
                        isDeleteMode = isDeleteMode,
                        isLast = isLast,
                        currency = uiState.currency,
                        onAmountChanged = { newAmount ->
                            viewModel.onCategoryAmountChanged(category.id, newAmount)
                        },
                        onRemove = { viewModel.onRemoveCategory(category.id) }
                    )
                }

                item {
                    Spacer(Modifier.height(32.dp))
                }
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
    currency: String,
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
                text = CurrencyUtils.extractSymbol(currency),
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
private fun AssignedRemainingSummary(assigned: Double, remaining: Double, total: Double, currency: String) {
    val progress by animateFloatAsState(
        targetValue = if (total > 0) (assigned / total).toFloat().coerceIn(0f, 1f) else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing)
    )
    val progressColor by animateColorAsState(
        targetValue = when {
            remaining < 0 -> ErrorRed
            progress > 0.9f -> Color(0xFFFBC02D) // Warning yellow
            else -> BrandBlue
        }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SummaryBg)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SummaryItem(label = "Assigned", amount = assigned, currency = currency, amountColor = TextPrimary)
            SummaryItem(
                label = "Remaining",
                amount = remaining,
                currency = currency,
                amountColor = if (remaining < 0) ErrorRed else BrandBlue,
                textAlign = TextAlign.End
            )
        }
        
        Spacer(Modifier.height(16.dp))
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = progressColor,
            trackColor = BorderColor
        )
        
        if (remaining < 0) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Budget exceeded by ${CurrencyUtils.formatAmount(kotlin.math.abs(remaining), currency)}",
                color = ErrorRed,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    amount: Double,
    currency: String,
    amountColor: Color,
    textAlign: TextAlign = TextAlign.Start,
) {
    Column(horizontalAlignment = if (textAlign == TextAlign.End) Alignment.End else Alignment.Start) {
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
        Spacer(Modifier.height(2.dp))
        Text(
            text = CurrencyUtils.formatAmount(amount, currency),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = amountColor
        )
    }
}

// ─── Category List ────────────────────────────────────────────────────────────

@Composable
private fun CategoryRowCard(
    category: BudgetCategory,
    readOnly: Boolean,
    isDeleteMode: Boolean,
    isLast: Boolean,
    currency: String,
    onAmountChanged: (String) -> Unit,
    onRemove: () -> Unit,
) {
    Surface(
        color = SurfaceWhite,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            CategoryRow(
                category = category,
                readOnly = readOnly,
                isDeleteMode = isDeleteMode,
                currency = currency,
                onAmountChanged = onAmountChanged,
                onRemove = onRemove
            )
            if (!isLast) {
                HorizontalDivider(
                    color = BorderColor,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(
    category: BudgetCategory,
    readOnly: Boolean,
    isDeleteMode: Boolean,
    currency: String,
    onAmountChanged: (String) -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isDeleteMode) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Remove",
                    tint = Color.Red.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

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

        // Currency prefix
        Text(
            text = CurrencyUtils.extractSymbol(currency),
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

@Composable
private fun EmojiSelectionItem(
    emoji: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (isSelected) BrandBlue.copy(alpha = 0.1f) else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (isSelected) BrandBlue else BorderColor,
                shape = CircleShape
            )
            .clickable { onSelect() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 18.sp)
    }
}

// ─── Add Category Button ──────────────────────────────────────────────────────

@Composable
private fun RemoveCategoryButton(isDeleteMode: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isDeleteMode) Color.Red.copy(alpha = 0.1f) else Color(0xFFF0F0F8),
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = null,
                tint = if (isDeleteMode) Color.Red else TextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = if (isDeleteMode) "Done" else "Remove",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDeleteMode) Color.Red else TextSecondary
            )
        }
    }
}

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