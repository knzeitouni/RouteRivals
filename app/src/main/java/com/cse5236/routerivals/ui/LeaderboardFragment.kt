package com.cse5236.routerivals.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cse5236.routerivals.R
import com.cse5236.routerivals.adapters.LeaderboardAdapter
import com.cse5236.routerivals.model.LeaderboardEntry
import com.cse5236.routerivals.model.User
import com.cse5236.routerivals.viewmodel.UserViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class LeaderboardFragment : Fragment() {

    private val tagLog = "LeaderboardFragmentLifecycle"
    private val userViewModel: UserViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private lateinit var scopeChipGroup: ChipGroup
    private lateinit var timeChips: List<Chip>

    private lateinit var textViewYourPoints: TextView
    private lateinit var textViewYourRank: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(tagLog, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_leaderboard, container, false)

        // Seed test data if needed (implementation is in the ViewModel)
        userViewModel.createLeaderboardTestData()

        setupView(view)
        setupObservers()

        return view
    }

    private fun setupView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewLeaderboard)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        leaderboardAdapter = LeaderboardAdapter()
        recyclerView.adapter = leaderboardAdapter

        scopeChipGroup = view.findViewById(R.id.scopeChipGroup)
        timeChips = listOf(
            view.findViewById(R.id.chipAllTime),
            view.findViewById(R.id.chipMonthly),
            view.findViewById(R.id.chipDaily)
        )

        textViewYourPoints = view.findViewById(R.id.textViewYourPoints)
        textViewYourRank = view.findViewById(R.id.textViewYourRank)

        // Reload leaderboard when chips change
        timeChips.forEach { chip ->
            chip.setOnClickListener { loadLeaderboard() }
        }
        scopeChipGroup.setOnCheckedChangeListener { _, _ -> loadLeaderboard() }

        // Initial load
        loadLeaderboard()
        userViewModel.loadCurrentUser()
    }

    private fun setupObservers() {
        userViewModel.leaderboard.observe(viewLifecycleOwner) { users ->
            val selectedTime = getSelectedTime()
            Log.d(tagLog, "Updating RecyclerView with ${users.size} users for $selectedTime")

            val entries: List<LeaderboardEntry> = users.toLeaderboardEntries(selectedTime)
            leaderboardAdapter.submitList(entries)

            updateYourStats(users, selectedTime)
        }

        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                val selectedTime = getSelectedTime()
                val points = user.getPointsFor(selectedTime)
                textViewYourPoints.text = points.toString()
                Log.d(tagLog, "Updated current user points: $points for $selectedTime, scores=${user.scores}")
            }
        }
    }

    // ---- Helpers ----

    // Safely read points from the scores map.
    // If we're on "allTime", also check for "totalPoints" to handle old docs.
    private fun User.getPointsFor(timePeriod: String): Int {
        return when (timePeriod) {
            "allTime" -> this.scores["allTime"]
                ?: this.scores["totalPoints"]
                ?: 0
            else -> this.scores[timePeriod] ?: 0
        }
    }

    // Convert List<User> -> List<LeaderboardEntry>
    private fun List<User>.toLeaderboardEntries(timePeriod: String): List<LeaderboardEntry> {
        return this.map { user ->
            val points = user.getPointsFor(timePeriod)
            LeaderboardEntry(
                userId = user.id,
                name = user.name,
                points = points
            )
        }
    }

    private fun updateYourStats(users: List<User>, timePeriod: String) {
        val currentUserId = userViewModel.getCurrentUserId()

        val yourIndex = users.indexOfFirst { it.id == currentUserId }
        val yourRank = if (yourIndex >= 0) yourIndex + 1 else -1

        val yourUser = users.find { it.id == currentUserId }
        val yourPoints = yourUser?.getPointsFor(timePeriod) ?: 0

        textViewYourPoints.text = yourPoints.toString()
        textViewYourRank.text = if (yourRank > 0) "#$yourRank" else "--"

        Log.d(
            tagLog,
            "Your stats - Rank: $yourRank, Points: $yourPoints, scores=${yourUser?.scores}"
        )
    }

    private fun getSelectedTime(): String {
        return when (timeChips.firstOrNull { it.isChecked }?.id) {
            R.id.chipAllTime -> "allTime"
            R.id.chipMonthly -> "monthly"
            R.id.chipDaily -> "daily"
            else -> "allTime"
        }
    }

    private fun loadLeaderboard() {
        val timePeriod = getSelectedTime()
        val scope = if (scopeChipGroup.checkedChipId == R.id.chipFriends) "friends" else "all"
        userViewModel.loadLeaderboard(timePeriod, scope)
    }

    override fun onResume() {
        super.onResume()
        Log.d(tagLog, "onResume")
        loadLeaderboard()
        userViewModel.loadCurrentUser()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(tagLog, "onDestroyView")
        userViewModel.unloadLeaderboardData()
    }

    override fun onDestroy() {
        super.onDestroy()
        recyclerView.adapter = null
        Log.d(tagLog, "onDestroy")
    }
}
