package com.amarina.powergym.login

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.amarina.powergym.R
import com.amarina.powergym.ui.activity.RegisterActivity
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class RegisterActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(RegisterActivity::class.java)

    @Test
    fun invalidEmail_showsError() {
        // Enter valid name
        onView(withId(R.id.etNameRegister))
            .perform(typeText("Test User"), closeSoftKeyboard())

        // Enter invalid email
        onView(withId(R.id.etEmailRegister))
            .perform(typeText("invalid"), closeSoftKeyboard())

        // Enter valid password
        onView(withId(R.id.etPasswordRegister))
            .perform(typeText("password123"), closeSoftKeyboard())

        // Enter same password for confirmation
        onView(withId(R.id.etConfirmPasswordRegister))
            .perform(typeText("password123"), closeSoftKeyboard())

        // Click register button
        onView(withId(R.id.btnRegister))
            .perform(click())

        // Verify error message for email
        onView(withId(R.id.etEmailRegister))
            .check(matches(hasTextInputLayoutErrorText("Email no válido")))
    }

    @Test
    fun invalidPassword_showsError() {
        // Enter valid name
        onView(withId(R.id.etNameRegister))
            .perform(typeText("Test User"), closeSoftKeyboard())

        // Enter valid email
        onView(withId(R.id.etEmailRegister))
            .perform(typeText("test@example.com"), closeSoftKeyboard())

        // Enter invalid password (too short)
        onView(withId(R.id.etPasswordRegister))
            .perform(typeText("pass"), closeSoftKeyboard())

        // Confirm password
        onView(withId(R.id.etConfirmPasswordRegister))
            .perform(typeText("pass"), closeSoftKeyboard())

        // Click register button
        onView(withId(R.id.btnRegister))
            .perform(click())

        // Verify error message for password
        onView(withId(R.id.etPasswordRegister))
            .check(matches(hasTextInputLayoutErrorText("La contraseña debe tener al menos 6 caracteres")))
    }

    @Test
    fun passwordMismatch_showsError() {
        // Enter valid name
        onView(withId(R.id.etNameRegister))
            .perform(typeText("Test User"), closeSoftKeyboard())

        // Enter valid email
        onView(withId(R.id.etEmailRegister))
            .perform(typeText("test@example.com"), closeSoftKeyboard())

        // Enter valid password
        onView(withId(R.id.etPasswordRegister))
            .perform(typeText("password123"), closeSoftKeyboard())

        // Enter different password for confirmation
        onView(withId(R.id.etConfirmPasswordRegister))
            .perform(typeText("different123"), closeSoftKeyboard())

        // Click register button
        onView(withId(R.id.btnRegister))
            .perform(click())

        // Verify error message for password confirmation
        onView(withId(R.id.etConfirmPasswordRegister))
            .check(matches(hasTextInputLayoutErrorText("Las contraseñas no coinciden")))
    }

    @Test
    fun emptyName_showsError() {
        // Leave name empty
        // Enter valid email
        onView(withId(R.id.etEmailRegister))
            .perform(typeText("test@example.com"), closeSoftKeyboard())

        // Enter valid password
        onView(withId(R.id.etPasswordRegister))
            .perform(typeText("password123"), closeSoftKeyboard())

        // Confirm password
        onView(withId(R.id.etConfirmPasswordRegister))
            .perform(typeText("password123"), closeSoftKeyboard())

        // Click register button
        onView(withId(R.id.btnRegister))
            .perform(click())

        // Verify error message for name
        onView(withId(R.id.etNameRegister))
            .check(matches(hasTextInputLayoutErrorText("Nombre requerido")))
    }

    // Helper function to check TextInputLayout error message
    private fun hasTextInputLayoutErrorText(expectedErrorText: String) =
        object : TypeSafeMatcher<View>() {
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