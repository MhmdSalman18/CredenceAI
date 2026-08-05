package com.credenceai.app.core.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    private val SMS_TRACKING_KEY = booleanPreferencesKey("sms_tracking")
    private val USER_NAME_KEY = stringPreferencesKey("user_name")
    private val CURRENCY_KEY = stringPreferencesKey("currency")
    private val LANGUAGE_KEY = stringPreferencesKey("language")
    private val TRANSACTION_ALERTS_KEY = booleanPreferencesKey("transaction_alerts")
    private val BUDGET_ALERTS_KEY = booleanPreferencesKey("budget_alerts")
    private val MONTHLY_REPORTS_KEY = booleanPreferencesKey("monthly_reports")
    private val APP_LOCK_KEY = booleanPreferencesKey("app_lock")
    private val MONTHLY_BUDGET_KEY = doublePreferencesKey("monthly_budget")
    private val HIDE_BALANCE_KEY = booleanPreferencesKey("hide_balance")

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: false
    }

    val isSmsTrackingEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SMS_TRACKING_KEY] ?: true
    }

    val userName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY] ?: "New User"
    }

    val currency: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CURRENCY_KEY] ?: "INR (₹)"
    }

    val language: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: "English (IN)"
    }

    val transactionAlerts: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[TRANSACTION_ALERTS_KEY] ?: true
    }

    val budgetAlerts: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[BUDGET_ALERTS_KEY] ?: true
    }

    val monthlyReports: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[MONTHLY_REPORTS_KEY] ?: false
    }

    val appLockEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[APP_LOCK_KEY] ?: true
    }

    val monthlyBudget: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[MONTHLY_BUDGET_KEY] ?: 0.0
    }

    val isBalanceHidden: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HIDE_BALANCE_KEY] ?: false
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    suspend fun setSmsTrackingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SMS_TRACKING_KEY] = enabled
        }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
        }
    }

    suspend fun setCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENCY_KEY] = currency
        }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }

    suspend fun setTransactionAlerts(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TRANSACTION_ALERTS_KEY] = enabled
        }
    }

    suspend fun setBudgetAlerts(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BUDGET_ALERTS_KEY] = enabled
        }
    }

    suspend fun setMonthlyReports(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MONTHLY_REPORTS_KEY] = enabled
        }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[APP_LOCK_KEY] = enabled
        }
    }

    suspend fun setMonthlyBudget(amount: Double) {
        context.dataStore.edit { preferences ->
            preferences[MONTHLY_BUDGET_KEY] = amount
        }
    }

    private val LAST_BACKUP_TIME_KEY = stringPreferencesKey("last_backup_time")

    val lastBackupTime: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LAST_BACKUP_TIME_KEY] ?: "Never"
    }

    suspend fun setLastBackupTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_BACKUP_TIME_KEY] = time
        }
    }

    suspend fun setBalanceHidden(hidden: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HIDE_BALANCE_KEY] = hidden
        }
    }
}
