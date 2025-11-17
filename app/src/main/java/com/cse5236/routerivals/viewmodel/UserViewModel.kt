package com.cse5236.routerivals.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cse5236.routerivals.model.User
import com.cse5236.routerivals.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val repository = UserRepository()

    // LiveData for all users
    val users: LiveData<List<User>> = repository.usersLiveData

    // LiveData for current user
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    // Current user ID (hardcoded for testing)
    private var currentUserId: String = repository.getCurrentUser()?.uid ?: "test_current_user_123"

    // LiveData for current user's friends (real data)
    private val _friends = MutableLiveData<List<User>>()
    val friends: LiveData<List<User>> = _friends

    // LiveData for current user's friend requests (real data)
    private val _friendRequests = MutableLiveData<List<User>>()
    val friendRequests: LiveData<List<User>> = _friendRequests

    // Basic user operations
    fun addOrUpdateUser(user: User) {
        viewModelScope.launch {
            repository.saveUser(user)
        }
    }

    fun removeUser(userId: String) {
        viewModelScope.launch {
            repository.deleteUser(userId)
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            repository.fetchUsers()
        }
    }

    // Friend request operations
    suspend fun sendFriendRequest(toUid: String): User? {
        val targetUser = repository.getUser(toUid)
        if (targetUser != null) {
            repository.sendFriendRequest(currentUserId, toUid)
            loadCurrentUser()
            return targetUser  // Can return value
        }
        return null
    }

    fun acceptFriendRequest(requestingUid: String) {
        viewModelScope.launch {
            try {
                repository.acceptFriendRequest(currentUserId, requestingUid)
                Log.d("UserViewModel", "Friend request accepted from $requestingUid")
                loadCurrentUser() // Refresh current user data
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error accepting friend request", e)
            }
        }
    }

    fun declineFriendRequest(requestingUid: String) {
        viewModelScope.launch {
            try {
                repository.declineFriendRequest(currentUserId, requestingUid)
                Log.d("UserViewModel", "Friend request declined from $requestingUid")
                loadCurrentUser() // Refresh current user data
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error declining friend request", e)
            }
        }
    }

    // Friend management
    fun removeFriend(friendUid: String) {
        viewModelScope.launch {
            try {
                repository.removeFriend(currentUserId, friendUid)
                Log.d("UserViewModel", "Friend removed: $friendUid")
                loadCurrentUser() // Refresh current user data
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error removing friend", e)
            }
        }
    }

    // Load current user data
    fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val user = repository.getUser(currentUserId)
                _currentUser.value = user

                // Load friends and requests as User objects
                loadFriends()
                loadFriendRequests()
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error loading current user", e)
            }
        }
    }

    // Load friends as User objects from Firestore
    private fun loadFriends() {
        viewModelScope.launch {
            try {
                val currentUser = repository.getUser(currentUserId)
                val friendUsers = currentUser?.friends?.mapNotNull { friendId ->
                    repository.getUser(friendId)
                } ?: emptyList()
                _friends.value = friendUsers
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error loading friends", e)
            }
        }
    }

    // Load friend requests as User objects from Firestore
    private fun loadFriendRequests() {
        viewModelScope.launch {
            try {
                val currentUser = repository.getUser(currentUserId)
                val requestUsers = currentUser?.incomingRequests?.mapNotNull { requestId ->
                    repository.getUser(requestId)
                } ?: emptyList()
                _friendRequests.value = requestUsers
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error loading friend requests", e)
            }
        }
    }

    // Method to update current user when Firebase Auth is ready
    fun setCurrentUser(uid: String) {
        currentUserId = uid
        loadCurrentUser()
    }

    fun getCurrentUserId(): String {
        return currentUserId
    }

    fun createTestData() {
        viewModelScope.launch {
            try {
                // Create test users
                val testUsers = listOf(
                    User(id = "user1", name = "Alice", email = "alice@test.com", friends = listOf("test_current_user_123"), incomingRequests = emptyList(), outgoingRequests = emptyList(), scores = emptyMap(), completedRoutes = emptyList()),
                    User(id = "user2", name = "Bob", email = "bob@test.com", friends = emptyList(), incomingRequests = emptyList(), outgoingRequests = listOf("test_current_user_123"), scores = emptyMap(), completedRoutes = emptyList()),
                    User(id = "user3", name = "Charlie", email = "charlie@test.com", friends = emptyList(), incomingRequests = emptyList(), outgoingRequests = listOf("test_current_user_123"), scores = emptyMap(), completedRoutes = emptyList()),
                    User(id = "test_current_user_123", name = "You", email = "you@test.com", friends = listOf("user1"), incomingRequests = listOf("user2", "user3"), outgoingRequests = emptyList(), scores = emptyMap(), completedRoutes = emptyList())
                )

                // Save all test users to Firestore
                testUsers.forEach { user ->
                    repository.saveUser(user)
                }

                Log.d("UserViewModel", "Test data created successfully")
                loadCurrentUser() // Refresh to show the new data

            } catch (e: Exception) {
                Log.e("UserViewModel", "Error creating test data", e)
            }
        }
    }
}