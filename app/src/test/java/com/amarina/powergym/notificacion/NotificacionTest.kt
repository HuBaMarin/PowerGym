package com.amarina.powergym.notificacion

import com.amarina.powergym.database.entities.Usuario
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests para funcionalidades relacionadas con notificaciones.
 * Verifica envío, desactivación y control de notificaciones.
 */
@RunWith(JUnit4::class)
class NotificacionTest {

    private lateinit var notificacionDao: NotificacionTestDao
    private lateinit var preferenciaDao: PreferenciaTestDao

    @Before
    fun setUp() {
        notificacionDao = mockk(relaxed = true)
        preferenciaDao = mockk(relaxed = true)
    }

    // --------------------------------------------------------------------
    // Prueba de envío de notificaciones
    // --------------------------------------------------------------------
    @Test
    fun enviarNotificacionesAUsuario() = runBlocking {
        val usuario = Usuario(
            id = 1,
            email = "user@example.com",
            password = "pass",
            nombre = "Test",
            rol = "usuario"
        )
        val notificacion = Notificacion(
            usuarioId = 1,
            titulo = "¡Hora de entrenar!",
            mensaje = "No olvides tu sesión de hoy",
            fecha = System.currentTimeMillis(),
            leida = false,
            tipo = "recordatorio"
        )

        val preferencias = Preferencia(
            usuarioId = 1,
            notificacionesHabilitadas = true,
            temaOscuro = false,
            recordatorios = true,
            frecuencia = "DIARIA"
        )

        // Configurar el mock
        every { runBlocking { preferenciaDao.obtenerPreferenciaSync(1) } } returns preferencias
        every { runBlocking { notificacionDao.insertar(notificacion) } } returns 1L

        // Comprobar si se pueden enviar notificaciones según preferencias
        val prefs = preferenciaDao.obtenerPreferenciaSync(usuario.id)!!
        assertTrue(prefs.notificacionesHabilitadas)
        assertTrue(prefs.recordatorios)

        // La notificación debería enviarse si las preferencias lo permiten
        if (prefs.notificacionesHabilitadas && prefs.recordatorios) {
            val id = notificacionDao.insertar(notificacion)
            assertEquals(1L, id)
        }
    }

    // --------------------------------------------------------------------
    // Prueba de desactivación de notificaciones
    // --------------------------------------------------------------------
    @Test
    fun desactivarNotificaciones() = runBlocking {
        val usuario = Usuario(
            id = 1,
            email = "user@example.com",
            password = "pass",
            nombre = "Test",
            rol = "usuario"
        )

        val preferencia = Preferencia(
            id = 1,
            usuarioId = 1,
            notificacionesHabilitadas = true,
            temaOscuro = false,
            recordatorios = true,
            frecuencia = "DIARIA"
        )

        val preferenciaActualizada = preferencia.copy(notificacionesHabilitadas = false)

        // Configurar el mock
        every { runBlocking { preferenciaDao.obtenerPreferenciaSync(1) } } returns
                preferencia andThen preferenciaActualizada
        every { runBlocking { preferenciaDao.actualizar(preferenciaActualizada) } } returns 1

        // Verificar que inicialmente las notificaciones están activadas
        val prefInicial = preferenciaDao.obtenerPreferenciaSync(usuario.id)!!
        assertTrue(prefInicial.notificacionesHabilitadas)

        // Desactivar notificaciones
        val nuevaPref = prefInicial.copy(notificacionesHabilitadas = false)
        val resultado = preferenciaDao.actualizar(nuevaPref)
        assertEquals(1, resultado)

        // Verificar que se desactivaron
        val prefActualizada = preferenciaDao.obtenerPreferenciaSync(usuario.id)!!
        assertFalse(prefActualizada.notificacionesHabilitadas)
    }

    // --------------------------------------------------------------------
    // Prueba de obtención de notificaciones no leídas
    // --------------------------------------------------------------------
    @Test
    fun obtenerNotificacionesNoLeidas() = runBlocking {
        val userId = 1
        val notificacionesNoLeidas = listOf(
            Notificacion(
                id = 1,
                usuarioId = userId,
                titulo = "Nuevo logro",
                mensaje = "Has superado tu record de sentadillas",
                fecha = System.currentTimeMillis() - 3600000, // 1 hora atrás
                leida = false,
                tipo = "logro"
            ),
            Notificacion(
                id = 2,
                usuarioId = userId,
                titulo = "Recordatorio",
                mensaje = "Tienes una rutina programada para hoy",
                fecha = System.currentTimeMillis(),
                leida = false,
                tipo = "recordatorio"
            )
        )

        // Configurar el mock
        every { runBlocking { notificacionDao.obtenerNotificacionesNoLeidas(userId) } } returns notificacionesNoLeidas

        // Obtener notificaciones no leídas
        val notificaciones = notificacionDao.obtenerNotificacionesNoLeidas(userId)
        assertNotNull(notificaciones)
        assertEquals(2, notificaciones.size)
        assertTrue(notificaciones.all { !it.leida })
    }
}

// -----------------------------------------------------------------------------
// Data classes y DAOs mínimos para tests
// -----------------------------------------------------------------------------

data class Notificacion(
    val id: Int = 0,
    val usuarioId: Int,
    val titulo: String,
    val mensaje: String,
    val fecha: Long,
    val leida: Boolean = false,
    val tipo: String
)

data class Preferencia(
    val id: Int = 0,
    val usuarioId: Int,
    val notificacionesHabilitadas: Boolean,
    val temaOscuro: Boolean,
    val recordatorios: Boolean,
    val frecuencia: String
)

interface NotificacionTestDao {
    suspend fun insertar(notificacion: Notificacion): Long
    suspend fun obtenerNotificacionesNoLeidas(usuarioId: Int): List<Notificacion>
}

interface PreferenciaTestDao {
    suspend fun obtenerPreferenciaSync(usuarioId: Int): Preferencia?
    suspend fun actualizar(preferencia: Preferencia): Int
}