package com.cse5236.routerivals.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.cse5236.routerivals.R
import com.cse5236.routerivals.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {
    private val TAG = "ProfileFragment"
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var auth: FirebaseAuth

    private lateinit var textViewUsername: TextView
    private lateinit var textViewEmail: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()

        // Initialize views
        textViewUsername = view.findViewById(R.id.textViewUsername)
        textViewEmail = view.findViewById(R.id.textViewEmail)

        val buttonLogout = view.findViewById<Button>(R.id.buttonLogout)
        val buttonDeleteProfile = view.findViewById<Button>(R.id.buttonDeleteProfile)

        // Load user data
        loadUserProfile()

        // Logout button
        buttonLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out") { dialog, _ ->
                    auth.signOut()
                    Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
                    // Navigate back to login
                    requireActivity().finish()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        // Delete Profile button
        buttonDeleteProfile.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete your profile? This action cannot be undone.")
                .setPositiveButton("Delete") { dialog, _ ->
                    userViewModel.removeUser(userViewModel.getCurrentUserId())
                    Toast.makeText(requireContext(), "Profile deleted", Toast.LENGTH_SHORT).show()
                    auth.signOut()
                    requireActivity().finish()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        return view
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Get display name or email username
            val displayName = currentUser.displayName
            val email = currentUser.email ?: ""

            // If no display name, use the part before @ in email
            val username = if (!displayName.isNullOrEmpty()) {
                displayName
            } else {
                email.substringBefore("@")
            }

            textViewUsername.text = username
            textViewEmail.text = email

            Log.d(TAG, "Loaded profile - Username: $username, Email: $email")
        } else {
            Log.w(TAG, "No user logged in")
            textViewUsername.text = "Not logged in"
            textViewEmail.text = "Not available"
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }
}