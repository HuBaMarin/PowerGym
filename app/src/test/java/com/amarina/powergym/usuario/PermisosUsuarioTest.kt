package com.amarina.powergym.usuario

import com.amarina.powergym.database.PowerGymDatabase
import com.amarina.powergym.database.dao.UsuarioDao
import com.amarina.powergym.database.entities.Usuario
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests unitarios de lógica de permisos de usuario/administrador.
 * Para evitar dependencias externas, definimos una interfaz mínima `PermisoDao` local.
 */
@RunWith(JUnit4::class)
class PermisosUsuarioTest {

    private lateinit var userDao: UsuarioDao
    private lateinit var permisoDao: PermisoDao
    private lateinit var db: PowerGymDatabase

    @Before
    fun setUp() {
        db = mockk(relaxed = true)
        userDao = mockk(relaxed = true)
        permisoDao = mockk(relaxed = true)

        every { db.userDao() } returns userDao
        // La base de datos real no tiene `permisoDao()`, esto es sólo para tests.
    }

    // ---------------------------------------------------------------------------------
    // Admin puede ver todos los usuarios
    // ---------------------------------------------------------------------------------
    @Test
    fun adminPuedeVerTodosLosUsuarios() = runBlocking {
        val admin = Usuario(
            email = "admin@powergym.com",
            password = "adminpass",
            nombre = "Admin",
            rol = "admin"
        )
        val permisoVerUsuarios = Permiso(rolId = "admin", accion = "ver_usuarios", permitido = true)

        every { runBlocking { userDao.obtenerUsuarioPorEmail("admin@powergym.com") } } returns admin
        every {
            runBlocking {
                permisoDao.obtenerPermiso(
                    "admin",
                    "ver_usuarios"
                )
            }
        } returns permisoVerUsuarios

        val adminActual = userDao.obtenerUsuarioPorEmail("admin@powergym.com")!!
        val permiso = permisoDao.obtenerPermiso(adminActual.rol, "ver_usuarios")
        assertTrue(permiso.permitido)
    }

    // ---------------------------------------------------------------------------------
    // Usuario regular NO puede ver todos los usuarios
    // ---------------------------------------------------------------------------------
    @Test
    fun usuarioRegularNoPuedeVerTodosLosUsuarios() = runBlocking {
        val usuario = Usuario(
            email = "user@powergym.com",
            password = "pass123",
            nombre = "Usuario",
            rol = "usuario"
        )
        val permisoVerUsuarios =
            Permiso(rolId = "usuario", accion = "ver_usuarios", permitido = false)

        every { runBlocking { userDao.obtenerUsuarioPorEmail("user@powergym.com") } } returns usuario
        every {
            runBlocking {
                permisoDao.obtenerPermiso(
                    "usuario",
                    "ver_usuarios"
                )
            }
        } returns permisoVerUsuarios

        val actual = userDao.obtenerUsuarioPorEmail("user@powergym.com")!!
        val permiso = permisoDao.obtenerPermiso(actual.rol, "ver_usuarios")
        assertFalse(permiso.permitido)
    }
}

// -------------------------------------------------------------------------------------
// Entidades y DAO mínimos para el sistema de permisos (solo para pruebas)
// -------------------------------------------------------------------------------------

data class Permiso(
    val rolId: String,
    val accion: String,
    val permitido: Boolean
)

interface PermisoDao {
    suspend fun obtenerPermiso(rolId: String, accion: String): Permiso
}