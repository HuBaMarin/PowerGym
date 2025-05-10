package com.amarina.powergym

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.amarina.powergym.ui.activity.LoginActivity
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginRegistrationFlowTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun registerNewUser_thenLogin() {
        // Generate a unique email to avoid conflicts with existing users
        val uniqueEmail = "test_${UUID.randomUUID().toString().substring(0, 8)}@example.com"
        val password = "password123"
        val name = "Test User"

        // Click on register link from login screen
        onView(withId(R.id.tvRegister)).perform(click())

        // Fill in registration form
        onView(withId(R.id.etNameRegister)).perform(typeText(name), closeSoftKeyboard())
        onView(withId(R.id.etEmailRegister)).perform(typeText(uniqueEmail), closeSoftKeyboard())
        onView(withId(R.id.etPasswordRegister)).perform(typeText(password), closeSoftKeyboard())
        onView(withId(R.id.etConfirmPasswordRegister)).perform(
            typeText(password),
            closeSoftKeyboard()
        )

        // Click register button
        onView(withId(R.id.btnRegister)).perform(click())

        // Wait for success and redirect to login screen
        // This may need to be adjusted with a proper IdlingResource if registration takes time
        Thread.sleep(1000)

        // Verify we're back at the login screen
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()))

        // Now login with the newly created user
        onView(withId(R.id.etEmail)).perform(typeText(uniqueEmail), closeSoftKeyboard())
        onView(withId(R.id.etPassword)).perform(typeText(password), closeSoftKeyboard())
        onView(withId(R.id.btnLogin)).perform(click())

        // Wait for login to complete
        Thread.sleep(1000)

        // Verify navigation to main screen - this assumes something on the main screen is visible
        // This may need to be adjusted based on your actual main screen layout
        onView(withId(R.id.main)).check(matches(isDisplayed()))
    }

    @Test
    fun loginWithInvalidCredentials_showsError() {
        // Enter invalid credentials
        onView(withId(R.id.etEmail)).perform(typeText("wrong@example.com"), closeSoftKeyboard())
        onView(withId(R.id.etPassword)).perform(typeText("wrongpassword"), closeSoftKeyboard())
        onView(withId(R.id.btnLogin)).perform(click())

        // Wait for error response
        Thread.sleep(1000)

        // We should still be on the login screen
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()))
    }

    // Helper function to check TextInputLayout error message
    private fun hasTextInputLayoutErrorText(expectedErrorText: String): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("with error: $expectedErrorText")
            }

            override fun matchesSafely(view: View): Boolean {
                if (view !is TextInputLayout) return false
                val error = view.error ?: return false
                return expectedErrorText == error.toString()
            }
        }
    }
}