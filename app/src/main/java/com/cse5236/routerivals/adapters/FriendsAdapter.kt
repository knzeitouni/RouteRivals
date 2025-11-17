package com.cse5236.routerivals.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cse5236.routerivals.R
import com.cse5236.routerivals.model.User

class FriendsAdapter(
    private val onRemoveFriend: (User) -> Unit
) : RecyclerView.Adapter<FriendsAdapter.ViewHolder>() {

    private val friendsList = mutableListOf<User>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.textUsername)
        val removeButton: Button = itemView.findViewById(R.id.buttonRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = friendsList[position]

        holder.usernameTextView.text = user.name // or user.id

        holder.removeButton.setOnClickListener {
            onRemoveFriend(user)
        }
    }

    override fun getItemCount(): Int {
        return friendsList.size
    }

    fun updateFriends(newFriends: List<User>) {
        friendsList.clear()
        friendsList.addAll(newFriends)
        notifyDataSetChanged()
    }
}