package com.cse5236.routerivals.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cse5236.routerivals.R
import com.cse5236.routerivals.model.LeaderboardEntry
import com.cse5236.routerivals.model.User   // <--- added

class LeaderboardAdapter :
    ListAdapter<LeaderboardEntry, LeaderboardAdapter.LeaderboardViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<LeaderboardEntry>() {
            override fun areItemsTheSame(
                oldItem: LeaderboardEntry,
                newItem: LeaderboardEntry
            ): Boolean {
                // same user
                return oldItem.userId == newItem.userId
            }

            override fun areContentsTheSame(
                oldItem: LeaderboardEntry,
                newItem: LeaderboardEntry
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard_row, parent, false)
        return LeaderboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val entry = getItem(position)
        val rank = position + 1
        holder.bind(rank, entry)
    }

    class LeaderboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val textRank: TextView = itemView.findViewById(R.id.textRank)
        private val textName: TextView = itemView.findViewById(R.id.textName)
        private val textPoints: TextView = itemView.findViewById(R.id.textPoints)

        fun bind(rank: Int, entry: LeaderboardEntry) {
            textRank.text = rank.toString()
            textName.text = entry.name
            textPoints.text = entry.points.toString()
        }
    }

    /**
     * Called from LeaderboardFragment:
     *   leaderboardAdapter.updateLeaderboard(users, selectedTime)
     *
     * Converts the List<User> from the ViewModel into a List<LeaderboardEntry>
     * for this adapter, based on the selected time period.
     */
    fun updateLeaderboard(users: List<User>, timePeriod: String) {
        val entries = users.map { user ->
            // scores[...] is Int, convert to Long
            val points: Long = (user.scores[timePeriod] ?: 0).toLong()

            LeaderboardEntry(
                userId = user.id,
                name = user.name,
                points = points
            )
        }
        submitList(entries)
    }

}
