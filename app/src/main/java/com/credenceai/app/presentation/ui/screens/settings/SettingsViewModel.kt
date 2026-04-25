package com.credenceai.app.presentation.ui.screens.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ─── Models ───────────────────────────────────────────────────────────────────

data class UserProfile(
    val name: String  = "Julian Sinclair",
    val email: String = "julian.sinclair@wealth.private",
    val avatarUrl: String? = null
)

// ─── UI State ─────────────────────────────────────────────────────────────────

data class SettingsUiState(
    val profile: UserProfile = UserProfile(),

    // App Preferences
    val isDarkMode: Boolean       = false,
    val currency: String          = "USD (\$)",
    val language: String          = "English (US)",

    // Notifications
    val transactionAlerts: Boolean = true,
    val budgetAlerts: Boolean      = true,
    val monthlyReports: Boolean    = false,

    // SMS Tracking
    val smsTrackingEnabled: Boolean = true,
    val supportedBanksCount: Int    = 12,

    // Security
    val appLockEnabled: Boolean = true,

    // App info
    val appVersion: String = "v2.4.0 (Gold)",

    // Dialogs
    val showClearDataDialog: Boolean  = false,
    val showCurrencyPicker: Boolean   = false,
    val showLanguagePicker: Boolean   = false,
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // ── App Preferences ───────────────────────────────────────────────────────

    fun onDarkModeToggle(enabled: Boolean) {
        _uiState.update { it.copy(isDarkMode = enabled) }
        // TODO: apply theme via ThemeManager
    }

    fun onCurrencyClick() {
        _uiState.update { it.copy(showCurrencyPicker = true) }
    }

    fun onCurrencySelected(currency: String) {
        _uiState.update { it.copy(currency = currency, showCurrencyPicker = false) }
    }

    fun onLanguageClick() {
        _uiState.update { it.copy(showLanguagePicker = true) }
    }

    fun onLanguageSelected(language: String) {
        _uiState.update { it.copy(language = language, showLanguagePicker = false) }
    }

    // ── Notifications ─────────────────────────────────────────────────────────

    fun onTransactionAlertsToggle(enabled: Boolean) {
        _uiState.update { it.copy(transactionAlerts = enabled) }
    }

    fun onBudgetAlertsToggle(enabled: Boolean) {
        _uiState.update { it.copy(budgetAlerts = enabled) }
    }

    fun onMonthlyReportsToggle(enabled: Boolean) {
        _uiState.update { it.copy(monthlyReports = enabled) }
    }

    // ── SMS Tracking ──────────────────────────────────────────────────────────

    fun onSmsTrackingToggle(enabled: Boolean) {
        _uiState.update { it.copy(smsTrackingEnabled = enabled) }
        // TODO: request SMS permission if enabling
    }

    fun onSupportedBanksClick() {
        // TODO: navigate to supported banks list
    }

    fun onManageUncategorizedClick() {
        // TODO: navigate to uncategorized screen
    }

    // ── Data & Backup ─────────────────────────────────────────────────────────

    fun onBackupRestoreClick() {
        // TODO: trigger backup/restore flow
    }

    fun onExportDataClick() {
        // TODO: export CSV/PDF
    }

    fun onClearDataClick() {
        _uiState.update { it.copy(showClearDataDialog = true) }
    }

    fun onClearDataConfirmed() {
        _uiState.update { it.copy(showClearDataDialog = false) }
        // TODO: clear local database via repository
    }

    fun onClearDataDismissed() {
        _uiState.update { it.copy(showClearDataDialog = false) }
    }

    // ── Security ──────────────────────────────────────────────────────────────

    fun onAppLockToggle(enabled: Boolean) {
        _uiState.update { it.copy(appLockEnabled = enabled) }
        // TODO: enable/disable biometric or PIN lock
    }

    fun onChangePinClick() {
        // TODO: navigate to PIN change screen
    }

    // ── About ─────────────────────────────────────────────────────────────────

    fun onPrivacyPolicyClick() {
        // TODO: open privacy policy URL
    }

    fun onSupportClick() {
        // TODO: open support screen or email intent
    }
}