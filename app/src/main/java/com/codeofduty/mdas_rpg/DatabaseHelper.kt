package com.codeofduty.mdas_rpg

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "UserDatabase.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_USERS = "Users"
        private const val TABLE_GAME = "game_table"
        private const val COLUMN_GAME_ID = "game_id"
        private const val COLUMN_OPERATION_DIFFICULTY = "operation_difficulty"
        private const val COLUMN_SCORE = "score"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_GAME_USERNAME = "game_username"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE $TABLE_USERS ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_USERNAME TEXT,"
                + "$COLUMN_PASSWORD TEXT)")
        db.execSQL(createTable)

        val createGameTable = ("CREATE TABLE $TABLE_GAME ("
                + "$COLUMN_GAME_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_GAME_USERNAME TEXT," // Foreign key for user
                + "$COLUMN_OPERATION_DIFFICULTY TEXT,"
                + "$COLUMN_SCORE INTEGER,"
                + "FOREIGN KEY($COLUMN_GAME_USERNAME) REFERENCES $TABLE_USERS($COLUMN_USERNAME))")
        db.execSQL(createGameTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GAME")
        onCreate(db)
    }

    fun insertUser(username: String, password: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password)
        }
        val result = db.insert(TABLE_USERS, null, contentValues)
        return result != -1L
    }

    fun checkUserExists(username: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ?"
        val cursor = db.rawQuery(query, arrayOf(username))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun checkUserCredentials(username: String, password: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(username, password))
        val credentialsMatch = cursor.count > 0
        cursor.close()
        return credentialsMatch
    }
    fun insertGameRecord(username: String, operationDifficulty: String, score: Int): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_GAME_USERNAME, username) // Add the user ID
            put(COLUMN_OPERATION_DIFFICULTY, operationDifficulty)
            put(COLUMN_SCORE, score)
        }
        val result = db.insert(TABLE_GAME, null, contentValues)
        return result != -1L
    }

    fun getUserId(username: String): Int {
        val db = this.readableDatabase
        val query = "SELECT $COLUMN_ID FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ?"
        val cursor = db.rawQuery(query, arrayOf(username))
        var userId = -1 // Default to -1 if not found

        if (cursor.moveToFirst()) {
            val userIdIndex = cursor.getColumnIndex(COLUMN_ID)
            if (userIdIndex != -1) { // Ensure the index is valid
                userId = cursor.getInt(userIdIndex)
            }
        }
        cursor.close()
        return userId
    }


    fun getLeaderboard(username: String): List<LeaderboardItem> {
        val leaderboard = mutableListOf<LeaderboardItem>()
        val db = this.readableDatabase

        // Select records for the specific user by username
        val query = "SELECT $COLUMN_OPERATION_DIFFICULTY, $COLUMN_SCORE " +
                "FROM $TABLE_GAME WHERE $COLUMN_GAME_USERNAME = ? " + // Filter by username
                "ORDER BY $COLUMN_SCORE DESC " + // Order by score descending
                "LIMIT 10" // Limit the results to 10

        val cursor = db.rawQuery(query, arrayOf(username))
        if (cursor.moveToFirst()) {
            var rank = 1
            do {
                val difficultyIndex = cursor.getColumnIndex(COLUMN_OPERATION_DIFFICULTY)
                val scoreIndex = cursor.getColumnIndex(COLUMN_SCORE)

                // Check if column indexes are valid
                if (difficultyIndex != -1 && scoreIndex != -1) {
                    val difficulty = cursor.getString(difficultyIndex)
                    val score = cursor.getInt(scoreIndex).toString()
                    leaderboard.add(LeaderboardItem(rank.toString(), difficulty, score))
                    rank++
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        return leaderboard
    }


}
