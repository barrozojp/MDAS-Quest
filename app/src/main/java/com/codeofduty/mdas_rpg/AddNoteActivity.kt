package com.codeofduty.mdas_rpg

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.codeofduty.mdas_rpg.databinding.ActivityAddNoteBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var db: DatabaseHelper
    private lateinit var firebaseDb: DatabaseReference
    private lateinit var currentUsername: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        firebaseDb = FirebaseDatabase.getInstance().getReference("allnotes")

        // Retrieve the username passed from NotepadFragment
        currentUsername = intent.getStringExtra("username") ?: ""

        binding.saveButton.setOnClickListener {
            val title = binding.titleEditText.text.toString().trim()
            val content = binding.contentEditText.text.toString().trim()

            if (title.isNotEmpty() && content.isNotEmpty()) {
                // Insert into SQLite and get the generated note_id
                val noteId: Long = db.insertNote(Note(0, title, content), currentUsername)

                // Save to Firebase
                val firebaseNoteId = firebaseDb.push().key // Generate unique Firebase ID
                if (firebaseNoteId != null) {
                    val noteData = hashMapOf(
                        "note_id" to noteId.toString(),  // Corrected SQLite ID
                        "title" to title,
                        "content" to content,
                        "note_user" to currentUsername
                    )
                    firebaseDb.child(firebaseNoteId).setValue(noteData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Note Saved", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to save to Firebase", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(this, "Title and Content cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
