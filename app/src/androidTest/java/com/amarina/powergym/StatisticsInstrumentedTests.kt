package com.amarina.powergym

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amarina.powergym.ui.activity.StatisticsActivity
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.startsWith
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for the Statistics feature of PowerGym application.
 *
 * These tests verify that statistics functionality works correctly in a real device
 * or emulator environment. Testing includes data retrieval, calculation accuracy,
 * UI display, and user interaction with the statistics screens.
 */
@RunWith(AndroidJUnit4::class)
class StatisticsInstrumentedTests {

    
    /**
     * Tests that the Statistics activity launches properly and
     * shows the expected header.
     */
    @Test
    fun testStatisticsActivityLaunch() {
        // Launch the activity
        ActivityScenario.launch(StatisticsActivity::class.java)
        
        // Verify the header is displayed
        onView(withId(R.id.tvMostFrequentExercises))
            .check(matches(isDisplayed()))
        
        onView(withText("Progress"))
            .check(matches(isDisplayed()))
    }
    
    /**
     * Tests that the most frequent exercises list is displayed and populated with data.
     */
    @Test
    fun testMostFrequentExercisesListDisplayed() {
        ActivityScenario.launch(StatisticsActivity::class.java)
        
        // Verify exercises list container is displayed
        onView(withId(R.id.rvGruposATrabajar))
            .check(matches(isDisplayed()))
            
        // Verify list contains at least one valid item
        onView(withId(R.id.rvGruposATrabajar))
            .check(matches(hasMinimumChildCount(1)))
            
        // Validate first item's content
        onView(withId(R.id.rvGruposATrabajar))
            .check(matches(hasDescendant(withText(startsWith("grupoMuscular: piernas")))))

    }
    
   
    

}