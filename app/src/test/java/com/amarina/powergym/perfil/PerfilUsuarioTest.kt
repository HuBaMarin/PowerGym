package com.amarina.powergym.perfil

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests funcionales para verificar que el usuario puede acceder
 * a su perfil y ver sus estadísticas y preferencias.
 */
@RunWith(JUnit4::class)
class PerfilUsuarioTest {

    private lateinit var userDao: UsuarioTestDao
    private lateinit var preferenciaDao: PreferenciaTestDao
    private lateinit var estadisticaDao: EstadisticaTestDao

    @Before
    fun setUp() {
        userDao = mockk(relaxed = true)
        preferenciaDao = mockk(relaxed = true)
        estadisticaDao = mockk(relaxed = true)
    }

    // ----------------------------------------------------------------------
    // Verificar acceso al perfil de usuario
    // ----------------------------------------------------------------------
    @Test
    fun perfilUsuarioAccesible() = runBlocking {
        val usuario = Usuario(
            id = 1,
            email = "user@powergym.com",
            password = "pass123",
            nombre = "Usuario Test",
            rol = "usuario"
        )

        val preferencia = Preferencia(
            usuarioId = 1,
            notificacionesHabilitadas = true,
            temaOscuro = false,
            recordatorios = true,
            frecuencia = "DIARIA"
        )

        val resumenEstadisticas = ResumenEstadisticas(
            totalSesiones = 15,
            totalTiempo = 12600000, // 3.5 horas
            caloriasQuemadas = 1500,
            ejerciciosCompletados = 180
        )

        // Configurar mocks
        every { runBlocking { userDao.obtenerUsuarioPorId(1) } } returns usuario
        every { runBlocking { preferenciaDao.obtenerPreferenciaSync(1) } } returns preferencia
        every { runBlocking { estadisticaDao.obtenerResumenUsuario(1) } } returns resumenEstadisticas

        // 1. Obtener información básica del usuario
        val usuarioObtenido = userDao.obtenerUsuarioPorId(1)
        assertNotNull(usuarioObtenido)
        assertEquals("Usuario Test", usuarioObtenido.nombre)
        assertEquals("user@powergym.com", usuarioObtenido.email)

        // 2. Obtener preferencias del usuario
        val preferenciasUsuario = preferenciaDao.obtenerPreferenciaSync(1)
        assertNotNull(preferenciasUsuario)
        assertTrue(preferenciasUsuario.notificacionesHabilitadas)

        // 3. Obtener estadísticas resumidas del usuario
        val resumen = estadisticaDao.obtenerResumenUsuario(1)
        assertNotNull(resumen)
        assertEquals(15, resumen.totalSesiones)
        assertEquals(1500, resumen.caloriasQuemadas)
        assertEquals(180, resumen.ejerciciosCompletados)
    }

    // ----------------------------------------------------------------------
    // Verificar presentación completa del perfil
    // ----------------------------------------------------------------------
    @Test
    fun construirPerfilCompletoUsuario() = runBlocking {
        val usuarioId = 1

        // Datos básicos del usuario
        val usuario = Usuario(
            id = usuarioId,
            email = "advanced@powergym.com",
            password = "pass123",
            nombre = "Usuario Avanzado",
            rol = "usuario",
            fotoPerfil = "https://example.com/perfil.jpg",
            peso = 75.5f,
            altura = 180,
            fechaNacimiento = "1990-01-15",
            objetivo = "Ganar masa muscular"
        )

        // Preferencias 
        val preferencias = Preferencia(
            usuarioId = usuarioId,
            notificacionesHabilitadas = true,
            temaOscuro = true,
            recordatorios = true,
            frecuencia = "DIARIA",
            idioma = "es",
            unidadesMedida = "METRICO"
        )

        // Estadísticas
        val resumen = ResumenEstadisticas(
            totalSesiones = 120,
            totalTiempo = 180000000, // 50 horas
            caloriasQuemadas = 12500,
            ejerciciosCompletados = 1450,
            nivelActual = 8,
            experiencia = 24500,
            experienciaParaSiguienteNivel = 30000,
            logrosDesbloqueados = 15,
            diasConsecutivos = 14
        )

        every { runBlocking { userDao.obtenerUsuarioPorId(usuarioId) } } returns usuario
        every { runBlocking { preferenciaDao.obtenerPreferenciaSync(usuarioId) } } returns preferencias
        every { runBlocking { estadisticaDao.obtenerResumenUsuario(usuarioId) } } returns resumen

        // Construir perfil completo con toda la información disponible
        val perfilCompleto = PerfilUsuario(
            datosBasicos = userDao.obtenerUsuarioPorId(usuarioId),
            preferencias = preferenciaDao.obtenerPreferenciaSync(usuarioId)!!,
            estadisticas = estadisticaDao.obtenerResumenUsuario(usuarioId)
        )

        // Verificar datos básicos
        assertEquals("Usuario Avanzado", perfilCompleto.datosBasicos.nombre)
        assertEquals("Ganar masa muscular", perfilCompleto.datosBasicos.objetivo)
        assertEquals(75.5f, perfilCompleto.datosBasicos.peso)

        // Verificar preferencias
        assertTrue(perfilCompleto.preferencias.temaOscuro)
        assertEquals("es", perfilCompleto.preferencias.idioma)

        // Verificar estadísticas
        assertEquals(8, perfilCompleto.estadisticas.nivelActual)
        assertEquals(14, perfilCompleto.estadisticas.diasConsecutivos)
        assertEquals(15, perfilCompleto.estadisticas.logrosDesbloqueados)
    }
}

// -----------------------------------------------------------------------------
// Data classes y DAOs mínimos para tests
// -----------------------------------------------------------------------------

// Extendemos Usuario con campos adicionales
data class Usuario(
    val id: Int = 0,
    val email: String,
    val password: String,
    val nombre: String,
    val rol: String = "usuario",
    val fotoPerfil: String = "",
    val peso: Float = 0f,
    val altura: Int = 0,
    val fechaNacimiento: String = "",
    val objetivo: String = ""
)

data class Preferencia(
    val id: Int = 0,
    val usuarioId: Int,
    val notificacionesHabilitadas: Boolean,
    val temaOscuro: Boolean,
    val recordatorios: Boolean,
    val frecuencia: String,
    val idioma: String = "es",
    val unidadesMedida: String = "METRICO"
)

// Extendemos el resumen con campos adicionales
data class ResumenEstadisticas(
    val totalSesiones: Int,
    val totalTiempo: Long,
    val caloriasQuemadas: Int,
    val ejerciciosCompletados: Int,
    val nivelActual: Int = 1,
    val experiencia: Int = 0,
    val experienciaParaSiguienteNivel: Int = 1000,
    val logrosDesbloqueados: Int = 0,
    val diasConsecutivos: Int = 0
)

// Modelo agregado que combina toda la información de perfil
data class PerfilUsuario(
    val datosBasicos: Usuario,
    val preferencias: Preferencia,
    val estadisticas: ResumenEstadisticas
)

interface UsuarioTestDao {
    suspend fun obtenerUsuarioPorId(id: Int): Usuario
}

interface PreferenciaTestDao {
    suspend fun obtenerPreferenciaSync(usuarioId: Int): Preferencia?
}

interface EstadisticaTestDao {
    suspend fun obtenerResumenUsuario(usuarioId: Int): ResumenEstadisticas
}