package com.cse5236.routerivals

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityUITest {

    // Test 1: Home Fragment
    @Test
    fun homeFragment_showsDistanceInputAndButton() {
        ActivityScenario.launch(MainActivity::class.java)

        // Check that distance input is displayed
        onView(withId(R.id.input_distance))
            .check(matches(isDisplayed()))

        // Check that find routes button is displayed
        onView(withId(R.id.button_find_routes))
            .check(matches(isDisplayed()))
    }

    // Test 2: Friends Fragment
    @Test
    fun friendsFragment_canNavigateAndDisplay() {
        ActivityScenario.launch(MainActivity::class.java)

        // Navigate to Friends tab
        onView(withId(R.id.nav_friends)).perform(click())

        // Example check: verify RecyclerView or placeholder
        onView(withId(R.id.recyclerViewFriends)) // replace with actual ID
            .check(matches(isDisplayed()))
    }

    // Test 3: Leaderboard Fragment
    @Test
    fun leaderboardFragment_canNavigateAndDisplay() {
        ActivityScenario.launch(MainActivity::class.java)

        // Navigate to Leaderboard tab
        onView(withId(R.id.nav_leaderboard)).perform(click())

        // Check a leaderboard element is visible
        onView(withId(R.id.recyclerViewLeaderboard)) // replace with actual ID
            .check(matches(isDisplayed()))
    }
}
