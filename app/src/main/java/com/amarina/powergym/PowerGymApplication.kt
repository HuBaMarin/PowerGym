package com.amarina.powergym

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.amarina.powergym.database.PowerGymDatabase
import com.amarina.powergym.repository.UserRepository
import com.amarina.powergym.ui.factory.admin.AdminExerciseViewModelFactory
import com.amarina.powergym.ui.factory.admin.AdminViewModelFactory
import com.amarina.powergym.ui.factory.auth.LoginViewModelFactory
import com.amarina.powergym.ui.factory.auth.RegisterViewModelFactory
import com.amarina.powergym.ui.factory.exercise.EjercicioDetailViewModelFactory
import com.amarina.powergym.ui.factory.main.MainViewModelFactory
import com.amarina.powergym.ui.factory.profile.ProfileViewModelFactory
import com.amarina.powergym.ui.factory.search.SearchViewModelFactory
import com.amarina.powergym.ui.factory.statistics.StatisticsViewModelFactory
import com.amarina.powergym.ui.viewmodel.statistics.StatisticsViewModel
import com.amarina.powergym.utils.LanguageHelper
import com.amarina.powergym.ui.search.SearchHistoryManager
import com.amarina.powergym.utils.SessionManager
import com.amarina.powergym.utils.crypto.AdminAuthManager
import com.amarina.powergym.utils.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Clase principal de la aplicación PowerGym.
 * Se encarga de la inicialización de componentes esenciales como la base de datos,
 * repositorios, manejadores de sesión y factories de ViewModels.
 */
class PowerGymApplication : Application() {
    /**
     * ViewModel compartido de estadísticas para uso en toda la aplicación
     */
    var statisticsViewModel: StatisticsViewModel? = null

    /**
     * Contexto de la aplicación almacenado para evitar problemas de dependencia en el constructor
     */
    private lateinit var appContext: Context

    /**
     * Sobrescribe el contexto base para establecer el contexto de la aplicación
     */
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        appContext = base
    }

    /**
     * Instancia de la base de datos inicializada de forma perezosa (lazy)
     */
    private val database by lazy { PowerGymDatabase.getInstance(appContext) }

    /**
     * Gestor del historial de búsquedas de ejercicios
     */
    private val searchHistoryManager by lazy {
        SearchHistoryManager(appContext)
    }

    /**
     * Factory para crear ViewModels de búsqueda de ejercicios
     */
    val searchViewModelFactory by lazy {
        SearchViewModelFactory(database.ejercicioDao(), searchHistoryManager)
    }

    /**
     * Repositorio de usuarios para gestionar la información de usuarios
     * Mantenido debido a la lógica de hash y seguridad que contiene
     */
    val userRepository by lazy { UserRepository(database.userDao(), appContext) }

    /**
     * Gestor de sesión para manejar la autenticación y sesión del usuario
     */
    val sessionManager by lazy { SessionManager(appContext) }

    /**
     * Gestor de autenticación de administrador
     */
    val adminAuthManager by lazy { AdminAuthManager(appContext) }

    /**
     * Gestor de preferencias de usuario
     */
    val preferenceManager by lazy { PreferenceManager(appContext) }

    /**
     * Factory para crear ViewModels de estadísticas
     * Ahora usando directamente DAOs en lugar de repositorios
     */
    val statisticsViewModelFactory by lazy {
        StatisticsViewModelFactory(
            database.estadisticaDao(),
            database.ejercicioDao(),
            sessionManager
        )
    }

    /**
     * Factory para crear ViewModels de inicio de sesión
     */
    val loginViewModelFactory by lazy {
        LoginViewModelFactory(userRepository, sessionManager, adminAuthManager)
    }

    /**
     * Factory para crear ViewModels de registro
     */
    val registerViewModelFactory by lazy {
        RegisterViewModelFactory(userRepository)
    }

    /**
     * Factory para crear ViewModels de la pantalla principal
     */
    val mainViewModelFactory by lazy {
        MainViewModelFactory(database.ejercicioDao())
    }

    /**
     * Factory para crear ViewModels de perfil de usuario
     */
    val profileViewModelFactory by lazy {
        ProfileViewModelFactory(userRepository, sessionManager)
    }

    /**
     * Factory para crear ViewModels de administración de ejercicios
     */
    val adminExerciseViewModelFactory by lazy {
        AdminExerciseViewModelFactory(database.ejercicioDao())
    }

    /**
     * Factory para crear ViewModels de administración de usuarios
     */
    val adminViewModelFactory by lazy {
        AdminViewModelFactory(
            database.userDao(),
            sessionManager
        )
    }

    /**
     * Factory para crear ViewModels de detalles de ejercicios
     */
    val ejercicioDetailViewModelFactory by lazy {
        EjercicioDetailViewModelFactory(
            database.ejercicioDao(),
            database.estadisticaDao(),
            sessionManager
        )
    }

    /**
     * Se ejecuta al crear la aplicación.
     * Inicializa componentes esenciales como el idioma, credenciales de administrador
     * y configuraciones de tema según las preferencias del usuario.
     */
    override fun onCreate() {
        super.onCreate()

        // Aplicar el idioma después de que el contexto está completamente inicializado
        val updatedContext = LanguageHelper.establecerIdioma(appContext)
        appContext = updatedContext

        // Ensure exercise translations are updated for the current language
        try {
            Log.d("PowerGymApplication", "Forcing exercise translations update at app start")
            PowerGymDatabase.updateExerciseTranslations(appContext)
        } catch (e: Exception) {
            Log.e("PowerGymApplication", "Failed to update exercise translations", e)
        }

        // Inicializar credenciales de administrador en segundo plano
        CoroutineScope(Dispatchers.IO).launch {
            adminAuthManager.initializeAdminCredentials()
        }

        // Establecer modo oscuro como predeterminado
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        // Cargar preferencias de tema del usuario si está autenticado
        CoroutineScope(Dispatchers.IO).launch {
            val userId = sessionManager.obtenerIdUsuario()
            if (userId != -1) {
                val preferences = preferenceManager.obtenerPreferenciasUsuario(userId)
                preferences?.let {
                    val mode = if (it.temaOscuro) {
                        AppCompatDelegate.MODE_NIGHT_YES
                    } else {
                        AppCompatDelegate.MODE_NIGHT_NO
                    }
                    AppCompatDelegate.setDefaultNightMode(mode)
                }
            }
        }
    }
}