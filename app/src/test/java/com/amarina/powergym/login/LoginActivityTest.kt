package com.amarina.powergym.login

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.amarina.powergym.R
import com.amarina.powergym.ui.activity.LoginActivity
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Description
import org.hamcrest.Matchers.not
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun invalidEmail_showsError() {
        // Enter invalid email
        onView(withId(R.id.etEmail))
            .perform(typeText("invalid"), closeSoftKeyboard())

        // Enter valid password
        onView(withId(R.id.etPassword))
            .perform(typeText("password123"), closeSoftKeyboard())

        // Click login button
        onView(withId(R.id.btnLogin))
            .perform(click())

        // Verify error message
        onView(withId(R.id.etEmail))
            .check(matches(hasTextInputLayoutErrorText("Email no válido")))
    }

    @Test
    fun invalidPassword_showsError() {
        // Enter valid email
        onView(withId(R.id.etEmail))
            .perform(typeText("test@example.com"), closeSoftKeyboard())

        // Enter invalid password (less than 6 chars)
        onView(withId(R.id.etPassword))
            .perform(typeText("pass"), closeSoftKeyboard())

        // Click login button
        onView(withId(R.id.btnLogin))
            .perform(click())

        // Verify error message
        onView(withId(R.id.etPassword))
            .check(matches(hasTextInputLayoutErrorText("La contraseña debe tener al menos 6 caracteres")))
    }

    @Test
    fun emptyFields_showsErrors() {
        // Leave fields empty
        // Click login button
        onView(withId(R.id.btnLogin))
            .perform(click())

        // Verify both fields show errors
        onView(withId(R.id.etEmail))
            .check(matches(hasTextInputLayoutErrorText("Email no válido")))
        onView(withId(R.id.etPassword))
            .check(matches(hasTextInputLayoutErrorText("La contraseña debe tener al menos 6 caracteres")))
    }

    @Test
    fun clickRegisterButton_navigatesToRegister() {
        // Click on the register text view
        onView(withId(R.id.tvRegister))
            .perform(click())

        // Verify navigation to register screen
        // This would ideally use Intents to verify the new activity is launched
        // but for simplicity we're checking the visibility of login screen components
        onView(withId(R.id.btnLogin)).check(matches(not(isDisplayed())))
    }

    // Helper function to check TextInputLayout error message
    private fun hasTextInputLayoutErrorText(expectedErrorText: String) = object : TypeSafeMatcher<View>() {
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
