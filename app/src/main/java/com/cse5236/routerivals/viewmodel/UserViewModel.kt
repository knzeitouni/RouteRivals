package com.cse5236.routerivals.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cse5236.routerivals.model.User
import com.cse5236.routerivals.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val repository = UserRepository()

    val users: LiveData<List<User>> = repository.usersLiveData

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
}
