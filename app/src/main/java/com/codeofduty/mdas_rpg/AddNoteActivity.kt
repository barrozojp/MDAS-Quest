package com.codeofduty.mdas_rpg

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.codeofduty.mdas_rpg.databinding.ActivityAddNoteBinding

class AddNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var db: DatabaseHelper
    private lateinit var currentUsername: String // To store the logged-in user's username

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)

        // Retrieve the username passed from NotepadFragment
        currentUsername = intent.getStringExtra("username") ?: ""


        binding.saveButton.setOnClickListener {
            val title = binding.titleEditText.text.toString()
            val content = binding.contentEditText.text.toString()
            val note = Note(0, title, content) // Assuming Note has an appropriate constructor
            db.insertNote(note, currentUsername) // Pass the username to insertNote
            finish()
            Toast.makeText(this, "Note Saved", Toast.LENGTH_SHORT).show()
        }
    }

}
