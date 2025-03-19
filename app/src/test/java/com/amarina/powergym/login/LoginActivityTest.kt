package com.amarina.powergym.login


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.amarina.powergym.R
import com.amarina.powergym.ui.activity.LoginActivity
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import android.view.View
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
        onView(withId(R.id.btnIniciarSesion))
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
        onView(withId(R.id.btnIniciarSesion))
            .perform(click())

        // Verify error message
        onView(withId(R.id.etPassword))
            .check(matches(hasTextInputLayoutErrorText("La contraseña debe tener al menos 6 caracteres")))
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
