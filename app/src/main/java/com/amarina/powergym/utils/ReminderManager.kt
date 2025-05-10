package com.amarina.powergym.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.amarina.powergym.R
import com.amarina.powergym.receivers.ReminderReceiver
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ReminderManager(private val context: Context) {

    companion object {
        private const val REMINDER_REQUEST_CODE = 100
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Schedule reminders based on frequency
     */
    fun programarRecordatorios(frecuencia: String) {
        cancelarRecordatorios() // Cancel any existing reminders first
        
        // Create the intent for the broadcast receiver
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Set up calendar for reminder time (default is 6:00 PM)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 18)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            // If the time is already past for today, set it for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        when (frecuencia) {
            context.getString(R.string.frequency_daily) -> {
                // Schedule daily reminders
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }
            context.getString(R.string.frequency_every_other_day) -> {
                // Schedule every other day reminders
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY * 2,
                    pendingIntent
                )
            }
            context.getString(R.string.frequency_twice_weekly) -> {
                // Schedule twice weekly (every 3-4 days)
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    TimeUnit.DAYS.toMillis(3),
                    pendingIntent
                )
            }
            context.getString(R.string.frequency_weekly) -> {
                // Schedule weekly reminders
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY * 7,
                    pendingIntent
                )
            }
            context.getString(R.string.frequency_monthly) -> {
                // Schedule monthly reminders (approximately every 30 days)
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY * 30,
                    pendingIntent
                )
            }
        }
    }

    /**
     * Cancel all scheduled reminders
     */
    fun cancelarRecordatorios() {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}