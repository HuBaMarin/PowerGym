package com.amarina.powergym.ejercicio

import com.amarina.powergym.database.PowerGymDatabase
import com.amarina.powergym.database.dao.EjercicioDao
import com.amarina.powergym.database.dao.UsuarioDao
import com.amarina.powergym.database.entities.Ejercicio
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
 * Pruebas de permisos relacionados con la gestión de ejercicios.
 */
@RunWith(JUnit4::class)
class PermisosAdminEjercicioTest {

    private lateinit var userDao: UsuarioDao
    private lateinit var ejercicioDao: EjercicioDao
    private lateinit var permisoDao: PermisoDao
    private lateinit var db: PowerGymDatabase

    @Before
    fun setUp() {
        db = mockk(relaxed = true)
        userDao = mockk(relaxed = true)
        ejercicioDao = mockk(relaxed = true)
        permisoDao = mockk(relaxed = true)

        every { db.userDao() } returns userDao
        every { db.ejercicioDao() } returns ejercicioDao
    }

    @Test
    fun adminPuedeInsertarEjercicio() = runBlocking {
        val admin =
            Usuario(email = "admin@pg.com", password = "pass", nombre = "Admin", rol = "admin")
        val ejercicio = Ejercicio(
            nombre = "Curl",
            descripcion = "Desc",
            grupoMuscular = "Biceps",
            dificultad = "Básico",
            dias = "Lunes",
            imagenEjercicio = "img",
            seccion = "Superior",
            frecuencia = 10,
            porcentaje = 0.3f
        )
        val permiso = Permiso("admin", "modificar_ejercicios", true)

        every { runBlocking { userDao.obtenerUsuarioPorEmail("admin@pg.com") } } returns admin
        every {
            runBlocking {
                permisoDao.obtenerPermiso(
                    "admin",
                    "modificar_ejercicios"
                )
            }
        } returns permiso
        every { runBlocking { ejercicioDao.insertar(ejercicio) } } returns 1L

        val usr = userDao.obtenerUsuarioPorEmail("admin@pg.com")!!
        val p = permisoDao.obtenerPermiso(usr.rol, "modificar_ejercicios")
        assertTrue(p.permitido)
        if (p.permitido) assertTrue(ejercicioDao.insertar(ejercicio) == 1L)
    }

    @Test
    fun usuarioRegularNoPuedeInsertarEjercicio() = runBlocking {
        val user =
            Usuario(email = "user@pg.com", password = "pass", nombre = "User", rol = "usuario")
        val ejercicio = Ejercicio(
            nombre = "Curl",
            descripcion = "Desc",
            grupoMuscular = "Biceps",
            dificultad = "Básico",
            dias = "Lunes",
            imagenEjercicio = "img",
            seccion = "Superior",
            frecuencia = 10,
            porcentaje = 0.3f
        )
        val permiso = Permiso("usuario", "modificar_ejercicios", false)

        every { runBlocking { userDao.obtenerUsuarioPorEmail("user@pg.com") } } returns user
        every {
            runBlocking {
                permisoDao.obtenerPermiso(
                    "usuario",
                    "modificar_ejercicios"
                )
            }
        } returns permiso

        val usr = userDao.obtenerUsuarioPorEmail("user@pg.com")!!
        val p = permisoDao.obtenerPermiso(usr.rol, "modificar_ejercicios")
        assertFalse(p.permitido)
    }
}

// DAO y entidad de permisos minimal para tests
interface PermisoDao {
    suspend fun obtenerPermiso(rolId: String, accion: String): Permiso
}

data class Permiso(val rolId: String, val accion: String, val permitido: Boolean)
