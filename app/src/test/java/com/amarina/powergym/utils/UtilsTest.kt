package com.amarina.powergym.utils


import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UtilsTest {

    @Test
    fun isValidEmail_withValidEmail_returnsTrue() {
        assertTrue(Utils.isValidEmail("test@example.com"))
        assertTrue(Utils.isValidEmail("user.name@domain.co.uk"))
        assertTrue(Utils.isValidEmail("user-name@domain.com"))
    }

    @Test
    fun isValidEmail_withInvalidEmail_returnsFalse() {
        assertFalse(Utils.isValidEmail(""))
        assertFalse(Utils.isValidEmail("test"))
        assertFalse(Utils.isValidEmail("test@"))
        assertFalse(Utils.isValidEmail("@domain.com"))
        assertFalse(Utils.isValidEmail("test@domain"))
    }

    @Test
    fun isValidPassword_withValidPassword_returnsTrue() {
        assertTrue(Utils.isValidPassword("password"))
        assertTrue(Utils.isValidPassword("123456"))
        assertTrue(Utils.isValidPassword("pass123"))
    }

    @Test
    fun isValidPassword_withInvalidPassword_returnsFalse() {
        assertFalse(Utils.isValidPassword(""))
        assertFalse(Utils.isValidPassword("12345"))
        assertFalse(Utils.isValidPassword("pass"))
    }

    @Test
    fun formatTime_returnsCorrectFormat() {
        // 30 segundos
        assertEquals("00:30", Utils.formatTime(30))

        // 5 minutos
        assertEquals("05:00", Utils.formatTime(300))

        // 1 hora 15 minutos 30 segundos
        assertEquals("01:15:30", Utils.formatTime(4530))
    }

    @Test
    fun formatDate_returnsCorrectFormat() {
        // 1 de enero de 2022
        val timestamp = 1640995200000L // 2022-01-01 00:00:00
        assertEquals("01/01/2022", Utils.formatDate(timestamp))
    }
}
