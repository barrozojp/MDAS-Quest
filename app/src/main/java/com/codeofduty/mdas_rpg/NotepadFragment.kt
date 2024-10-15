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

class NotepadFragment : Fragment() {

    private var _binding: FragmentNotepadBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: DatabaseHelper // Updated to use DatabaseHelper
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var currentUsername: String // To store the logged-in user's username

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotepadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the DatabaseHelper
        db = DatabaseHelper(requireContext())

        // Get the logged-in user's username from SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", AppCompatActivity.MODE_PRIVATE)
        currentUsername = sharedPreferences.getString("username", "") ?: ""


        // Initialize the notes adapter, passing the currentUsername
        notesAdapter = NotesAdapter(db.getAllNotes(currentUsername), requireContext(), currentUsername) // Pass currentUsername here

        binding.notesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.notesRecyclerView.adapter = notesAdapter

        binding.addButton.setOnClickListener {
            val intent = Intent(requireContext(), AddNoteActivity::class.java).apply {
                putExtra("username", currentUsername) // Pass the username to AddNoteActivity
            }
            startActivity(intent)
        }
    }


    override fun onResume() {
        super.onResume()
        notesAdapter.refreshData(db.getAllNotes(currentUsername)) // Pass currentUsername here to refresh notes
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}