package com.smartalerter.notification.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MatchType {
    CONTAINS,
    EXACT,
    REGEX
}

enum class MatchTarget {
    ANY,
    TITLE_ONLY,
    BODY_ONLY
}

enum class VibrationPattern {
    CONTINUOUS,
    SOS,
    RAPID_PULSE,
    HEARTBEAT,
    NONE
}

@Entity(tableName = "alert_rules")
data class RuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Target Application
    val appName: String,
    val packageName: String,
    
    // Keyword or Contact matching filter
    val keywordOrContact: String,
    val matchType: MatchType = MatchType.CONTAINS,
    val matchTarget: MatchTarget = MatchTarget.ANY,
    val isCaseSensitive: Boolean = false,
    
    // Ringtone / Song configuration
    val soundUri: String? = null, // null for default loud siren, or Content URI from device storage
    val soundTitle: String = "Emergency Siren",
    val bypassSilentMode: Boolean = true,
    val volumePercent: Int = 100, // 0 - 100
    
    // Vibration configuration
    val vibrationEnabled: Boolean = true,
    val vibrationPattern: VibrationPattern = VibrationPattern.SOS,
    
    // Operational flags
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)