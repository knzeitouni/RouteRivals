package com.cse5236.routerivals.ui

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
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateAccountFragment : Fragment() {

    private val TAG = "CreateAccountFragment"

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // uses your fragment_create_account.xml
        return inflater.inflate(R.layout.fragment_create_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // This should ALWAYS show when you land on the screen
        Toast.makeText(requireContext(), "CreateAccountFragment loaded", Toast.LENGTH_SHORT).show()

        val nameEditText =
            view.findViewById<TextInputEditText>(R.id.editTextName)
        val emailEditText =
            view.findViewById<TextInputEditText>(R.id.editTextEmail)
        val passwordEditText =
            view.findViewById<TextInputEditText>(R.id.editTextPassword)
        val confirmPasswordEditText =
            view.findViewById<TextInputEditText>(R.id.editTextConfirmPassword)

        val createAccountButton =
            view.findViewById<MaterialButton>(R.id.buttonCreateAccount)
        val loginLinkText =
            view.findViewById<TextView>(R.id.textLoginLink)

        Log.d(TAG, "Found views: name=$nameEditText, email=$emailEditText, " +
                "pwd=$passwordEditText, confirm=$confirmPasswordEditText, " +
                "button=$createAccountButton, loginLink=$loginLinkText")

        if (createAccountButton == null) {
            Toast.makeText(requireContext(), "buttonCreateAccount not found in layout", Toast.LENGTH_LONG).show()
            return
        }

        // Clicking the purple "Create Account" button
        createAccountButton.setOnClickListener {
            Log.d(TAG, "Create Account button CLICKED")
            Toast.makeText(requireContext(), "Create clicked", Toast.LENGTH_SHORT).show()

            val name = nameEditText?.text?.toString()?.trim().orEmpty()
            val email = emailEditText?.text?.toString()?.trim().orEmpty()
            val password = passwordEditText?.text?.toString().orEmpty()
            val confirmPassword = confirmPasswordEditText?.text?.toString().orEmpty()

            // Basic validation
            if (name.isEmpty() || email.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty()
            ) {
                Toast.makeText(
                    requireContext(),
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(
                    requireContext(),
                    "Passwords do not match",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(
                    requireContext(),
                    "Password must be at least 6 characters",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Create user with Firebase Auth
            Log.d(TAG, "Creating Firebase user for email=$email")
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val user = result.user
                    Log.d(TAG, "Auth user created: uid=${user?.uid}")

                    if (user != null) {
                        // Save user info in Firestore "users" collection
                        val userData = hashMapOf(
                            "name" to name,
                            "email" to email
                        )

                        db.collection("users")
                            .document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Log.d(TAG, "User document written for uid=${user.uid}")
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Failed to write user document", e)
                            }
                    }

                    Toast.makeText(
                        requireContext(),
                        "Account created!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Go back to Login screen
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to create user", e)
                    Toast.makeText(
                        requireContext(),
                        "Error: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        // The "Already have an account? Log in" text
        loginLinkText.setOnClickListener {
            Log.d(TAG, "Login link clicked from CreateAccountFragment")
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }
}
