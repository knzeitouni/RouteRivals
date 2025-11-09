package com.cse5236.routerivals.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cse5236.routerivals.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    private val _usersLiveData = MutableLiveData<List<User>>()
    val usersLiveData: LiveData<List<User>> get() = _usersLiveData

    // CREATE / UPDATE
    suspend fun saveUser(user: User) = suspendCancellableCoroutine<Unit> { cont ->
        usersCollection.document(user.id)
            .set(user)
            .addOnSuccessListener {
                println("User saved: ${user.id}")
                cont.resume(Unit)
            }
            .addOnFailureListener { e ->
                println("Error saving user: $e")
                cont.resumeWithException(e)
            }
    }

    suspend fun getUser(userId: String): User? = suspendCancellableCoroutine { cont ->
        usersCollection.document(userId)
            .get()
            .addOnSuccessListener { doc ->
                cont.resume(doc.toObject(User::class.java))
            }
            .addOnFailureListener { e ->
                println("Error getting user: $e")
                cont.resumeWithException(e)
            }
    }

    suspend fun fetchUsers(): List<User> = suspendCancellableCoroutine { cont ->
        usersCollection.get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
                cont.resume(users)
            }
            .addOnFailureListener { e ->
                println("Error fetching users: $e")
                cont.resumeWithException(e)
            }
    }

    suspend fun deleteUser(userId: String) = suspendCancellableCoroutine<Unit> { cont ->
        usersCollection.document(userId)
            .delete()
            .addOnSuccessListener {
                println("User deleted: $userId")
                cont.resume(Unit)
            }
            .addOnFailureListener { e ->
                println("Error deleting user: $e")
                cont.resumeWithException(e)
            }
    }
}
