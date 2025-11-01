package com.cse5236.routerivals.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.cse5236.routerivals.model.Leaderboard
import com.cse5236.routerivals.repository.LeaderboardRepository

class LeaderboardViewModel : ViewModel() {

    private val repository = LeaderboardRepository()
    val leaderboards: LiveData<List<Leaderboard>> = repository.leaderboardsLiveData

    fun fetch(scoreType: String, isGlobal: Boolean) {
        repository.fetchLeaderboards(scoreType, isGlobal)
    }
}
