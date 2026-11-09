package com.smartalerter.notification

import android.app.Application
import com.smartalerter.notification.data.db.AppDatabase

class SmartAlerterApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppDatabase.getInstance(this)
    }
}
