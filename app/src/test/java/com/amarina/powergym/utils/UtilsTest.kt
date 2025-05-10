package com.amarina.powergym.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UtilsTest {

    @Test
    fun esEmailValido_withValidEmail_returnsTrue() {
        // Standard emails
        assertTrue(Utils.esEmailValido("test@example.com"))
        assertTrue(Utils.esEmailValido("user.name@domain.co.uk"))
        assertTrue(Utils.esEmailValido("user-name@domain.com"))

        // Complex but valid emails
        assertTrue(Utils.esEmailValido("test+label@example.com"))
        assertTrue(Utils.esEmailValido("user_name123@domain.co.jp"))
        assertTrue(Utils.esEmailValido("first.last@subdomain.example.com"))
    }

    @Test
    fun esEmailValido_withInvalidEmail_returnsFalse() {
        assertFalse(Utils.esEmailValido(""))
        assertFalse(Utils.esEmailValido("test"))
        assertFalse(Utils.esEmailValido("test@"))
        assertFalse(Utils.esEmailValido("@domain.com"))
        assertFalse(Utils.esEmailValido("test@domain"))
        assertFalse(Utils.esEmailValido("test@.com"))
        assertFalse(Utils.esEmailValido("test@domain..com"))
        assertFalse(Utils.esEmailValido("test@domain."))
    }

    @Test
    fun esContrasenaValida_withValidPassword_returnsTrue() {
        // Minimum length passwords (6 characters)
        assertTrue(Utils.esContrasenaValida("123456"))
        assertTrue(Utils.esContrasenaValida("abcdef"))

        // Longer passwords
        assertTrue(Utils.esContrasenaValida("password123"))
        assertTrue(Utils.esContrasenaValida("LongerPassword"))
        assertTrue(Utils.esContrasenaValida("Complex-P@ssw0rd"))
        assertTrue(Utils.esContrasenaValida("VeryLongPasswordThatExceeds20Characters"))
    }

    @Test
    fun esContrasenaValida_withInvalidPassword_returnsFalse() {
        assertFalse(Utils.esContrasenaValida(""))
        assertFalse(Utils.esContrasenaValida("123"))
        assertFalse(Utils.esContrasenaValida("abcd"))
        assertFalse(Utils.esContrasenaValida("12345"))
        assertFalse(Utils.esContrasenaValida(" "))
    }

    @Test
    fun formatearFecha_returnsCorrectFormat() {
        // Create a Calendar for January 1, 2022
        val calendar = Calendar.getInstance()
        calendar.set(2022, Calendar.JANUARY, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val timestamp = calendar.timeInMillis
        val expectedFormat = "01/01/2022"

        assertEquals(expectedFormat, Utils.formatearFecha(timestamp))
    }

    @Test
    fun formatearFecha_worksWithDifferentDates() {
        // Test case for December 31, 2023
        val calendar = Calendar.getInstance()
        calendar.set(2023, Calendar.DECEMBER, 31, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val timestamp = calendar.timeInMillis
        val expectedFormat = "31/12/2023"

        assertEquals(expectedFormat, Utils.formatearFecha(timestamp))

        // Test case for February 29, 2024 (leap year)
        calendar.set(2024, Calendar.FEBRUARY, 29, 0, 0, 0)
        val leapYearTimestamp = calendar.timeInMillis
        val leapYearExpectedFormat = "29/02/2024"

        assertEquals(leapYearExpectedFormat, Utils.formatearFecha(leapYearTimestamp))
    }

    @Test
    fun formatearFecha_matchesSimpleDateFormat() {
        val now = System.currentTimeMillis()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val expectedDate = sdf.format(Date(now))

        assertEquals(expectedDate, Utils.formatearFecha(now))
    }
}