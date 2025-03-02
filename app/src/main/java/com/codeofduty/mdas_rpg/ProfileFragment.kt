package com.codeofduty.mdas_rpg

import android.app.Dialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import android.widget.Toast
import com.google.firebase.database.*

class ProfileFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var usernameTextView: TextView
    private lateinit var loadingDialog: Dialog
    private lateinit var database: DatabaseReference  // Firebase reference for database

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize the TextView for displaying the username
        usernameTextView = view.findViewById(R.id.hi_userTV)

        // Set up loading dialog
        setupLoadingDialog()

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference

        sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", MODE_PRIVATE)

        // Retrieve and set the username from SharedPreferences
        val username = sharedPreferences.getString("username", "User")
        usernameTextView.text = "Hi $username!"

        // Set up the logout button click listener
        val logoutButton: TextView = view.findViewById(R.id.btn_logout)
        logoutButton.setOnClickListener {
            showLogoutDialog()
        }

        // Set up the Change Username button click listener
        val changeUsernameButton: AppCompatButton = view.findViewById(R.id.btn_changeUsername)
        changeUsernameButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ChangeUsernameFragment())
                .addToBackStack(null)
                .commit()
        }

        // Set up the Change Password button click listener
        val changePasswordButton: AppCompatButton = view.findViewById(R.id.btn_changePassword)
        changePasswordButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ChangePasswordFragment())
                .addToBackStack(null)
                .commit()
        }

        // Set up the Delete Account button click listener
        val deleteAccountButton: AppCompatButton = view.findViewById(R.id.btn_deleteAcc)
        deleteAccountButton.setOnClickListener {
            showDeleteAccountDialog()
        }

        return view
    }

    private fun showDeleteAccountDialog() {
        // Create a Dialog
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_account_deletion) // Use your deletion dialog layout
        dialog.setCancelable(true)

        // Initialize buttons
        val yesButton: TextView = dialog.findViewById(R.id.delete_yes)
        val noButton: TextView = dialog.findViewById(R.id.delete_no)

        // Yes button click listener
        yesButton.setOnClickListener {
            loadingDialog.show()

            // Delay to simulate account deletion process
            Handler(Looper.getMainLooper()).postDelayed({
                // Get the current username
                val currentUsername = sharedPreferences.getString("username", "") ?: ""

                // Delete the user account from the Firebase users table first
                deleteUserAccount(currentUsername) { userDeleted ->
                    // Then, delete the game records associated with the username
                    if (userDeleted) {
                        deleteGameUsername(currentUsername) { gameRecordsDeleted ->
                            // Finally, delete the notes associated with the username
                            if (gameRecordsDeleted) {
                                deleteNotesUsername(currentUsername) { notesDeleted ->
                                    if (notesDeleted) {
                                        // Clear SharedPreferences
                                        sharedPreferences.edit().clear().apply()

                                        // Dismiss loading dialog and deletion dialog
                                        dialog.dismiss()
                                        loadingDialog.dismiss()

                                        // Navigate back to LoginActivity if everything was deleted successfully
                                        val intent = Intent(requireActivity(), LoginRegister::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                    } else {
                                        Toast.makeText(requireContext(), "Failed to delete notes.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(requireContext(), "Failed to delete game records.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete user.", Toast.LENGTH_SHORT).show()
                    }
                }
            }, 2000) // 2-second delay for simulation
        }

        // No button click listener
        noButton.setOnClickListener {
            dialog.dismiss() // Just dismiss the dialog
        }

        // Show the dialog
        dialog.show()
    }

    private fun deleteUserAccount(username: String, callback: (Boolean) -> Unit) {
        val usersRef = database.child("users")

        // Query to find the user by their username
        usersRef.orderByChild("username").equalTo(username).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                for (userSnapshot in snapshot.children) {
                    // Delete the user record from the "users" table
                    userSnapshot.ref.removeValue().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            callback(true)  // Notify deletion success
                        } else {
                            callback(false)  // Notify deletion failure
                        }
                    }
                }
            } else {
                callback(false)  // User not found
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(requireContext(), "Failed to fetch user data: ${exception.message}", Toast.LENGTH_SHORT).show()
            callback(false)  // Notify failure
        }
    }

    private fun deleteGameUsername(username: String, callback: (Boolean) -> Unit) {
        val gameTableRef = database.child("game_table")

        // Query to find all games where game_username matches the username
        gameTableRef.orderByChild("game_username").equalTo(username).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (gameSnapshot in snapshot.children) {
                    // Delete the game record where the game_username matches the current username
                    gameSnapshot.ref.removeValue().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            callback(true)  // Notify deletion success
                        } else {
                            callback(false)  // Notify deletion failure
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to delete game records.", Toast.LENGTH_SHORT).show()
                callback(false)  // Notify failure
            }
        })
    }

    private fun deleteNotesUsername(username: String, callback: (Boolean) -> Unit) {
        val notesTableRef = database.child("allnotes")

        // Query to find all notes where note_user matches the username
        notesTableRef.orderByChild("note_user").equalTo(username).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (noteSnapshot in snapshot.children) {
                    // Delete the note record where the note_user matches the current username
                    noteSnapshot.ref.removeValue().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            callback(true)  // Notify deletion success
                        } else {
                            callback(false)  // Notify deletion failure
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to delete notes.", Toast.LENGTH_SHORT).show()
                callback(false)  // Notify failure
            }
        })
    }


    private fun showLogoutDialog() {
        // Create a Dialog
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_logout)
        dialog.setCancelable(true)

        // Initialize buttons
        val yesButton: TextView = dialog.findViewById(R.id.logout_yes)
        val noButton: TextView = dialog.findViewById(R.id.logout_no)

        // Yes button click listener
        yesButton.setOnClickListener {
            // Show loading dialog
            loadingDialog.show()

            // Delay to simulate logout process
            Handler(Looper.getMainLooper()).postDelayed({
                // Clear SharedPreferences
                sharedPreferences.edit().clear().apply()

                // Navigate back to the LoginActivity
                val intent = Intent(requireActivity(), LoginRegister::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

                // Dismiss logout and loading dialogs
                dialog.dismiss()
                loadingDialog.dismiss()

            }, 2000) // 2-second delay for simulation
        }

        // No button click listener
        noButton.setOnClickListener {
            dialog.dismiss() // Just dismiss the dialog
        }

        // Show the dialog
        dialog.show()
    }

    // Set up the loading dialog
    private fun setupLoadingDialog() {
        loadingDialog = Dialog(requireContext())
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_loading, null)
        loadingDialog.setContentView(dialogView)
        loadingDialog.setCancelable(false)
        loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}
