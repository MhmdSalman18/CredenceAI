package com.credenceai.credence_ai_flutter.notifications

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONObject

/** A durable inbox survives Flutter engine shutdown. Entries leave only after acknowledgement. */
class NotificationQueue(context: Context) : SQLiteOpenHelper(context, "notification_inbox.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) { db.execSQL("CREATE TABLE pending (id TEXT PRIMARY KEY, payload TEXT NOT NULL, created_at INTEGER NOT NULL)") }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) { error("Missing notification queue migration") }
    fun enqueue(record: Map<String, Any?>) {
        val values = ContentValues().apply {
            put("id", record["id"] as String)
            put("payload", JSONObject(record).toString())
            put("created_at", record["date_ms"] as Long)
        }
        writableDatabase.insertWithOnConflict("pending", null, values, SQLiteDatabase.CONFLICT_IGNORE)
    }
    fun pending(): List<Map<String, Any?>> {
        val records = mutableListOf<Map<String, Any?>>()
        readableDatabase.query("pending", arrayOf("payload"), null, null, null, null, "created_at ASC", "200").use { cursor ->
            while (cursor.moveToNext()) {
                val json = JSONObject(cursor.getString(0))
                records.add(json.keys().asSequence().associateWith { key -> if (json.isNull(key)) null else json.get(key) })
            }
        }
        return records
    }
    fun acknowledge(id: String) { writableDatabase.delete("pending", "id = ?", arrayOf(id)) }
}
