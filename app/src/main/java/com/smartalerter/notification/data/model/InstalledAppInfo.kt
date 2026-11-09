package com.smartalerter.notification.data.model

import android.graphics.drawable.Drawable

/**
 * Represents an application installed on the user's Android device.
 */
data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable? = null,
    val isSystemApp: Boolean = false,
    val activeRuleCount: Int = 0
) {
    val initials: String
        get() = appName.trim().take(2).uppercase().ifEmpty { "AP" }
}