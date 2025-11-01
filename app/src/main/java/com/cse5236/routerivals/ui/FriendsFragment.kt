package com.cse5236.routerivals.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cse5236.routerivals.R

class FriendsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_friends, container, false)

        // Example: reference a TextView in the fragment
        // val textView = view.findViewById<TextView>(R.id.text_friends)
        // textView.text = "This is the Friends screen"

        return view
    }
}