package com.cse5236.routerivals.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.cse5236.routerivals.repository.FriendsRepository

class FriendsViewModel : ViewModel() {
    private val repository = FriendsRepository()
    val friendsList: LiveData<List<String>> = repository.getFriends()
}
