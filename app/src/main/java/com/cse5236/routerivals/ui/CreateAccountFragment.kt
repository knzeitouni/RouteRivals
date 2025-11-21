package com.cse5236.routerivals.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cse5236.routerivals.MainActivity
import com.cse5236.routerivals.R
import com.cse5236.routerivals.model.User
import com.cse5236.routerivals.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class CreateAccountFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userRepo: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        userRepo = UserRepository()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nameEditText = view.findViewById<EditText>(R.id.editTextName)
        val emailEditText = view.findViewById<EditText>(R.id.editTextEmail)
        val passwordEditText = view.findViewById<EditText>(R.id.editTextPassword)
        val createAccountButton = view.findViewById<Button>(R.id.buttonCreateAccount)
        val backToLoginButton = view.findViewById<Button>(R.id.buttonBackToLogin)

        createAccountButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        val uid = firebaseUser?.uid

                        if (uid == null) {
                            Toast.makeText(requireContext(), "Error creating user", Toast.LENGTH_SHORT).show()
                            return@addOnCompleteListener
                        }

                        val newUser = User(
                            id = uid,
                            name = name,
                            email = email,
                            friends = emptyList(),
                            scores = emptyMap()
                        )

                        viewLifecycleOwner.lifecycleScope.launch {
                            try {
                                userRepo.saveUser(newUser)

                                Toast.makeText(
                                    requireContext(),
                                    "Account created!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                val intent = Intent(requireContext(), MainActivity::class.java)
                                startActivity(intent)
                                requireActivity().finish()

                            } catch (e: Exception) {
                                Toast.makeText(
                                    requireContext(),
                                    "Failed to save user: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            task.exception?.localizedMessage ?: "Account creation failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        backToLoginButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}
