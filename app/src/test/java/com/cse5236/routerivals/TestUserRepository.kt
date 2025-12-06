package com.cse5236.routerivals
import com.cse5236.routerivals.model.User

class TestUserRepository {

    val users = mutableMapOf<String, User>()

    suspend fun saveUser(user: User) {
        users[user.id] = user
    }

    suspend fun getUser(userId: String): User? {
        return users[userId]
    }

    suspend fun fetchUsers(): List<User> {
        return users.values.toList()
    }

    suspend fun sendFriendRequest(fromUid: String, toUid: String) {
        val from = users[fromUid] ?: return
        val to = users[toUid] ?: return

        users[fromUid] = from.copy(
            outgoingRequests = from.outgoingRequests + toUid
        )
        users[toUid] = to.copy(
            incomingRequests = to.incomingRequests + fromUid
        )
    }

    suspend fun acceptFriendRequest(acceptingUid: String, requestingUid: String) {
        val acc = users[acceptingUid] ?: return
        val req = users[requestingUid] ?: return

        users[acceptingUid] = acc.copy(
            incomingRequests = acc.incomingRequests - requestingUid,
            friends = acc.friends + requestingUid
        )

        users[requestingUid] = req.copy(
            outgoingRequests = req.outgoingRequests - acceptingUid,
            friends = req.friends + acceptingUid
        )
    }

    suspend fun declineFriendRequest(decliningUid: String, requestingUid: String) {
        val dec = users[decliningUid] ?: return
        val req = users[requestingUid] ?: return

        users[decliningUid] = dec.copy(
            incomingRequests = dec.incomingRequests - requestingUid
        )
        users[requestingUid] = req.copy(
            outgoingRequests = req.outgoingRequests - decliningUid
        )
    }

    suspend fun removeFriend(userId: String, friendId: String) {
        val a = users[userId] ?: return
        val b = users[friendId] ?: return

        users[userId] = a.copy(
            friends = a.friends - friendId
        )
        users[friendId] = b.copy(
            friends = b.friends - userId
        )
    }
}
