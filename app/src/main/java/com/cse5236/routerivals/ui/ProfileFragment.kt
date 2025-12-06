package com.cse5236.routerivals.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cse5236.routerivals.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private val TAG = "ProfileFragment"

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var textUserName: TextView
    private lateinit var textUserEmail: TextView
    private lateinit var buttonLogout: MaterialButton
    private lateinit var buttonDeleteProfile: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the pretty layout
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views (IDs must match fragment_profile.xml)
        textUserName = view.findViewById(R.id.textUserName)
        textUserEmail = view.findViewById(R.id.textUserEmail)
        buttonLogout = view.findViewById(R.id.buttonLogout)
        buttonDeleteProfile = view.findViewById(R.id.buttonDeleteProfile)

        setupButtons()
        loadUserInfo()

        return view
    }

    private fun setupButtons() {
        // Log out
        buttonLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()

            // Go back to LoginActivity and clear back stack
            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }

        // Delete profile (Firebase Auth user only)
        buttonDeleteProfile.setOnClickListener {
            val user = auth.currentUser
            if (user == null) {
                Toast.makeText(requireContext(), "No user is signed in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            user.delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Profile deleted", Toast.LENGTH_SHORT).show()

                        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                    } else {
                        Log.e(TAG, "Failed to delete user", task.exception)
                        Toast.makeText(
                            requireContext(),
                            "Failed to delete profile. Try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun loadUserInfo() {
        val user: FirebaseUser? = auth.currentUser

        if (user == null) {
            Log.w(TAG, "No logged-in user found")
            textUserName.text = "Unknown"
            textUserEmail.text = "Not signed in"
            return
        }

        val uid = user.uid
        val authDisplayName = user.displayName
        val authEmail = user.email

        // Show auth values immediately as a fallback
        textUserName.text = authDisplayName ?: "Unnamed User"
        textUserEmail.text = authEmail ?: "No email"

        // Then try to override with Firestore data from users/{uid}
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (!isAdded) return@addOnSuccessListener

                if (doc.exists()) {
                    val nameFromDb = doc.getString("name")
                    val emailFromDb = doc.getString("email")

                    val finalName = nameFromDb ?: authDisplayName ?: "Unnamed User"
                    val finalEmail = emailFromDb ?: authEmail ?: "No email"

                    textUserName.text = finalName
                    textUserEmail.text = finalEmail
                } else {
                    Log.w(TAG, "User document $uid does not exist in Firestore")
                }
            }
            .addOnFailureListener { e ->
                if (!isAdded) return@addOnFailureListener
                Log.e(TAG, "Failed to load user profile from Firestore", e)
                // We already showed auth info above, so just keep it
            }
    }
}
