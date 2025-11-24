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
import com.cse5236.routerivals.viewmodel.UserViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class LeaderboardFragment : Fragment() {

    private val TAG = "LeaderboardFragmentLifecycle"
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
        Log.d(TAG, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_leaderboard, container, false)

        // Create test data only once (won't overwrite existing user data)
        userViewModel.createLeaderboardTestData()

        setupView(view)
        setupObservers()

        return view
    }

    private fun setupView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewLeaderboard)
        recyclerView.layoutManager = LinearLayoutManager(context)

        leaderboardAdapter = LeaderboardAdapter()
        recyclerView.adapter = leaderboardAdapter

        scopeChipGroup = view.findViewById(R.id.scopeChipGroup)
        timeChips = listOf(
            view.findViewById(R.id.chipAllTime),
            view.findViewById(R.id.chipMonthly),
            view.findViewById(R.id.chipDaily)
        )

        // Initialize TextViews for points card
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
            Log.d(TAG, "Updating RecyclerView with ${users.size} users for $selectedTime")
            leaderboardAdapter.updateLeaderboard(users, selectedTime)

            // Update your points and rank
            updateYourStats(users, selectedTime)
        }

        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                val selectedTime = getSelectedTime()
                val points = user.scores[selectedTime] ?: 0
                textViewYourPoints.text = points.toString()
                Log.d(TAG, "Updated current user points: $points for $selectedTime")
            }
        }
    }

    private fun updateYourStats(users: List<com.cse5236.routerivals.model.User>, timePeriod: String) {
        val currentUserId = userViewModel.getCurrentUserId()
        val yourRank = users.indexOfFirst { it.id == currentUserId } + 1
        val yourPoints = users.find { it.id == currentUserId }?.scores?.get(timePeriod) ?: 0

        textViewYourPoints.text = yourPoints.toString()
        textViewYourRank.text = if (yourRank > 0) "#$yourRank" else "-"

        Log.d(TAG, "Your stats - Rank: $yourRank, Points: $yourPoints")
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

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        // Refresh leaderboard when returning to this fragment
        loadLeaderboard()
        userViewModel.loadCurrentUser()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }
}
