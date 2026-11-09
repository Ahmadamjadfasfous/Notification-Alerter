package com.smartalerter.notification.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MatchType { CONTAINS, EXACT, REGEX }
enum class MatchTarget { ANY, TITLE_ONLY, BODY_ONLY }
enum class VibrationPattern { CONTINUOUS, SOS, RAPID_PULSE, HEARTBEAT, NONE }

@Entity(tableName = "alert_rules")
data class RuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appName: String,
    val packageName: String,
    val keywordOrContact: String,
    val matchType: String = "CONTAINS",
    val matchTarget: String = "ANY",
    val isCaseSensitive: Boolean = false,
    val soundUri: String? = null,
    val soundTitle: String = "Emergency Siren",
    val bypassSilentMode: Boolean = true,
    val volumePercent: Int = 100,
    val vibrationEnabled: Boolean = true,
    val vibrationPattern: String = "SOS",
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)


// Room stores these as Strings; these helpers convert them safely to enums.
fun RuleEntity.matchTypeEnum(): MatchType =
    runCatching { MatchType.valueOf(matchType) }.getOrDefault(MatchType.CONTAINS)

fun RuleEntity.matchTargetEnum(): MatchTarget =
    runCatching { MatchTarget.valueOf(matchTarget) }.getOrDefault(MatchTarget.ANY)

fun RuleEntity.vibrationPatternEnum(): VibrationPattern =
    runCatching { VibrationPattern.valueOf(vibrationPattern) }.getOrDefault(VibrationPattern.SOS)
