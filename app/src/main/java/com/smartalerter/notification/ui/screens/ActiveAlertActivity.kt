package com.smartalerter.notification.ui.screens

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartalerter.notification.service.AlarmForegroundService

class ActiveAlertActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wakeAndUnlockScreen()

        // قراءة النصوص القادمة من الإشعار مع وضع نصوص افتراضية في حال كانت المتغيرات غير معرفة بالخدمة لمنع الخطأ
        val appName = intent.getStringExtra("EXTRA_APP_NAME") ?: intent.getStringExtra("appName") ?: "Discord / App Notification"
        val title = intent.getStringExtra("EXTRA_TITLE") ?: intent.getStringExtra("title") ?: "High Priority Alert"
        val body = intent.getStringExtra("EXTRA_BODY") ?: intent.getStringExtra("body") ?: "لديك رسالة هامة جديدة تتوافق مع الشروط المخصصة."

        setContent {
            ActiveAlertOverlay(
                appName = appName,
                title = title,
                body = body,
                onDismiss = {
                    dismissAlarm()
                }
            )
        }
    }

    private fun wakeAndUnlockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }

    private fun dismissAlarm() {
        val dismissIntent = Intent(this, AlarmForegroundService::class.java)
        // محاولة إيقاف الخدمة مباشرة لضمان كتم الصوت فورا
        stopService(dismissIntent)
        finish()
    }
}

@Composable
fun ActiveAlertOverlay(
    appName: String,
    title: String,
    body: String,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF140505)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .scale(pulseScale)
                        .background(Color(0xFFE53935).copy(alpha = 0.22f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(105.dp)
                            .background(Color(0xFFE53935), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // استبدال الأيقونة المفقودة بأيقونة الإشعارات المدمجة والجميلة
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = "Active Alert",
                            tint = Color.White,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "CRITICAL NOTIFICATION",
                    color = Color(0xFFFF5252),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    letterSpacing = 2.dp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = appName,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.headlineLarge
                )

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF261010)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        if (title.isNotBlank()) {
                            Text(
                                text = title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Text(
                            text = body,
                            color = Color(0xFFE2D6D6),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
                shape = CircleShape
            ) {
                // استبدال أيقونة كتم الصوت بأيقونة NotificationsOff المدمجة والمضمونة
                Icon(Icons.Filled.NotificationsOff, contentDescription = null, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "SILENCE RINGTONE",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
