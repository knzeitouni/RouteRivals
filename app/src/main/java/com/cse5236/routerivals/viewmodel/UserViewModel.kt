package com.cse5236.routerivals.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cse5236.routerivals.model.User
import com.cse5236.routerivals.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val repository = UserRepository()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // LiveData for all users
    val users: LiveData<List<User>> = repository.usersLiveData

    // LiveData for current user
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    // Use actual Firebase Auth user ID
    private var currentUserId: String = auth.currentUser?.uid ?: "test_current_user_123"

    // LiveData for current user's friends (real data)
    private val _friends = MutableLiveData<List<User>>()
    val friends: LiveData<List<User>> = _friends

    // LiveData for current user's friend requests (real data)
    private val _friendRequests = MutableLiveData<List<User>>()
    val friendRequests: LiveData<List<User>> = _friendRequests

    // LiveData for leaderboard
    private val _leaderboard = MutableLiveData<List<User>>()
    val leaderboard: LiveData<List<User>> = _leaderboard

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

    // ADD POINTS TO CURRENT USER
    fun addPointsToCurrentUser(points: Int) {
        val userId = getCurrentUserId()
        val userRef = db.collection("users").document(userId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)

            // Get current scores map or create empty one
            @Suppress("UNCHECKED_CAST")
            val currentScores = snapshot.get("scores") as? Map<String, Long> ?: mapOf(
                "allTime" to 0L,
                "monthly" to 0L,
                "weekly" to 0L,
                "daily" to 0L
            )

            // Add points to all time periods
            val updatedScores = mapOf(
                "allTime" to (currentScores["allTime"] ?: 0L) + points,
                "monthly" to (currentScores["monthly"] ?: 0L) + points,
                "weekly" to (currentScores["weekly"] ?: 0L) + points,
                "daily" to (currentScores["daily"] ?: 0L) + points
            )

            transaction.update(userRef, "scores", updatedScores)
        }.addOnSuccessListener {
            Log.d("UserViewModel", "Successfully added $points points to user $userId")
            loadCurrentUser() // Refresh user data
            loadLeaderboard() // Refresh leaderboard
        }.addOnFailureListener { e ->
            Log.e("UserViewModel", "Error adding points: ${e.message}", e)
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

    fun createLeaderboardTestData() {
        viewModelScope.launch {
            try {
                // Get current user ID
                val actualUserId = auth.currentUser?.uid

                val testUsers = listOf(
                    User(
                        id = "user1",
                        name = "Alice",
                        email = "alice@test.com",
                        friends = if (actualUserId != null) listOf(actualUserId) else emptyList(),
                        incomingRequests = emptyList(),
                        outgoingRequests = emptyList(),
                        scores = mapOf(
                            "allTime" to 1200,
                            "monthly" to 300,
                            "weekly" to 80,
                            "daily" to 20
                        )
                    ),
                    User(
                        id = "user2",
                        name = "Bob",
                        email = "bob@test.com",
                        friends = emptyList(),
                        incomingRequests = emptyList(),
                        outgoingRequests = if (actualUserId != null) listOf(actualUserId) else emptyList(),
                        scores = mapOf(
                            "allTime" to 1500,
                            "monthly" to 500,
                            "weekly" to 100,
                            "daily" to 25
                        )
                    ),
                    User(
                        id = "user3",
                        name = "Charlie",
                        email = "charlie@test.com",
                        friends = emptyList(),
                        incomingRequests = emptyList(),
                        outgoingRequests = if (actualUserId != null) listOf(actualUserId) else emptyList(),
                        scores = mapOf(
                            "allTime" to 800,
                            "monthly" to 200,
                            "weekly" to 50,
                            "daily" to 10
                        )
                    )
                )

                // Save only test users (NOT the current user)
                testUsers.forEach { user ->
                    repository.saveUser(user)
                }

                // If current user doesn't exist in Firestore, create them with 0 points
                if (actualUserId != null) {
                    val existingUser = repository.getUser(actualUserId)
                    if (existingUser == null) {
                        val newUser = User(
                            id = actualUserId,
                            name = auth.currentUser?.displayName ?: auth.currentUser?.email?.substringBefore("@") ?: "You",
                            email = auth.currentUser?.email ?: "",
                            friends = emptyList(),
                            incomingRequests = listOf("user2", "user3"),
                            outgoingRequests = emptyList(),
                            scores = mapOf(
                                "allTime" to 0,
                                "monthly" to 0,
                                "weekly" to 0,
                                "daily" to 0
                            )
                        )
                        repository.saveUser(newUser)
                    }
                    _currentUser.value = repository.getUser(actualUserId)
                }

                Log.d("UserViewModel", "Leaderboard test data created")
                loadLeaderboard() // Refresh LiveData so the leaderboard updates

            } catch (e: Exception) {
                Log.e("UserViewModel", "Error creating test data", e)
            }
        }
    }


    fun loadLeaderboard(timePeriod: String = "allTime", scope: String = "all") {
        viewModelScope.launch {
            try {
                val allUsers = repository.fetchUsers()  // fetch all users from Firestore

                // Filter by friends if needed
                val filteredUsers = if (scope == "friends") {
                    val currentUser = _currentUser.value
                    val friendIds = currentUser?.friends ?: emptyList()
                    allUsers.filter { it.id in friendIds || it.id == currentUser?.id } // include current user
                } else allUsers

                // Sort by selected score descending
                val sorted = filteredUsers.sortedByDescending { it.scores[timePeriod] ?: 0 }

                Log.d("UserViewModel", "Leaderboard loaded for $timePeriod ($scope):")
                sorted.forEachIndexed { index, user ->
                    val score = user.scores[timePeriod] ?: 0
                    Log.d("UserViewModel", "${index + 1}. ${user.name} (ID: ${user.id}) - Score: $score")
                }
                _leaderboard.value = sorted
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error loading leaderboard", e)
            }
        }
    }
}