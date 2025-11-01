package com.cse5236.routerivals.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.cse5236.routerivals.model.User
import com.cse5236.routerivals.repository.UserRepository

class UserViewModel : ViewModel() {

    private val repository = UserRepository()

    val users: LiveData<List<User>> = repository.usersLiveData

    fun addOrUpdateUser(user: User) {
        repository.saveUser(user)
    }

    fun removeUser(userId: String) {
        repository.deleteUser(userId)
    }

    fun loadUsers() {
        repository.fetchUsers()
    }
}
