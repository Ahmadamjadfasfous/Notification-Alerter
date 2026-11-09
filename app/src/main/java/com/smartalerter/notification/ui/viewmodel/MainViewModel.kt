package com.smartalerter.notification.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartalerter.notification.data.db.AppDatabase
import com.smartalerter.notification.data.model.InstalledAppInfo
import com.smartalerter.notification.data.model.RuleEntity
import com.smartalerter.notification.util.AppListFetcher
import com.smartalerter.notification.util.PermissionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MainUiState(
    val rules: List<RuleEntity> = emptyList(),
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val filteredApps: List<InstalledAppInfo> = emptyList(),
    val isLoadingApps: Boolean = true,
    val searchQuery: String = "",
    val isNotificationAccessGranted: Boolean = false,
    val isDndAccessGranted: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val ruleDao = database.ruleDao()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        observeRules()
        loadInstalledApps()
        checkPermissions()
    }

    private fun observeRules() {
        viewModelScope.launch {
            ruleDao.getAllRules().collectLatest { rulesList ->
                _uiState.update { state ->
                    val updatedApps = recalculateAppRuleCounts(state.installedApps, rulesList)
                    state.copy(
                        rules = rulesList,
                        installedApps = updatedApps,
                        filteredApps = filterApps(updatedApps, state.searchQuery)
                    )
                }
            }
        }
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingApps = true) }
            val apps = AppListFetcher.getInstalledUserApps(getApplication())
            val currentRules = _uiState.value.rules
            val appsWithCounts = recalculateAppRuleCounts(apps, currentRules)

            _uiState.update { state ->
                state.copy(
                    installedApps = appsWithCounts,
                    filteredApps = filterApps(appsWithCounts, state.searchQuery),
                    isLoadingApps = false
                )
            }
        }
    }

    private fun recalculateAppRuleCounts(
        apps: List<InstalledAppInfo>,
        rules: List<RuleEntity>
    ): List<InstalledAppInfo> {
        val countMap = rules.filter { it.isEnabled }.groupingBy { it.packageName }.eachCount()
        return apps.map { app ->
            app.copy(activeRuleCount = countMap[app.packageName] ?: 0)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredApps = filterApps(state.installedApps, query)
            )
        }
    }

    private fun filterApps(apps: List<InstalledAppInfo>, query: String): List<InstalledAppInfo> {
        if (query.isBlank()) return apps
        val trimmed = query.trim().lowercase()
        return apps.filter {
            it.appName.lowercase().contains(trimmed) || it.packageName.lowercase().contains(trimmed)
        }
    }

    fun saveRule(rule: RuleEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            ruleDao.insertRule(rule)
        }
    }

    fun toggleRule(ruleId: Long, isEnabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            ruleDao.toggleRuleEnabled(ruleId, isEnabled)
        }
    }

    fun deleteRule(rule: RuleEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            ruleDao.deleteRule(rule)
        }
    }

    fun checkPermissions() {
        val context = getApplication<Application>()
        val notifGranted = PermissionHelper.isNotificationAccessGranted(context)
        val dndGranted = PermissionHelper.isDndAccessGranted(context)

        _uiState.update { state ->
            state.copy(
                isNotificationAccessGranted = notifGranted,
                isDndAccessGranted = dndGranted
            )
        }
    }
}