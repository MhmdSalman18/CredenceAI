package com.credenceai.credence_ai_flutter

import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import com.credenceai.credence_ai_flutter.notifications.NotificationQueue
import com.credenceai.credence_ai_flutter.notifications.TransactionNotificationListener

class MainActivity : FlutterFragmentActivity() {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "com.credenceai/notifications").setMethodCallHandler { call, result ->
            try {
                when (call.method) {
                    "hasAccess" -> {
                        val component = ComponentName(this, TransactionNotificationListener::class.java).flattenToString()
                        val enabled = Settings.Secure.getString(contentResolver, "enabled_notification_listeners").orEmpty().split(':')
                        result.success(component in enabled)
                    }
                    "openSettings" -> { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)); result.success(null) }
                    "setTracking" -> {
                        val enabled = call.arguments as? Boolean ?: false
                        getSharedPreferences("credence_tracking", MODE_PRIVATE).edit().putBoolean("enabled", enabled).apply()
                        result.success(null)
                    }
                    "pending" -> NotificationQueue(this).use { result.success(it.pending()) }
                    "acknowledge" -> NotificationQueue(this).use { it.acknowledge(call.arguments as String); result.success(null) }
                    else -> result.notImplemented()
                }
            } catch (_: Exception) { result.error("NOTIFICATION_BRIDGE", "Notification access failed", null) }
        }
        EventChannel(flutterEngine.dartExecutor.binaryMessenger, "com.credenceai/notification_events").setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventChannel.EventSink) { TransactionNotificationListener.eventSink = events }
            override fun onCancel(arguments: Any?) { TransactionNotificationListener.eventSink = null }
        })
    }
    override fun cleanUpFlutterEngine(flutterEngine: FlutterEngine) {
        TransactionNotificationListener.eventSink = null
        super.cleanUpFlutterEngine(flutterEngine)
    }
}
