package com.amarina.powergym

import android.app.Application
import com.amarina.powergym.database.PowerGymDatabase
import com.amarina.powergym.repository.EjercicioRepository
import com.amarina.powergym.repository.EstadisticaRepository
import com.amarina.powergym.repository.PreferenciaRepository
import com.amarina.powergym.repository.UserRepository
import com.amarina.powergym.ui.viewmodel.auth.factory.LoginViewModelFactory
import com.amarina.powergym.ui.viewmodel.auth.factory.RegisterViewModelFactory
import com.amarina.powergym.ui.viewmodel.ejercicio.EjercicioDetailViewModelFactory
import com.amarina.powergym.ui.viewmodel.main.factory.MainViewModelFactory
import com.amarina.powergym.ui.viewmodel.profile.ProfileViewModelFactory
import com.amarina.powergym.ui.viewmodel.settings.factory.SettingsViewModelFactory
import com.amarina.powergym.ui.viewmodel.statistics.StatisticsViewModelFactory
import com.amarina.powergym.utils.SessionManager

class PowerGymApplication : Application() {

    private val database by lazy { PowerGymDatabase.getInstance(this) }

    val userRepository by lazy { UserRepository(database.userDao()) }
    private val ejercicioRepository by lazy { EjercicioRepository(database.ejercicioDao()) }
    private val estadisticaRepository by lazy { EstadisticaRepository(database.estadisticaDao()) }
    private val preferenciaRepository by lazy { PreferenciaRepository(database.preferenciaDao()) }

    val sessionManager by lazy { SessionManager(this) }

    val loginViewModelFactory by lazy {
        LoginViewModelFactory(userRepository, sessionManager)
    }

    val registerViewModelFactory by lazy {
        RegisterViewModelFactory(userRepository)
    }

    val mainViewModelFactory by lazy {
        MainViewModelFactory(ejercicioRepository)
    }

    val settingsViewModelFactory by lazy {
        SettingsViewModelFactory(preferenciaRepository, sessionManager)
    }

    val statisticsViewModelFactory by lazy {
        StatisticsViewModelFactory(estadisticaRepository, sessionManager)
    }

    val profileViewModelFactory by lazy {
        ProfileViewModelFactory(userRepository, sessionManager)
    }

    val ejercicioDetailViewModelFactory by lazy {
        EjercicioDetailViewModelFactory(ejercicioRepository)
    }
}
