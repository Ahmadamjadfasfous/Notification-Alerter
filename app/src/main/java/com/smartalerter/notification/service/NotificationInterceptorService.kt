package com.smartalerter.notification.service

import android.app.Notification
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.smartalerter.notification.data.db.AppDatabase
import com.smartalerter.notification.data.model.MatchTarget
import com.smartalerter.notification.data.model.MatchType
import com.smartalerter.notification.data.model.RuleEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class NotificationInterceptorService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(applicationContext)
        Log.i(TAG, "NotificationInterceptorService initialized and listening.")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName
        // Avoid intercepting our own alarm foreground notification
        if (packageName == applicationContext.packageName) return

        val notification = sbn.notification ?: return
        val extras: Bundle = notification.extras ?: return

        // Extract title & body texts safely
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
        val fullBody = if (bigText.isNotBlank()) bigText else text

        Log.d(TAG, "Notification from: $packageName | Title: '$title' | Body: '$fullBody'")

        serviceScope.launch {
            evaluateRules(packageName, title, fullBody)
        }
    }

    private suspend fun evaluateRules(packageName: String, title: String, body: String) {
        val rules = database.ruleDao().getEnabledRulesForPackage(packageName)
        if (rules.isEmpty()) return

        for (rule in rules) {
            if (isMatch(rule, title, body)) {
                Log.w(TAG, "MATCH FOUND! Rule '${rule.keywordOrContact}' triggered for app $packageName")
                triggerEmergencyAlert(rule, title, body)
                break // Trigger first matched rule
            }
        }
    }

    private fun isMatch(rule: RuleEntity, title: String, body: String): Boolean {
        val pattern = rule.keywordOrContact.trim()
        if (pattern.isEmpty()) return true // Match all notifications if no keyword set

        val checkText = { targetText: String ->
            when (rule.matchType) {
                MatchType.CONTAINS -> {
                    if (rule.isCaseSensitive) targetText.contains(pattern)
                    else targetText.contains(pattern, ignoreCase = true)
                }
                MatchType.EXACT -> {
                    if (rule.isCaseSensitive) targetText.trim() == pattern
                    else targetText.trim().equals(pattern, ignoreCase = true)
                }
                MatchType.REGEX -> {
                    try {
                        val options = if (rule.isCaseSensitive) emptySet() else setOf(RegexOption.IGNORE_CASE)
                        pattern.toRegex(options).containsMatchIn(targetText)
                    } catch (e: Exception) {
                        Log.e(TAG, "Invalid Regex in rule: $pattern", e)
                        false
                    }
                }
            }
        }

        return when (rule.matchTarget) {
            MatchTarget.TITLE_ONLY -> checkText(title)
            MatchTarget.BODY_ONLY -> checkText(body)
            MatchTarget.ANY -> checkText(title) || checkText(body)
        }
    }

    private fun triggerEmergencyAlert(rule: RuleEntity, title: String, body: String) {
        val intent = Intent(applicationContext, AlarmForegroundService::class.java).apply {
            action = AlarmForegroundService.ACTION_START_ALARM
            putExtra(AlarmForegroundService.EXTRA_RULE_ID, rule.id)
            putExtra(AlarmForegroundService.EXTRA_APP_NAME, rule.appName)
            putExtra(AlarmForegroundService.EXTRA_TITLE, title)
            putExtra(AlarmForegroundService.EXTRA_BODY, body)
            putExtra(AlarmForegroundService.EXTRA_SOUND_URI, rule.soundUri)
            putExtra(AlarmForegroundService.EXTRA_VIBRATION_PATTERN, rule.vibrationPattern.name)
            putExtra(AlarmForegroundService.EXTRA_BYPASS_DND, rule.bypassSilentMode)
            putExtra(AlarmForegroundService.EXTRA_VOLUME, rule.volumePercent)
        }
        applicationContext.startForegroundService(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        private const val TAG = "NotificationInterceptor"
    }
}