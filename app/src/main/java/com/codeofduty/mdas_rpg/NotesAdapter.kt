package com.codeofduty.mdas_rpg

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotesAdapter(private var notes: List<Note>, private val context: Context, private val username: String) :
    RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    private val db: DatabaseHelper = DatabaseHelper(context)
    private lateinit var loadingDialog: Dialog

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val contentTextView: TextView = itemView.findViewById(R.id.contentTextView)
        val updateButton: ImageView = itemView.findViewById(R.id.updateButton)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
        return NoteViewHolder(view)
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.titleTextView.text = note.title
        holder.contentTextView.text = note.content

        setupLoadingDialog(holder.itemView.context) // Initialize Loading Dialog

        // Start UpdateNoteActivity with note ID and username
        holder.updateButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, UpdateNoteActivity::class.java).apply {
                putExtra("note_id", note.note_id)
                putExtra("USERNAME", username)
            }
            holder.itemView.context.startActivity(intent)
        }

        holder.deleteButton.setOnClickListener {
            loadingDialog.show() // Show loading before deletion

            // Delete from SQLite
            db.deleteNote(note.note_id)

            // Delete from Firebase
            val firebaseDb = FirebaseDatabase.getInstance().getReference("allnotes")
            firebaseDb.orderByChild("note_id").equalTo(note.note_id.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (noteSnapshot in snapshot.children) {
                            noteSnapshot.ref.removeValue()
                        }
                        // Refresh the RecyclerView list after deletion
                        refreshData(db.getAllNotes(username))
                        loadingDialog.dismiss() // Dismiss loading
                        Toast.makeText(holder.itemView.context, "Note Deleted", Toast.LENGTH_SHORT).show()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        loadingDialog.dismiss() // Dismiss if error occurs
                        Toast.makeText(holder.itemView.context, "Failed to delete from Firebase", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun setupLoadingDialog(context: Context) {
        loadingDialog = Dialog(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null)
        loadingDialog.setContentView(dialogView)
        loadingDialog.setCancelable(false)
        loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    fun refreshData(newNotes: List<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }
}
