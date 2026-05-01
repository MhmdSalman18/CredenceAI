package com.credenceai.app.presentation.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.credenceai.app.core.preferences.PreferencesManager
import com.credenceai.app.domain.usecase.ClearAllTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── Models ───────────────────────────────────────────────────────────────────

data class UserProfile(
    val name: String  = "Julian Sinclair",
    val avatarUrl: String? = null
)

sealed class SettingsEffect {
    data class ExportData(val csvData: String) : SettingsEffect()
}

// ─── UI State ─────────────────────────────────────────────────────────────────

data class SettingsUiState(
    val profile: UserProfile = UserProfile(),

    // App Preferences
    val isDarkMode: Boolean       = false,
    val currency: String          = "INR (₹)",
    val language: String          = "English (IN)",

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
    val appVersion: String = "v1.0.0",

    // Dialogs
    val showClearDataDialog: Boolean  = false,
    val showCurrencyPicker: Boolean   = false,
    val showLanguagePicker: Boolean   = false,
    val showEditNameDialog: Boolean   = false,
    val tempName: String              = "",
    val openUrl: String?              = null
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val clearAllTransactionsUseCase: ClearAllTransactionsUseCase,
    private val exportTransactionsUseCase: com.credenceai.app.domain.usecase.ExportTransactionsUseCase
) : ViewModel() {

    private val _effect = MutableSharedFlow<SettingsEffect>()
    val effect: SharedFlow<SettingsEffect> = _effect.asSharedFlow()

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = combine(
        _uiState,
        preferencesManager.isDarkMode,
        preferencesManager.userName,
        preferencesManager.currency,
        preferencesManager.isSmsTrackingEnabled,
        preferencesManager.language,
        preferencesManager.transactionAlerts,
        preferencesManager.budgetAlerts,
        preferencesManager.monthlyReports,
        preferencesManager.appLockEnabled
    ) { flows ->
        val state = flows[0] as SettingsUiState
        state.copy(
            isDarkMode = flows[1] as Boolean,
            profile = state.profile.copy(name = flows[2] as String),
            currency = flows[3] as String,
            smsTrackingEnabled = flows[4] as Boolean,
            language = flows[5] as String,
            transactionAlerts = flows[6] as Boolean,
            budgetAlerts = flows[7] as Boolean,
            monthlyReports = flows[8] as Boolean,
            appLockEnabled = flows[9] as Boolean
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun onEditNameClick() {
        _uiState.update { it.copy(showEditNameDialog = true, tempName = uiState.value.profile.name) }
    }

    fun onTempNameChange(newName: String) {
        _uiState.update { it.copy(tempName = newName) }
    }

    fun onSaveName() {
        viewModelScope.launch {
            preferencesManager.setUserName(_uiState.value.tempName)
            _uiState.update { it.copy(showEditNameDialog = false) }
        }
    }

    fun onEditNameDismissed() {
        _uiState.update { it.copy(showEditNameDialog = false) }
    }

    // ── App Preferences ───────────────────────────────────────────────────────

    fun onDarkModeToggle(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setDarkMode(enabled)
        }
    }

    fun onCurrencyClick() {
        _uiState.update { it.copy(showCurrencyPicker = true) }
    }

    fun onCurrencySelected(currency: String) {
        viewModelScope.launch {
            preferencesManager.setCurrency(currency)
            _uiState.update { it.copy(showCurrencyPicker = false) }
        }
    }

    fun onLanguageClick() {
        _uiState.update { it.copy(showLanguagePicker = true) }
    }

    fun onLanguageSelected(language: String) {
        viewModelScope.launch {
            preferencesManager.setLanguage(language)
            _uiState.update { it.copy(showLanguagePicker = false) }
        }
    }

    // ── Notifications ─────────────────────────────────────────────────────────

    fun onTransactionAlertsToggle(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setTransactionAlerts(enabled)
        }
    }

    fun onBudgetAlertsToggle(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setBudgetAlerts(enabled)
        }
    }

    fun onMonthlyReportsToggle(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setMonthlyReports(enabled)
        }
    }

    // ── SMS Tracking ──────────────────────────────────────────────────────────

    fun onSmsTrackingToggle(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setSmsTrackingEnabled(enabled)
        }
    }

    fun onSupportedBanksClick() {
        // This usually opens a WebView or a static list screen
        // For now, let's keep it as is, or maybe we could use an effect to show a list
    }

    fun onManageUncategorizedClick() {
        // TODO: navigate to uncategorized screen
    }

    // ── Data & Backup ─────────────────────────────────────────────────────────

    fun onBackupRestoreClick() {
        // TODO: trigger backup/restore flow
    }

    fun onExportDataClick() {
        viewModelScope.launch {
            val csvData = exportTransactionsUseCase()
            _effect.emit(SettingsEffect.ExportData(csvData))
        }
    }

    fun onClearDataClick() {
        _uiState.update { it.copy(showClearDataDialog = true) }
    }

    fun onClearDataConfirmed() {
        viewModelScope.launch {
            clearAllTransactionsUseCase()
            _uiState.update { it.copy(showClearDataDialog = false) }
        }
    }

    fun onClearDataDismissed() {
        _uiState.update { it.copy(showClearDataDialog = false) }
    }

    // ── Security ──────────────────────────────────────────────────────────────

    fun onAppLockToggle(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAppLockEnabled(enabled)
        }
    }

    fun onChangePinClick() {
        // TODO: navigate to PIN change screen
    }

    // ── About ─────────────────────────────────────────────────────────────────

    fun onPrivacyPolicyClick() {
        _uiState.update { it.copy(openUrl = "https://credencecapital.com/privacy") }
    }

    fun onSupportClick() {
        _uiState.update { it.copy(openUrl = "mailto:support@credencecapital.com") }
    }

    fun onUrlOpened() {
        _uiState.update { it.copy(openUrl = null) }
    }
}