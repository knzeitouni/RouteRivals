package com.cse5236.routerivals.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cse5236.routerivals.R
import com.cse5236.routerivals.adapters.FriendRequestsAdapter
import com.cse5236.routerivals.adapters.FriendsAdapter
import com.cse5236.routerivals.adapters.LeaderboardAdapter
import com.cse5236.routerivals.viewmodel.UserViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch

class LeaderboardFragment : Fragment() {

    private val TAG = "LeaderboardFragmentLifecycle"
    private val userViewModel: UserViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private lateinit var scopeChipGroup: ChipGroup
    private lateinit var timeChips: List<Chip>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_leaderboard, container, false)

        // Create test data first
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

        // Reload leaderboard when chips change
        timeChips.forEach { chip ->
            chip.setOnClickListener { loadLeaderboard() }
        }
        scopeChipGroup.setOnCheckedChangeListener { _, _ -> loadLeaderboard() }

        // Initial load
        loadLeaderboard()
    }

    private fun setupObservers() {
        userViewModel.leaderboard.observe(viewLifecycleOwner) { users ->
            val selectedTime = getSelectedTime()
            Log.d(TAG, "Updating RecyclerView with ${users.size} users for $selectedTime")
            leaderboardAdapter.updateLeaderboard(users, selectedTime)
        }
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