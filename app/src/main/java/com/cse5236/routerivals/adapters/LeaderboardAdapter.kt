package com.cse5236.routerivals.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cse5236.routerivals.R
import com.cse5236.routerivals.model.User

class LeaderboardAdapter(
    private var userList: List<User> = emptyList(), // make mutable
    private var timeScope: String = "allTime"
) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rankTextView: TextView = itemView.findViewById(R.id.textRank)
        val nameTextView: TextView = itemView.findViewById(R.id.textUsername)
        val scoreTextView: TextView = itemView.findViewById(R.id.textScore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.rankTextView.text = (position + 1).toString()
        holder.nameTextView.text = user.name
        holder.scoreTextView.text = (user.scores[timeScope] ?: 0).toString()
    }

    override fun getItemCount(): Int = userList.size

    // Add this method to update the leaderboard dynamically
    fun updateLeaderboard(newUsers: List<User>, newTimeScope: String) {
        userList = newUsers
        timeScope = newTimeScope
        notifyDataSetChanged()
    }
}
