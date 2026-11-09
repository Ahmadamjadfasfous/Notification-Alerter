package com.smartalerter.notification.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartalerter.notification.data.model.MatchTarget
import com.smartalerter.notification.data.model.MatchType
import com.smartalerter.notification.data.model.RuleEntity
import com.smartalerter.notification.data.model.VibrationPattern
import com.smartalerter.notification.data.model.matchTargetEnum
import com.smartalerter.notification.data.model.matchTypeEnum
import com.smartalerter.notification.data.model.vibrationPatternEnum
import androidx.compose.foundation.horizontalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRuleScreen(
    packageName: String,
    appName: String,
    existingRule: RuleEntity? = null,
    onSaveRule: (RuleEntity) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    var keyword by remember { mutableStateOf(existingRule?.keywordOrContact ?: "") }
    var matchType by remember { mutableStateOf(existingRule?.matchTypeEnum() ?: MatchType.CONTAINS) }
    var matchTarget by remember { mutableStateOf(existingRule?.matchTargetEnum() ?: MatchTarget.ANY) }
    var isCaseSensitive by remember { mutableStateOf(existingRule?.isCaseSensitive ?: false) }
    
    var soundUriStr by remember { mutableStateOf(existingRule?.soundUri) }
    var soundTitle by remember { mutableStateOf(existingRule?.soundTitle ?: "Default Loud Alarm") }
    var bypassSilentMode by remember { mutableStateOf(existingRule?.bypassSilentMode ?: true) }
    var volumePercent by remember { mutableFloatStateOf((existingRule?.volumePercent ?: 100).toFloat()) }

    var vibrationEnabled by remember { mutableStateOf(existingRule?.vibrationEnabled ?: true) }
    var vibrationPattern by remember { mutableStateOf(existingRule?.vibrationPatternEnum() ?: VibrationPattern.SOS) }

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            soundUriStr = it.toString()
            soundTitle = "Custom Song: ${it.lastPathSegment ?: "Audio file"}"
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Fallback
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingRule != null) "Edit Rule" else "Configure Rule") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.List,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = appName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = packageName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("1. Text Matching Criteria", fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = keyword,
                        onValueChange = { keyword = it },
                        label = { Text("Contact Name or Keyword (e.g. 'Dad', 'P0', 'Emergency')") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Leave blank to trigger on all notifications") }
                    )

                    Text("Match Type:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MatchType.values().forEach { type ->
                            FilterChip(
                                selected = matchType == type,
                                onClick = { matchType = type },
                                label = { Text(type.name) }
                            )
                        }
                    }

                    Text("Search Target:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            MatchTarget.ANY to "Title & Body",
                            MatchTarget.TITLE_ONLY to "Title (Sender)",
                            MatchTarget.BODY_ONLY to "Body Text"
                        ).forEach { (target, label) ->
                            FilterChip(
                                selected = matchTarget == target,
                                onClick = { matchTarget = target },
                                label = { Text(label) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Case Sensitive", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = isCaseSensitive, onCheckedChange = { isCaseSensitive = it })
                    }
                }
            }

            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("2. Emergency Audio / Song", fontWeight = FontWeight.Bold)

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = soundTitle,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = if (soundUriStr != null) "Custom Audio File" else "System Alarm Sound",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(onClick = { mediaPickerLauncher.launch(arrayOf("audio/*")) }) {
                                Icon(Icons.Default.Folder, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pick Song")
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Bypass Silent & Do-Not-Disturb", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Uses STREAM_ALARM to ring at maximum volume",
style = MaterialTheme.typography.bodySmall,
color = MaterialTheme.colorScheme.onSurfaceVariant
)
}
Switch(checked = bypassSilentMode, onCheckedChange = { bypassSilentMode = it })
}
Column {
Text("Alarm Volume: ${volumePercent.toInt()}%", style = MaterialTheme.typography.bodySmall)
Slider(
value = volumePercent,
onValueChange = { volumePercent = it },
valueRange = 10f..100f
)
}
}
}
Card(shape = RoundedCornerShape(16.dp)) {
Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
Row(
modifier = Modifier.fillMaxWidth(),
horizontalArrangement = Arrangement.SpaceBetween,
verticalAlignment = Alignment.CenterVertically
) {
Text("3. Vibration Engine", fontWeight = FontWeight.Bold)
Switch(checked = vibrationEnabled, onCheckedChange = { vibrationEnabled = it })
}
if (vibrationEnabled) {
Text("Vibration Pattern:", style = MaterialTheme.typography.labelMedium)
Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
listOf(
VibrationPattern.SOS,
VibrationPattern.RAPID_PULSE,
VibrationPattern.CONTINUOUS,
VibrationPattern.HEARTBEAT
).forEach { pat ->
FilterChip(
selected = vibrationPattern == pat,
onClick = { vibrationPattern = pat },
label = { Text(pat.name) }
)
}
}
}
}
}
Spacer(modifier = Modifier.height(8.dp))
Button(
onClick = {
val rule = (existingRule ?: RuleEntity(
appName = appName,
packageName = packageName,
keywordOrContact = keyword
)).copy(
appName = appName,
packageName = packageName,
keywordOrContact = keyword,
matchType = matchType.name,
matchTarget = matchTarget.name,
isCaseSensitive = isCaseSensitive,
soundUri = soundUriStr,
soundTitle = soundTitle,
bypassSilentMode = bypassSilentMode,
volumePercent = volumePercent.toInt(),
vibrationEnabled = vibrationEnabled,
vibrationPattern = vibrationPattern.name,
isEnabled = true
)
onSaveRule(rule)
onNavigateBack()
},
modifier = Modifier
.fillMaxWidth()
.height(54.dp),
shape = RoundedCornerShape(16.dp)
) {
Icon(Icons.Default.Check, contentDescription = null)
Spacer(modifier = Modifier.width(8.dp))
Text(
if (existingRule != null) "Update Alert Rule" else "Save Alert Rule",
fontWeight = FontWeight.Bold
)
}
}
}
}
