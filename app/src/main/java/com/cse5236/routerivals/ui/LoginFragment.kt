package com.cse5236.routerivals.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cse5236.routerivals.MainActivity      // 👈 IMPORTANT import
import com.cse5236.routerivals.R
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private val tagLog = "LoginFragment"

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = FirebaseAuth.getInstance()
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailEditText: EditText = view.findViewById(R.id.editTextEmail)
        val passwordEditText: EditText = view.findViewById(R.id.editTextPassword)
        val loginButton: Button = view.findViewById(R.id.buttonLogin)
        val createAccountButton: Button = view.findViewById(R.id.buttonGoToCreateAccount)

        // -------- LOGIN --------
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please enter email and password",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "Login successful",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
                .addOnFailureListener { e ->
                    Log.e(tagLog, "Login failed", e)
                    Toast.makeText(
                        requireContext(),
                        "Login failed: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        // -------- GO TO CREATE ACCOUNT --------
        createAccountButton.setOnClickListener {
            Toast.makeText(requireContext(), "Create account clicked", Toast.LENGTH_SHORT).show()
            Log.d(tagLog, "Create account clicked")

            val containerId = (view.parent as? ViewGroup)?.id ?: View.NO_ID
            if (containerId == View.NO_ID) {
                Log.e(tagLog, "No container id found for LoginFragment")
                return@setOnClickListener
            }

            parentFragmentManager.beginTransaction()
                .replace(containerId, CreateAccountFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
