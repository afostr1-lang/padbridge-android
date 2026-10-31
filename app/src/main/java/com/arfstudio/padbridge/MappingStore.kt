package com.arfstudio.padbridge

import android.content.Context
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class TouchBinding(val id: Long, val packageName: String, val button: String, val x: Float, val y: Float, val mode: String)

/** Schema v2 adds normalized touch targets without deleting existing game profiles. */
class MappingStore(context: Context) : SQLiteOpenHelper(context, "padbridge-mappings.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE mappings (id INTEGER PRIMARY KEY AUTOINCREMENT, package_name TEXT NOT NULL, button TEXT NOT NULL, x REAL NOT NULL, y REAL NOT NULL, mode TEXT NOT NULL DEFAULT 'tap')")
        db.execSQL("CREATE INDEX mappings_game_idx ON mappings(package_name)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    fun list(game: String): List<TouchBinding> = readableDatabase.query("mappings",
        arrayOf("id", "package_name", "button", "x", "y", "mode"), "package_name=?", arrayOf(game),
        null, null, "id ASC").use { cursor ->
        buildList {
            while (cursor.moveToNext()) add(TouchBinding(cursor.getLong(0),cursor.getString(1),
                cursor.getString(2),cursor.getFloat(3),cursor.getFloat(4),cursor.getString(5)))
        }
    }
    fun add(game: String, button: String): Long = writableDatabase.insert("mappings", null,
        ContentValues().apply {
            put("package_name", game); put("button", button); put("x", .5f); put("y", .5f); put("mode", "tap")
        })
    fun move(id: Long, x: Float, y: Float) {
        writableDatabase.update("mappings", ContentValues().apply {
            put("x", x.coerceIn(0f, 1f)); put("y", y.coerceIn(0f, 1f))
        }, "id=?", arrayOf(id.toString()))
    }
    fun delete(id: Long) { writableDatabase.delete("mappings", "id=?", arrayOf(id.toString())) }
}

data class Calibration(val deadZone: Float = .12f, val invertY: Boolean = false)

class CalibrationStore(context: Context) {
    private val prefs = context.getSharedPreferences("padbridge-calibration", Context.MODE_PRIVATE)
    fun get(deviceDescriptor: String): Calibration = Calibration(
        prefs.getFloat("dz_$deviceDescriptor", .12f),
        prefs.getBoolean("invert_$deviceDescriptor", false)
    )
    fun save(deviceDescriptor: String, calibration: Calibration) {
        prefs.edit().putFloat("dz_$deviceDescriptor", calibration.deadZone)
            .putBoolean("invert_$deviceDescriptor", calibration.invertY).apply()
    }
}
