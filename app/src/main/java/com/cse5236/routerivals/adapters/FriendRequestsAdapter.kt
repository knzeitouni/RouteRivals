package com.cse5236.routerivals.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cse5236.routerivals.R
import com.cse5236.routerivals.model.User

class FriendRequestsAdapter(
    private val onAccept: (User) -> Unit,
    private val onDecline: (User) -> Unit
) : RecyclerView.Adapter<FriendRequestsAdapter.ViewHolder>() {

    private val friendRequests = mutableListOf<User>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.textUsername)
        val acceptButton: Button = itemView.findViewById(R.id.buttonAccept)
        val declineButton: Button = itemView.findViewById(R.id.buttonDecline)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = friendRequests[position]

        holder.usernameTextView.text = user.name

        holder.acceptButton.setOnClickListener {
            onAccept(user)
        }

        holder.declineButton.setOnClickListener {
            onDecline(user)
        }
    }

    override fun getItemCount(): Int {
        return friendRequests.size
    }

    fun updateFriendRequests(newRequests: List<User>) {
        friendRequests.clear()
        friendRequests.addAll(newRequests)
        notifyDataSetChanged()
    }
}