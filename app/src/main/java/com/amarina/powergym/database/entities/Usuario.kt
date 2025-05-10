package com.amarina.powergym.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    /**
     * Identificador único del usuario, se genera automáticamente
     */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    /**
     * Correo electrónico del usuario, utilizado como identificador único para inicio de sesión
     */
    val email: String,
    /**
     * Contraseña del usuario (almacenada con hash)
     */
    val password: String,
    /**
     * Nombre completo del usuario
     */
    val nombre: String = "",
    /**
     * Rol del usuario en el sistema (usuario normal o administrador)
     */
    val rol: String = "usuario",
    /**
     * Indica si el usuario tiene habilitadas las notificaciones
     */
    val notificacionesHabilitadas: Boolean = true,
    /**
     * Fecha de registro del usuario en milisegundos desde epoch
     */
    val fechaRegistro: Long = System.currentTimeMillis(),
    /**
     * Token de un solo uso para recuperación de contraseña
     */
    val oneTimeToken: String? = null,
    /**
     * Indica si la cuenta está bloqueada debido a múltiples intentos fallidos
     */
    val accountLocked: Boolean = false,
    /**
     * Contador de intentos fallidos de inicio de sesión
     */
    val failedLoginAttempts: Int = 0
)