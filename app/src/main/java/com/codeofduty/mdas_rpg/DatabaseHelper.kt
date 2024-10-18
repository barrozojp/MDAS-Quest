package com.codeofduty.mdas_rpg

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "UserDatabase.db"
        const val DATABASE_VERSION = 3 // Incremented version
        const val TABLE_USERS = "Users"
        const val TABLE_GAME = "game_table"
        const val TABLE_NOTES = "allnotes" // New table for notes
        const val COLUMN_GAME_ID = "game_id"
        const val COLUMN_OPERATION_DIFFICULTY = "operation_difficulty"
        const val COLUMN_SCORE = "score"
        const val COLUMN_ID = "id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_GAME_USERNAME = "game_username"
        const val COLUMN_TITLE = "title" // New column for note title
        const val COLUMN_CONTENT = "content" // New column for note content
        const val COLUMN_NOTE_USER = "note_user" // New column for user reference in notes
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
        // Create Notes table with foreign key
        val createNotesTable = ("CREATE TABLE $TABLE_NOTES ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_TITLE TEXT,"
                + "$COLUMN_CONTENT TEXT,"
                + "$COLUMN_NOTE_USER TEXT," // New column for user reference
                + "FOREIGN KEY($COLUMN_NOTE_USER) REFERENCES $TABLE_USERS($COLUMN_USERNAME))") // Foreign key reference
        db.execSQL(createNotesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GAME")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTES")
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

    // Note-related methods
    fun insertNote(note: Note, username: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, note.title)
            put(COLUMN_CONTENT, note.content)
            put(COLUMN_NOTE_USER, username) // Store the username as a foreign key
        }
        db.insert(TABLE_NOTES, null, values)
        db.close()
    }

    fun getAllNotes(username: String): List<Note> {
        val noteList = mutableListOf<Note>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NOTES WHERE $COLUMN_NOTE_USER = ?"
        val cursor = db.rawQuery(query, arrayOf(username)) // Filter notes by username

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
            val content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))

            val note = Note(id, title, content)
            noteList.add(note)
        }
        cursor.close()
        db.close()
        return noteList
    }


    fun updateNote(note: Note, username: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, note.title)
            put(COLUMN_CONTENT, note.content)
            put(COLUMN_NOTE_USER, username) // Update username if needed
        }
        val whereClause = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(note.id.toString())
        db.update(TABLE_NOTES, values, whereClause, whereArgs)
        db.close()
    }

    fun getNoteByID(noteId: Int): Note {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NOTES WHERE $COLUMN_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(noteId.toString()))

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
            val content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))
            cursor.close()
            db.close()
            return Note(id, title, content)
        }
        cursor.close()
        db.close()
        throw Exception("Note not found")
    }

    fun deleteNote(noteId: Int) {
        val db = writableDatabase
        val whereClause = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(noteId.toString())
        db.delete(TABLE_NOTES, whereClause, whereArgs)
        db.close()
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


    fun getLeaderboard(): List<LeaderboardItem> {
        val leaderboard = mutableListOf<LeaderboardItem>()
        val db = this.readableDatabase

        // Select all records and join with the Users table to get the username
        val query = """
        SELECT $COLUMN_GAME_USERNAME, $COLUMN_OPERATION_DIFFICULTY, $COLUMN_SCORE 
        FROM $TABLE_GAME
        ORDER BY $COLUMN_SCORE DESC
        LIMIT 10
    """

        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val usernameIndex = cursor.getColumnIndex(COLUMN_GAME_USERNAME)
                val difficultyIndex = cursor.getColumnIndex(COLUMN_OPERATION_DIFFICULTY)
                val scoreIndex = cursor.getColumnIndex(COLUMN_SCORE)

                if (usernameIndex != -1 && difficultyIndex != -1 && scoreIndex != -1) {
                    val username = cursor.getString(usernameIndex)
                    val difficulty = cursor.getString(difficultyIndex)
                    val score = cursor.getInt(scoreIndex).toString()

                    // Add each user with their respective data to the leaderboard
                    leaderboard.add(LeaderboardItem(username, difficulty, score))
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        return leaderboard
    }

    fun updateUsername(oldUsername: String, newUsername: String): Boolean {
        val db = this.writableDatabase

        // Start a transaction to ensure the update happens atomically
        db.beginTransaction()
        return try {
            // Update the username in the Users table
            val userContentValues = ContentValues().apply {
                put(COLUMN_USERNAME, newUsername)
            }
            val userUpdateResult = db.update(
                TABLE_USERS,
                userContentValues,
                "$COLUMN_USERNAME = ?",
                arrayOf(oldUsername)
            )

            // Update the username in the game_table, if it exists
            val gameContentValues = ContentValues().apply {
                put(COLUMN_GAME_USERNAME, newUsername)
            }
            db.update(
                TABLE_GAME,
                gameContentValues,
                "$COLUMN_GAME_USERNAME = ?",
                arrayOf(oldUsername)
            )

            // If the user update is successful, commit the transaction
            if (userUpdateResult > 0) {
                db.setTransactionSuccessful()
                true // Username updated successfully
            } else {
                false // Username update failed
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false // An error occurred
        } finally {
            db.endTransaction()
            db.close() // Close the database
        }
    }

    fun updatePassword(username: String, newPassword: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_PASSWORD, newPassword)
        }
        val result = db.update(
            TABLE_USERS,
            contentValues,
            "$COLUMN_USERNAME = ?",
            arrayOf(username)
        )
        return result > 0
    }

    fun deleteUserAccount(username: String): Boolean {
        val db = this.writableDatabase

        // Start a transaction to ensure both deletions happen together
        db.beginTransaction()
        return try {
            // Delete game records that match the username
            db.delete(TABLE_GAME, "$COLUMN_GAME_USERNAME = ?", arrayOf(username))

            // Delete the user account
            val success = db.delete(TABLE_USERS, "$COLUMN_USERNAME = ?", arrayOf(username))

            // If user deletion was successful, commit the transaction
            if (success > 0) {
                db.setTransactionSuccessful()
                true // Account and related game records deleted successfully
            } else {
                false // Account deletion failed
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false // An error occurred
        } finally {
            db.endTransaction()
            db.close() // Close the database
        }
    }
    fun getAllUserData(): List<Pair<String, String>> {
        val userList = mutableListOf<Pair<String, String>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_USERNAME, $COLUMN_PASSWORD FROM $TABLE_USERS", null)

        while (cursor.moveToNext()) {
            val username = cursor.getString(0)
            val password = cursor.getString(1)
            userList.add(Pair(username, password))
        }
        cursor.close()
        db.close()
        return userList
    }

    fun getAllUserScores(): List<Triple<String, String, Int>> {
        val scoreList = mutableListOf<Triple<String, String, Int>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_GAME_USERNAME, $COLUMN_OPERATION_DIFFICULTY, $COLUMN_SCORE FROM $TABLE_GAME", null)

        while (cursor.moveToNext()) {
            val username = cursor.getString(0)
            val operationDifficulty = cursor.getString(1)
            val score = cursor.getInt(2)
            scoreList.add(Triple(username, operationDifficulty, score))
        }
        cursor.close()
        db.close()
        return scoreList
    }

    fun getAllUserNotes(): List<Note> {
        val noteList = mutableListOf<Note>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_NOTE_USER, $COLUMN_TITLE, $COLUMN_CONTENT FROM $TABLE_NOTES", null)

        while (cursor.moveToNext()) {
            val username = cursor.getString(0)
            val title = cursor.getString(1)
            val content = cursor.getString(2)
            noteList.add(Note(-1, title, content)) // Using -1 for ID as it's not needed for export
        }
        cursor.close()
        db.close()
        return noteList
    }




}
