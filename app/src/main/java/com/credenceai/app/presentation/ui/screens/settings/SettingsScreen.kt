package com.credenceai.app.presentation.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Intent
import android.net.Uri
import com.credenceai.app.core.utils.ExportUtils
import com.credenceai.app.ui.theme.*


// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToSupportedBanks: () -> Unit = {},
    onNavigateToUncategorized: () -> Unit = {},
    onNavigateToBackup: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SettingsEffect.ExportData -> {
                    ExportUtils.exportAndShareReport(context, effect.csvData)
                }
            }
        }
    }

    LaunchedEffect(uiState.openUrl) {
        uiState.openUrl?.let { url ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
            viewModel.onUrlOpened()
        }
    }

    // ── Edit Name Dialog ──────────────────────────────────────────────────
    if (uiState.showEditNameDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onEditNameDismissed,
            title = { Text("Edit Name", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = uiState.tempName,
                    onValueChange = viewModel::onTempNameChange,
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::onSaveName) {
                    Text("Save", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onEditNameDismissed) {
                    Text("Cancel")
                }
            }
        )
    }

    // ── Clear Data Confirmation Dialog ────────────────────────────────────
    if (uiState.showClearDataDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onClearDataDismissed,
            icon    = { Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed) },
            title   = { Text("Clear All Data?", fontWeight = FontWeight.Bold) },
            text    = { Text("This will permanently delete all your transactions, categories, and settings. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = viewModel::onClearDataConfirmed) {
                    Text("Clear Data", color = ErrorRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onClearDataDismissed) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ── Profile Header Card ───────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                )
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            // Shield watermark
            Icon(
                Icons.Default.Shield,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.07f),
                modifier = Modifier
                    .size(110.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 20.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            uiState.profile.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Standard Plan",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }

                IconButton(
                    onClick = viewModel::onEditNameClick,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Name",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // ── APP PREFERENCES ───────────────────────────────────────────────
        SettingsSection(title = "APP PREFERENCES") {
            ToggleRow(
                icon        = Icons.Default.DarkMode,
                label       = "Dark Mode",
                checked     = uiState.isDarkMode,
                onToggle    = viewModel::onDarkModeToggle
            )
            SettingsDivider()
            NavigationRow(
                icon    = Icons.Default.CurrencyExchange,
                label   = "Currency",
                value   = uiState.currency,
                onClick = viewModel::onCurrencyClick
            )
            SettingsDivider()
            NavigationRow(
                icon    = Icons.Default.Language,
                label   = "Language",
                value   = uiState.language,
                onClick = viewModel::onLanguageClick
            )
        }

        // ── NOTIFICATIONS ─────────────────────────────────────────────────
        SettingsSection(title = "NOTIFICATIONS") {
            ToggleRow(
                icon     = Icons.Default.NotificationsActive,
                label    = "Transaction alerts",
                checked  = uiState.transactionAlerts,
                onToggle = viewModel::onTransactionAlertsToggle
            )
            SettingsDivider()
            ToggleRow(
                icon     = Icons.Default.TrendingUp,
                label    = "Budget alerts",
                checked  = uiState.budgetAlerts,
                onToggle = viewModel::onBudgetAlertsToggle
            )
            SettingsDivider()
            ToggleRow(
                icon     = Icons.Default.Description,
                label    = "Monthly reports",
                checked  = uiState.monthlyReports,
                onToggle = viewModel::onMonthlyReportsToggle
            )
        }

        // ── SMS TRACKING ──────────────────────────────────────────────────
        SettingsSection(title = "SMS TRACKING") {
            ToggleRow(
                icon     = Icons.Default.Sms,
                label    = "Enable Tracking",
                checked  = uiState.smsTrackingEnabled,
                onToggle = viewModel::onSmsTrackingToggle
            )
            SettingsDivider()
            NavigationRow(
                icon    = Icons.Default.AccountBalance,
                label   = "Supported Banks",
                value   = "27 Banks",
                onClick = onNavigateToSupportedBanks
            )
            SettingsDivider()
            NavigationRow(
                icon    = Icons.Default.SwapHoriz,
                label   = "Manage Uncategorized",
                onClick = onNavigateToUncategorized
            )
        }

        // ── DATA & BACKUP ─────────────────────────────────────────────────
        SettingsSection(title = "DATA & BACKUP") {
            NavigationRow(
                icon    = Icons.Default.CloudSync,
                label   = "Backup & Restore",
                onClick = onNavigateToBackup
            )
            SettingsDivider()
            NavigationRow(
                icon    = Icons.Default.Download,
                label   = "Export Data",
                onClick = viewModel::onExportDataClick
            )
            SettingsDivider()
            // Destructive row — red label, warning icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onClearDataClick() }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SettingsIcon(icon = Icons.Default.DeleteForever, tint = ErrorRed)
                    Text("Clear Data", fontSize = 15.sp,
                        fontWeight = FontWeight.Medium, color = ErrorRed)
                }
                Icon(Icons.Default.Warning, contentDescription = null,
                    tint = ErrorRed, modifier = Modifier.size(20.dp))
            }
        }

        // ── SECURITY ──────────────────────────────────────────────────────
        SettingsSection(title = "SECURITY") {
            ToggleRow(
                icon     = Icons.Default.Lock,
                label    = "App Lock",
                checked  = uiState.appLockEnabled,
                onToggle = viewModel::onAppLockToggle
            )
            SettingsDivider()
            NavigationRow(
                icon    = Icons.Default.Pin,
                label   = "Change PIN",
                onClick = viewModel::onChangePinClick
            )
        }

        // ── ABOUT ─────────────────────────────────────────────────────────
        SettingsSection(title = "ABOUT") {
            NavigationRow(
                icon    = Icons.Default.Info,
                label   = "Version",
                value   = uiState.appVersion,
                valueColor = MaterialTheme.colorScheme.primary,
                showArrow = false,
                onClick = {}
            )
            SettingsDivider()
            NavigationRow(
                icon    = Icons.Default.PrivacyTip,
                label   = "Privacy Policy",
                trailingIcon = Icons.Default.OpenInNew,
                onClick = viewModel::onPrivacyPolicyClick
            )
            SettingsDivider()
            NavigationRow(
                icon    = Icons.Default.SupportAgent,
                label   = "Support",
                onClick = viewModel::onSupportClick
            )
        }

        // ── Footer ────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "DESIGNED FOR FINANCIAL EXCELLENCE",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Text(
                "© 2024 CREDENCE CAPITAL, LLC",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
        }
    }
}

// ─── Settings Section wrapper ─────────────────────────────────────────────────

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text(
            title,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column { content() }
        }
    }
}

// ─── Toggle Row ───────────────────────────────────────────────────────────────

@Composable
private fun ToggleRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SettingsIcon(icon = icon)
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor       = Color.White,
                checkedTrackColor       = GreenOn,
                uncheckedThumbColor     = Color.White,
                uncheckedTrackColor     = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                uncheckedBorderColor    = Color.Transparent,
                checkedBorderColor      = Color.Transparent
            ),
            modifier = Modifier.height(28.dp)
        )
    }
}

// ─── Navigation Row ───────────────────────────────────────────────────────────

@Composable
private fun NavigationRow(
    icon: ImageVector,
    label: String,
    value: String? = null,
    valueColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    trailingIcon: ImageVector = Icons.Default.ChevronRight,
    showArrow: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SettingsIcon(icon = icon)
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (value != null) {
                Text(value, fontSize = 14.sp, color = valueColor, fontWeight = FontWeight.Medium)
            }
            if (showArrow) {
                Icon(trailingIcon, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ─── Icon with tinted rounded background ─────────────────────────────────────

@Composable
private fun SettingsIcon(
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(tint.copy(alpha = 0.10f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
    }
}

// ─── Thin divider between rows ────────────────────────────────────────────────

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 68.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 0.8.dp
    )
}