package com.amarina.powergym.database.dao

import androidx.room.*
import com.amarina.powergym.database.entities.Ejercicio

/**
 * Objeto de Acceso a Datos (DAO) para la entidad Ejercicio.
 * Proporciona métodos para realizar operaciones CRUD en la tabla ejercicios.
 */
@Dao
interface EjercicioDao {

    @Insert
    suspend fun insertar(ejercicio: Ejercicio): Long

    /**
     * Inserta una lista de ejercicios en la base de datos.
     *
     * @param ejercicios Lista de ejercicios a insertar
     */
    @Insert
    suspend fun insertarTodos(ejercicios: List<Ejercicio>)


    @Delete
    suspend fun eliminar(ejercicio: Ejercicio)


    @Update
    suspend fun actualizar(ejercicio: Ejercicio)

    /**
     * Obtiene todos los ejercicios de la base de datos.
     *
     * @return Lista con todos los ejercicios
     */
    @Query("SELECT * FROM ejercicios")
    suspend fun obtenerTodosEjercicios(): List<Ejercicio>

    /**
     * Obtiene todas las secciones únicas disponibles.
     *
     * @return Lista de nombres de secciones
     */
    @Query("SELECT DISTINCT seccion FROM ejercicios")
    suspend fun obtenerTodasSecciones(): List<String>

    /**
     * Obtiene todos los grupos musculares únicos disponibles.
     *
     * @return Lista de nombres de grupos musculares
     */
    @Query("SELECT DISTINCT grupoMuscular FROM ejercicios")
    suspend fun obtenerTodosGruposMusculares(): List<String>

    /**
     * Obtiene un ejercicio específico según su ID.
     *
     * @param id El ID del ejercicio a buscar
     * @return El ejercicio encontrado o null si no existe
     */
    @Query("SELECT * FROM ejercicios WHERE id = :id")
    suspend fun obtenerEjercicioPorId(id: Int): Ejercicio?

    /**
     * Obtiene un ejercicio por su nombre exacto.
     *
     * @param nombre Nombre del ejercicio a buscar
     * @return El ejercicio encontrado o null si no existe
     */
    @Query("SELECT * FROM ejercicios WHERE nombre = :nombre LIMIT 1")
    suspend fun obtenerEjercicioPorNombre(nombre: String): Ejercicio?

    /**
     * Obtiene ejercicios filtrados según varios criterios.
     * Es utilizado tanto en producción como en pruebas para
     * obtener ejercicios.
     *
     * @param dias Filtro por días
     * @param dificultad Filtro por nivel de dificultad
     * @param grupoMuscular Filtro por grupo muscular
     * @param query Texto de búsqueda para nombre o descripción
     * @return Lista de ejercicios con los filtros aplicados
     */
    @Query("""
        SELECT * FROM ejercicios 
        WHERE (:dias IS NULL OR dias LIKE '%' || :dias || '%')
        AND (:dificultad IS NULL OR dificultad = :dificultad)
        AND (:grupoMuscular IS NULL OR grupoMuscular = :grupoMuscular)
        AND (:query = '' OR nombre LIKE '%' || :query || '%' OR descripcion LIKE '%' || :query || '%')
        ORDER BY nombre ASC
    """)
    suspend fun obtenerEjerciciosFiltrados(
        dias: String? = null,
        dificultad: String? = null,
        grupoMuscular: String? = null,
        query: String = ""
    ): List<Ejercicio>

    /**
     * Obtiene todos los valores únicos de dificultad.
     *
     * @return Lista de niveles de dificultad disponibles
     */
    @Query("SELECT DISTINCT dificultad FROM ejercicios")
    suspend fun obtenerValoresDificultad(): List<String>

    /**
     * Actualiza la traducción (nombre y descripción) de un ejercicio.
     *
     * @param id ID del ejercicio a actualizar
     * @param nombre Nuevo nombre traducido
     * @param descripcion Nueva descripción traducida
     */
    @Query("UPDATE ejercicios SET nombre = :nombre, descripcion = :descripcion WHERE id = :id")
    suspend fun actualizarTraduccion(id: Int, nombre: String, descripcion: String)

    /**
     * Actualiza la traducción del grupo muscular de un ejercicio.
     *
     * @param id ID del ejercicio a actualizar
     * @param grupoMuscular Nuevo grupo muscular traducido
     */
    @Query("UPDATE ejercicios SET grupoMuscular = :grupoMuscular WHERE id = :id")
    suspend fun actualizarGrupoMuscular(id: Int, grupoMuscular: String)
}