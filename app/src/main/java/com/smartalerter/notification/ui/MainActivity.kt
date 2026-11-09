package com.smartalerter.notification.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import com.smartalerter.notification.data.model.RuleEntity
import com.smartalerter.notification.ui.screens.AddEditRuleScreen
import com.smartalerter.notification.ui.screens.AppSelectionScreen
import com.smartalerter.notification.ui.screens.MainScreen
import com.smartalerter.notification.ui.viewmodel.MainViewModel
import com.smartalerter.notification.util.PermissionHelper

sealed class Screen {
    object Main : Screen()
    object AppSelection : Screen()
    data class AddEditRule(
        val packageName: String,
        val appName: String,
        val existingRule: RuleEntity? = null
    ) : Screen()
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Android 13+: the alarm's foreground notification needs this runtime permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }

                when (val screen = currentScreen) {
                    is Screen.Main -> {
                        MainScreen(
                            viewModel = viewModel,
                            onNavigateToAddRule = { currentScreen = Screen.AppSelection },
                            onNavigateToEditRule = { rule ->
                                currentScreen = Screen.AddEditRule(
                                    packageName = rule.packageName,
                                    appName = rule.appName,
                                    existingRule = rule
                                )
                            },
                            onOpenNotificationSettings = {
                                PermissionHelper.openNotificationListenerSettings(this@MainActivity)
                            },
                            onOpenDndSettings = {
                                PermissionHelper.openDndSettings(this@MainActivity)
                            }
                        )
                    }

                    is Screen.AppSelection -> {
                        AppSelectionScreen(
                            viewModel = viewModel,
                            onAppSelected = { appInfo ->
                                currentScreen = Screen.AddEditRule(
                                    packageName = appInfo.packageName,
                                    appName = appInfo.appName,
                                    existingRule = null
                                )
                            },
                            onNavigateBack = { currentScreen = Screen.Main }
                        )
                    }

                    is Screen.AddEditRule -> {
                        AddEditRuleScreen(
                            packageName = screen.packageName,
                            appName = screen.appName,
                            existingRule = screen.existingRule,
                            onSaveRule = { rule ->
                                viewModel.saveRule(rule)
                            },
                            onNavigateBack = { currentScreen = Screen.Main }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkPermissions()
    }
}