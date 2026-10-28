package com.credenceai.credence_ai_flutter.notifications

import android.app.Notification
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import io.flutter.plugin.common.EventChannel
import java.util.concurrent.Executors

class TransactionNotificationListener : NotificationListenerService() {
    private val worker = Executors.newSingleThreadExecutor()
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!getSharedPreferences("credence_tracking", MODE_PRIVATE).getBoolean("enabled", true)) return
        if (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.takeIf { it.isNotBlank() }
            ?: extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val record = TransactionParser.parse(sbn.packageName, title, text, sbn.postTime, sbn.key) ?: return
        worker.execute {
            try {
                NotificationQueue(this).use { it.enqueue(record) }
                Handler(Looper.getMainLooper()).post { eventSink?.success(true) }
            } catch (_: Exception) {
                // Never log payment messages or account information.
                Handler(Looper.getMainLooper()).post { eventSink?.error("INBOX_WRITE", "Unable to capture transaction", null) }
            }
        }
    }
    override fun onDestroy() { worker.shutdown(); super.onDestroy() }
    companion object { @Volatile var eventSink: EventChannel.EventSink? = null }
}
