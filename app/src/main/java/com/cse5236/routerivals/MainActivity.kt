package com.cse5236.routerivals

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cse5236.routerivals.ui.FriendsFragment
import com.cse5236.routerivals.ui.HomeFragment
import com.cse5236.routerivals.ui.LeaderboardFragment
import com.cse5236.routerivals.model.User
import com.cse5236.routerivals.repository.UserRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import androidx.lifecycle.lifecycleScope
import com.cse5236.routerivals.ui.ProfileFragment
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivityLifecycle"
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Run CRUD demo
        demonstrateCRUD()

        // Default fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setOnItemSelectedListener { item ->
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_friends -> FriendsFragment()
                R.id.nav_leaderboard -> LeaderboardFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> HomeFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .commit()
            true
        }
    }
    private fun demonstrateCRUD() {
        val usersRepo = UserRepository()
        lifecycleScope.launch {
            try {
                // ---- Create ----
                val testUser = User(
                    id = "JonRuns",
                    name = "Jon",
                    email = "JonDoe@emailprovider.com",
                    friends = emptyList(),
                    scores = emptyMap(),
                    completedRoutes = emptyList()
                )
                usersRepo.saveUser(testUser)

                // ---- Read ----
                val retrievedUser = usersRepo.getUser(testUser.id)
                Log.d(TAG, "Retrieved user: $retrievedUser")

                // ---- Update ----
                testUser.name = "Jon Doe"
                usersRepo.saveUser(testUser)
                Log.d(TAG, "User updated")

                // ---- Delete ----
                usersRepo.deleteUser(testUser.id)
                Log.d(TAG, "User deleted")

            } catch (e: Exception) {
                Log.e(TAG, "CRUD demo error: $e")
            }
        }



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

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }
}