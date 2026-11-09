package com.smartalerter.notification.ui

import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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