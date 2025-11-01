package com.cse5236.routerivals.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cse5236.routerivals.model.User
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    private val _usersLiveData = MutableLiveData<List<User>>()
    val usersLiveData: LiveData<List<User>> get() = _usersLiveData

    // CREATE / UPDATE
    fun saveUser(user: User) {
        usersCollection.document(user.id)
            .set(user)
            .addOnSuccessListener { println("User saved") }
            .addOnFailureListener { e -> println("Error saving user: $e") }
    }

    // READ
    fun fetchUsers() {
        usersCollection.get()
            .addOnSuccessListener { result ->
                val users = result.map { it.toObject(User::class.java) }
                _usersLiveData.postValue(users)
            }
            .addOnFailureListener { e -> println("Error fetching users: $e") }
    }

    fun getUser(userId: String, callback: (User?) -> Unit) {
        usersCollection.document(userId).get()
            .addOnSuccessListener { doc ->
                val user = doc.toObject(User::class.java)
                callback(user)
            }
            .addOnFailureListener { e ->
                println("Error fetching user: $e")
                callback(null)
            }
    }

    // DELETE
    fun deleteUser(userId: String) {
        usersCollection.document(userId)
            .delete()
            .addOnSuccessListener { println("User deleted") }
            .addOnFailureListener { e -> println("Error deleting user: $e") }
    }
}
