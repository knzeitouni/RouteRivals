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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cse5236.routerivals.R
import com.cse5236.routerivals.adapters.FriendRequestsAdapter
import com.cse5236.routerivals.adapters.FriendsAdapter
import com.cse5236.routerivals.viewmodel.UserViewModel

class FriendsFragment : Fragment() {

    private val TAG = "FriendsFragmentLifecycle"
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var friendRequestsAdapter: FriendRequestsAdapter
    private lateinit var friendsAdapter: FriendsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_friends, container, false)
        setupView(view)
        return view
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

    private fun setupView(view: View) {
        setupAdapters()
        setupRecyclerViews(view)
        setupAddFriendButton(view)
        setupObservers()
        loadData()
    }

    private fun setupAdapters() {
        // Friend Requests Adapter
        friendRequestsAdapter = FriendRequestsAdapter(
            onAccept = { user ->
                userViewModel.acceptFriendRequest(user.id)
                Toast.makeText(requireContext(), "Accepted: ${user.name}", Toast.LENGTH_SHORT).show()
            },
            onDecline = { user ->
                userViewModel.declineFriendRequest(user.id)
                Toast.makeText(requireContext(), "Declined: ${user.name}", Toast.LENGTH_SHORT).show()
            }
        )

        // Friends List Adapter
        friendsAdapter = FriendsAdapter(
            onRemoveFriend = { user ->
                userViewModel.removeFriend(user.id)
                Toast.makeText(requireContext(), "Removed: ${user.name}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun setupRecyclerViews(view: View) {
        val recyclerViewFriendRequests = view.findViewById<RecyclerView>(R.id.recyclerViewFriendRequests)
        val recyclerViewFriends = view.findViewById<RecyclerView>(R.id.recyclerViewFriends)

        recyclerViewFriendRequests.apply {
            adapter = friendRequestsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        recyclerViewFriends.apply {
            adapter = friendsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupAddFriendButton(view: View) {
        val editTextFriendId = view.findViewById<EditText>(R.id.editTextFriendId)
        val buttonAddFriend = view.findViewById<Button>(R.id.buttonAddFriend)

        buttonAddFriend.setOnClickListener {
            val friendId = editTextFriendId.text.toString().trim()

            if (friendId.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a user ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (friendId == userViewModel.getCurrentUserId()) {
                Toast.makeText(requireContext(), "You cannot add yourself as a friend", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userViewModel.sendFriendRequest(friendId)
            editTextFriendId.text.clear()
        }
    }

    private fun setupObservers() {
        userViewModel.friendRequests.observe(viewLifecycleOwner) { requests ->
            friendRequestsAdapter.updateFriendRequests(requests)
        }

        userViewModel.friends.observe(viewLifecycleOwner) { friends ->
            friendsAdapter.updateFriends(friends)
        }
    }

    private fun loadData() {
        userViewModel.loadCurrentUser()
        userViewModel.loadUsers()

        // For testing purposes
        //userViewModel.createTestData()
    }
}
