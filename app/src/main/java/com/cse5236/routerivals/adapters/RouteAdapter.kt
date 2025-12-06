package com.cse5236.routerivals.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cse5236.routerivals.R
import com.cse5236.routerivals.model.Route

class RouteAdapter(
    private val onRouteAccepted: (Route) -> Unit
) : ListAdapter<Route, RouteAdapter.RouteViewHolder>(RouteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_route, parent, false)
        return RouteViewHolder(view, onRouteAccepted)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    class RouteViewHolder(
        itemView: View,
        private val onRouteAccepted: (Route) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val routeNumber: TextView = itemView.findViewById(R.id.text_route_number)
        private val routeDistance: TextView = itemView.findViewById(R.id.text_route_distance)
        private val startAddress: TextView = itemView.findViewById(R.id.text_start_address)
        private val endAddress: TextView = itemView.findViewById(R.id.text_end_address)
        private val acceptButton: Button = itemView.findViewById(R.id.button_accept_route)

        fun bind(route: Route, number: Int) {
            routeNumber.text = "Route #$number"
            routeDistance.text = String.format("%.1f km", route.distance)
            startAddress.text = route.startAddress
            endAddress.text = route.endAddress

            acceptButton.setOnClickListener {
                onRouteAccepted(route)
            }
        }
    }

    class RouteDiffCallback : DiffUtil.ItemCallback<Route>() {
        override fun areItemsTheSame(oldItem: Route, newItem: Route): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Route, newItem: Route): Boolean {
            return oldItem == newItem
        }
    }
}
