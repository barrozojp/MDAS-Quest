package com.codeofduty.mdas_rpg

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.codeofduty.mdas_rpg.databinding.ActivityUpdateNoteBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UpdateNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateNoteBinding
    private lateinit var currentUsername: String // To store the logged-in user's username
    private var note_id: String = "" // Firebase note_id is a String
    private lateinit var database: DatabaseReference // Reference to Firebase Realtime Database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get reference to Firebase Realtime Database
        database = FirebaseDatabase.getInstance().reference

        // Retrieve the username passed from NotepadFragment
        currentUsername = intent.getStringExtra("USERNAME") ?: ""
        note_id = intent.getStringExtra("note_id") ?: ""

        // Show a Toast to verify that values are correctly passed
        Toast.makeText(this, "Note ID: $note_id, Username: $currentUsername", Toast.LENGTH_SHORT).show()

        if (note_id.isEmpty()) {
            finish()
            return
        }

        // Retrieve the note from Firebase using note_id from the allnotes table
        getNoteFromFirebase(note_id)

        binding.updateSaveButton.setOnClickListener {
            val newTitle = binding.updateTitleEditText.text.toString()
            val newContent = binding.updateContentEditText.text.toString()

            // Create a new FirebaseNote with updated content
            val updatedNote = FirebaseNote(newContent, note_id, currentUsername, newTitle)

            // Update the note in the allnotes table in Firebase
            updateNoteInFirebase(updatedNote)

            // Show a Toast when saving changes
            Toast.makeText(this, "Changes Saved", Toast.LENGTH_SHORT).show()

            finish()
        }
    }

    // Function to retrieve the note from Firebase under the allnotes node
    private fun getNoteFromFirebase(noteId: String) {
        database.child("allnotes").child(noteId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val note = snapshot.getValue(FirebaseNote::class.java)
                if (note != null) {
                    // Set the retrieved note values to the UI
                    binding.updateTitleEditText.setText(note.title)
                    binding.updateContentEditText.setText(note.content)

                    // Set text color programmatically
                    binding.updateTitleEditText.setTextColor(resources.getColor(android.R.color.black))
                    binding.updateContentEditText.setTextColor(resources.getColor(android.R.color.black))
                }
            } else {
                Toast.makeText(this, "Note not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to retrieve note", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to update the note in Firebase under the allnotes node
    private fun updateNoteInFirebase(updatedNote: FirebaseNote) {
        // Use the existing note_id to reference the node in Firebase
        val noteRef = database.child("allnotes").child(updatedNote.note_id)

        // Update the note's data using the same note_id
        noteRef.setValue(updatedNote).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Note updated successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to update note", Toast.LENGTH_SHORT).show()
            }
        }
    }


}
