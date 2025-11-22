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
import com.cse5236.routerivals.MainActivity
import com.cse5236.routerivals.R
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailEditText = view.findViewById<EditText>(R.id.editTextEmail)
        val passwordEditText = view.findViewById<EditText>(R.id.editTextPassword)
        val loginButton = view.findViewById<Button>(R.id.buttonLogin)
        val createAccountButton = view.findViewById<Button>(R.id.buttonGoToCreateAccount)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Go to main app
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            task.exception?.localizedMessage ?: "Login failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        createAccountButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.loginFragmentContainer, CreateAccountFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
