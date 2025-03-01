package com.codeofduty.mdas_rpg

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.codeofduty.mdas_rpg.databinding.FragmentNotepadBinding
import com.google.firebase.database.*

class NotepadFragment : Fragment() {

    private var _binding: FragmentNotepadBinding? = null
    private val binding get() = _binding!!
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var firebaseDb: DatabaseReference
    private lateinit var currentUsername: String
    private val notesList = mutableListOf<Note>() // List only for Firebase notes

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotepadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseDb = FirebaseDatabase.getInstance().getReference("allnotes")

        val sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", AppCompatActivity.MODE_PRIVATE)
        currentUsername = sharedPreferences.getString("username", "") ?: ""

        notesAdapter = NotesAdapter(notesList, requireContext(), currentUsername)
        binding.notesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.notesRecyclerView.adapter = notesAdapter

        binding.addButton.setOnClickListener {
            val intent = Intent(requireContext(), AddNoteActivity::class.java).apply {
                putExtra("username", currentUsername)
            }
            startActivity(intent)
        }

        fetchFirebaseNotes() // Fetch notes only from Firebase
    }

    private fun fetchFirebaseNotes() {
        notesList.clear()

        firebaseDb.orderByChild("note_user").equalTo(currentUsername).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (noteSnapshot in snapshot.children) {
                    val note_id = noteSnapshot.child("note_id").getValue(String::class.java) ?: ""
                    val title = noteSnapshot.child("title").getValue(String::class.java) ?: ""
                    val content = noteSnapshot.child("content").getValue(String::class.java) ?: ""

                    val note = Note(note_id.hashCode(), title, content) // Convert Firebase ID to Int hash
                    if (!notesList.any { it.title == note.title && it.content == note.content }) {
                        notesList.add(note) // Avoid duplicate notes
                    }
                }
                notesAdapter.refreshData(notesList)
                toggleEmptyState()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to fetch notes from Firebase", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun toggleEmptyState() {
        if (notesAdapter.itemCount == 0) {
            binding.animEmpty.visibility = View.VISIBLE
            binding.tvEmpty.visibility = View.VISIBLE
            binding.notesRecyclerView.visibility = View.GONE
        } else {
            binding.animEmpty.visibility = View.GONE
            binding.tvEmpty.visibility = View.GONE
            binding.notesRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        fetchFirebaseNotes() // Refresh notes only from Firebase
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
