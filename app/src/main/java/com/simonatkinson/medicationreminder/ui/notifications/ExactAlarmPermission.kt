package com.simonatkinson.medicationreminder.ui.notifications

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

object ExactAlarmPermission {

    fun requestExactAlarmPermissionIntent(context: Context): Intent? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
        return Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = android.net.Uri.parse("package:${context.packageName}")
        }
    }
}
