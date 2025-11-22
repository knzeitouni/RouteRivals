package com.cse5236.routerivals.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cse5236.routerivals.R
import com.cse5236.routerivals.adapters.FriendRequestsAdapter
import com.cse5236.routerivals.adapters.FriendsAdapter
import com.cse5236.routerivals.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import androidx.appcompat.app.AlertDialog
import kotlin.getValue

class ProfileFragment : Fragment() {
    private val TAG = "ProfileFragmentLifecycle"
    private val userViewModel: UserViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val deleteButton = view.findViewById<Button>(R.id.buttonDeleteProfile)

        deleteButton.setOnClickListener {
            // Show confirmation dialog
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete your profile? This action cannot be undone.")
                .setPositiveButton("Delete") { dialog, _ ->
                    // Call the ViewModel to delete user
                    userViewModel.removeUser(userViewModel.getCurrentUserId())
                    Toast.makeText(requireContext(), "Profile deleted", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()

                    // Optional: navigate back or to login screen
                    requireActivity().finish() // or use NavController to navigate
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
        return view
    }
}