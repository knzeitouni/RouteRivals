package com.cse5236.routerivals.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cse5236.routerivals.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val usersCollection = db.collection("users")

    private val _usersLiveData = MutableLiveData<List<User>>()
    val usersLiveData: LiveData<List<User>> get() = _usersLiveData

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

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

    // TODO: remove fromUid and use currentUser when login is implemented
    suspend fun sendFriendRequest(fromUid: String, toUid: String) = suspendCancellableCoroutine<Unit> { cont ->
        /*val fromUid = auth.currentUser?.uid
        if (fromUid == null) {
            cont.resumeWithException(Exception("User not logged in"))
            return@suspendCancellableCoroutine
        }*/

        val fromRef = usersCollection.document(fromUid)
        val toRef = usersCollection.document(toUid)

        db.runBatch { batch ->
            batch.update(fromRef, "outgoingRequests", FieldValue.arrayUnion(toUid))
            batch.update(toRef, "incomingRequests", FieldValue.arrayUnion(fromUid))
        }.addOnSuccessListener {
            cont.resume(Unit)
        }.addOnFailureListener { e ->
            cont.resumeWithException(e)
        }
    }

    suspend fun acceptFriendRequest(acceptingUid: String, requestingUid: String) = suspendCancellableCoroutine<Unit> { cont ->
        val acceptingRef = usersCollection.document(acceptingUid)
        val requestingRef = usersCollection.document(requestingUid)

        db.runBatch { batch ->
            // For the user accepting the request
            batch.update(acceptingRef, "incomingRequests", FieldValue.arrayRemove(requestingUid))
            batch.update(acceptingRef, "friends", FieldValue.arrayUnion(requestingUid))

            // For the user who sent the request
            batch.update(requestingRef, "outgoingRequests", FieldValue.arrayRemove(acceptingUid))
            batch.update(requestingRef, "friends", FieldValue.arrayUnion(acceptingUid))
        }.addOnSuccessListener {
            cont.resume(Unit)
        }.addOnFailureListener { e ->
            cont.resumeWithException(e)
        }
    }

    suspend fun declineFriendRequest(decliningUid: String, requestingUid: String) = suspendCancellableCoroutine<Unit> { cont ->
        val decliningRef = usersCollection.document(decliningUid)
        val requestingRef = usersCollection.document(requestingUid)

        db.runBatch { batch ->
            batch.update(decliningRef, "incomingRequests", FieldValue.arrayRemove(requestingUid))
            batch.update(requestingRef, "outgoingRequests", FieldValue.arrayRemove(decliningUid))
        }.addOnSuccessListener {
            cont.resume(Unit)
        }.addOnFailureListener { e ->
            cont.resumeWithException(e)
        }
    }

    suspend fun removeFriend(userId: String, friendId: String) = suspendCancellableCoroutine<Unit> { cont ->

        val userRef = usersCollection.document(userId)
        val friendRef = usersCollection.document(friendId)


        db.runBatch() { batch ->
            batch.update(userRef, "friends", FieldValue.arrayRemove(friendId))
            batch.update(friendRef, "friends", FieldValue.arrayRemove(userId))
            }.addOnSuccessListener {
            cont.resume(Unit)
        }.addOnFailureListener { e ->
            cont.resumeWithException(e)
        }
    }

}
