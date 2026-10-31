package com.arfstudio.padbridge

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class SavedGame(val packageName: String, val label: String, val profileName: String)

/** Offline storage; future migrations must preserve user data. */
class GameStore(context: Context) : SQLiteOpenHelper(context, "padbridge.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE games (package_name TEXT PRIMARY KEY NOT NULL, label TEXT NOT NULL, profile_name TEXT NOT NULL DEFAULT 'Default')")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Use additive migrations when the schema changes.
    }
    fun all(): List<SavedGame> = readableDatabase.query(
        "games", arrayOf("package_name", "label", "profile_name"), null, null, null, null, "label COLLATE NOCASE"
    ).use { cursor ->
        buildList {
            while (cursor.moveToNext()) add(SavedGame(cursor.getString(0), cursor.getString(1), cursor.getString(2)))
        }
    }
    fun add(packageName: String, label: String) {
        writableDatabase.insertWithOnConflict("games", null, ContentValues().apply {
            put("package_name", packageName)
            put("label", label)
            put("profile_name", "Default")
        }, SQLiteDatabase.CONFLICT_IGNORE)
    }
    fun renameProfile(packageName: String, name: String) {
        writableDatabase.update("games", ContentValues().apply { put("profile_name", name) },
            "package_name = ?", arrayOf(packageName))
    }
    fun remove(packageName: String) {
        writableDatabase.delete("games", "package_name = ?", arrayOf(packageName))
    }
}
