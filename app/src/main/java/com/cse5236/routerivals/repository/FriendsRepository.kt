package com.cse5236.routerivals.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class FriendsRepository {
    // TODO: Replace with firestore
    private val friendsData = mutableListOf("Alice", "Bob", "Charlie")
    private val friendsLiveData = MutableLiveData<List<String>>(friendsData)

    fun getFriends(): LiveData<List<String>> = friendsLiveData

    fun addFriend(name: String) {
        friendsData.add(name)
        friendsLiveData.value = friendsData.toList()
    }

    fun removeFriend(name: String) {
        friendsData.remove(name)
        friendsLiveData.value = friendsData.toList()
    }
}
