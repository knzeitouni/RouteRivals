package com.cse5236.routerivals.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cse5236.routerivals.model.Leaderboard
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class LeaderboardRepository {

    private val db = FirebaseFirestore.getInstance()
    private val leaderboardCollection = db.collection("leaderboards")

    private val _leaderboardsLiveData = MutableLiveData<List<Leaderboard>>()
    val leaderboardsLiveData: LiveData<List<Leaderboard>> get() = _leaderboardsLiveData

    fun fetchLeaderboards(scoreType: String, isGlobal: Boolean) {
        var query: Query = leaderboardCollection
            .whereEqualTo("scoreType", scoreType)
            .whereEqualTo("isGlobal", isGlobal)
            .orderBy("entries.score", Query.Direction.DESCENDING)

        query.get()
            .addOnSuccessListener { result ->
                val boards = result.map { it.toObject(Leaderboard::class.java) }
                _leaderboardsLiveData.postValue(boards)
            }
            .addOnFailureListener { e -> println("Error fetching leaderboard: $e") }
    }
}
