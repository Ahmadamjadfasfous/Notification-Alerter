package com.smartalerter.notification.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.smartalerter.notification.data.model.InstalledAppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppListFetcher {

    suspend fun getInstalledUserApps(context: Context): List<InstalledAppInfo> = withContext(Dispatchers.IO) {
        val packageManager: PackageManager = context.packageManager
        val installedAppsList = mutableListOf<InstalledAppInfo>()

        val appList: List<ApplicationInfo> = try {
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        } catch (e: Exception) {
            emptyList()
        }

        val launchablePackages: Set<String> = try {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            packageManager.queryIntentActivities(mainIntent, 0)
                .map { it.activityInfo.packageName }
                .toSet()
        } catch (e: Exception) {
            emptySet()
        }

        for (appInfo in appList) {
            if (appInfo.packageName == context.packageName) continue

            val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isUpdatedSystemApp = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            val isLaunchable = launchablePackages.contains(appInfo.packageName)

            if (!isSystemApp || isUpdatedSystemApp || isLaunchable) {
                try {
                    val label = packageManager.getApplicationLabel(appInfo).toString()
                    val icon = packageManager.getApplicationIcon(appInfo)

                    installedAppsList.add(
                        InstalledAppInfo(
                            packageName = appInfo.packageName,
                            appName = label,
                            icon = icon,
                            isSystemApp = isSystemApp && !isUpdatedSystemApp
                        )
                    )
                } catch (e: Exception) {
                    // Ignore transient errors
                }
            }
        }

        installedAppsList.sortedBy { it.appName.lowercase() }
    }
}