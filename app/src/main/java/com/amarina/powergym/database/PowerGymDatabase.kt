package com.amarina.powergym.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.amarina.powergym.database.dao.EjercicioDao
import com.amarina.powergym.database.dao.EstadisticaDao
import com.amarina.powergym.database.dao.PreferenciaDao
import com.amarina.powergym.database.dao.UsuarioDao
import com.amarina.powergym.database.entities.Ejercicio
import com.amarina.powergym.database.entities.Estadistica
import com.amarina.powergym.database.entities.Preferencia
import com.amarina.powergym.database.entities.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Usuario::class,
        Ejercicio::class,
        Estadistica::class,
        Preferencia::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PowerGymDatabase : RoomDatabase() {
    abstract fun userDao(): UsuarioDao
    abstract fun ejercicioDao(): EjercicioDao
    abstract fun estadisticaDao(): EstadisticaDao
    abstract fun preferenciaDao(): PreferenciaDao

    companion object {
        @Volatile
        private var INSTANCE: PowerGymDatabase? = null

        fun getInstance(context: Context): PowerGymDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PowerGymDatabase::class.java,
                    "powergym.db"
                )
                    .addCallback(PowerGymDatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class PowerGymDatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database)
                }
            }
        }

        suspend fun populateDatabase(database: PowerGymDatabase) {
            // Insertar datos predefinidos

            // Admin user
            database.userDao().insert(
                Usuario(
                    email = "admin@powergym.com",
                    password = "admin123",
                    nombre = "Administrador",
                    rol = "admin"
                )
            )

            // Demo user
            val demoUserId = database.userDao().insert(
                Usuario(
                    email = "demo@powergym.com",
                    password = "demo123",
                    nombre = "Usuario Demo",
                    rol = "usuario"
                )
            ).toInt()

            // Insertar preferencias para el usuario demo
            database.preferenciaDao().insert(
                Preferencia(
                    usuarioId = demoUserId,
                    notificacionesHabilitadas = true,
                    temaDark = false,
                    recordatorios = true,
                    frecuencia = "DIARIA"
                )
            )

            // Ejercicios predefinidos
            val ejercicios = listOf(
                // Parte Superior - Hombros
                Ejercicio(
                    nombre = "Press militar",
                    descripcion = "Ejercicio para desarrollar los hombros, presionando peso hacia arriba.",
                    grupoMuscular = "Hombros",
                    dificultad = "Medio",
                    dias = "Lunes, Jueves",
                    urlEjercicio = "https://example.com/press_militar.jpg",
                    videoUrl = "https://youtube.com/watch?v=example1",
                    seccion = "Parte Superior",
                    calorias = 120
                ),
                Ejercicio(
                    nombre = "Elevaciones laterales",
                    descripcion = "Ejercicio para definir los deltoides laterales.",
                    grupoMuscular = "Hombros",
                    dificultad = "Básico",
                    dias = "Lunes, Jueves",
                    urlEjercicio = "https://example.com/elevaciones_laterales.jpg",
                    videoUrl = "https://youtube.com/watch?v=example2",
                    seccion = "Parte Superior",
                    calorias = 80
                ),

                // Parte Superior - Pecho
                Ejercicio(
                    nombre = "Press de banca",
                    descripcion = "Ejercicio básico para el desarrollo del pecho.",
                    grupoMuscular = "Pecho",
                    dificultad = "Medio",
                    dias = "Martes, Viernes",
                    urlEjercicio = "https://example.com/press_banca.jpg",
                    videoUrl = "https://youtube.com/watch?v=example3",
                    seccion = "Parte Superior",
                    calorias = 150
                ),
                Ejercicio(
                    nombre = "Aperturas con mancuernas",
                    descripcion = "Ejercicio de aislamiento para el pecho.",
                    grupoMuscular = "Pecho",
                    dificultad = "Básico",
                    dias = "Martes, Viernes",
                    urlEjercicio = "https://example.com/aperturas.jpg",
                    videoUrl = "https://youtube.com/watch?v=example4",
                    seccion = "Parte Superior",
                    calorias = 100
                ),

                // Parte Superior - Espalda
                Ejercicio(
                    nombre = "Dominadas",
                    descripcion = "Ejercicio compuesto para la espalda.",
                    grupoMuscular = "Espalda",
                    dificultad = "Avanzado",
                    dias = "Miércoles, Sábado",
                    urlEjercicio = "https://example.com/dominadas.jpg",
                    videoUrl = "https://youtube.com/watch?v=example5",
                    seccion = "Parte Superior",
                    calorias = 200
                ),
                Ejercicio(
                    nombre = "Remo con barra",
                    descripcion = "Ejercicio para el desarrollo de la espalda media.",
                    grupoMuscular = "Espalda",
                    dificultad = "Medio",
                    dias = "Miércoles, Sábado",
                    urlEjercicio = "https://example.com/remo_barra.jpg",
                    videoUrl = "https://youtube.com/watch?v=example6",
                    seccion = "Parte Superior",
                    calorias = 130
                ),

                // Parte Inferior - Piernas
                Ejercicio(
                    nombre = "Sentadillas",
                    descripcion = "Ejercicio básico para el desarrollo de piernas.",
                    grupoMuscular = "Piernas",
                    dificultad = "Medio",
                    dias = "Lunes, Jueves",
                    urlEjercicio = "https://example.com/sentadillas.jpg",
                    videoUrl = "https://youtube.com/watch?v=example7",
                    seccion = "Parte Inferior",
                    calorias = 180
                ),
                Ejercicio(
                    nombre = "Extensiones de pierna",
                    descripcion = "Ejercicio de aislamiento para cuádriceps.",
                    grupoMuscular = "Piernas",
                    dificultad = "Básico",
                    dias = "Lunes, Jueves",
                    urlEjercicio = "https://example.com/extensiones_pierna.jpg",
                    videoUrl = "https://youtube.com/watch?v=example8",
                    seccion = "Parte Inferior",
                    calorias = 90
                ),

                // Parte Inferior - Glúteos
                Ejercicio(
                    nombre = "Hip Thrust",
                    descripcion = "Ejercicio efectivo para el desarrollo de glúteos.",
                    grupoMuscular = "Glúteos",
                    dificultad = "Medio",
                    dias = "Martes, Viernes",
                    urlEjercicio = "https://example.com/hip_thrust.jpg",
                    videoUrl = "https://youtube.com/watch?v=example9",
                    seccion = "Parte Inferior",
                    calorias = 140
                ),
                Ejercicio(
                    nombre = "Puente de glúteos",
                    descripcion = "Ejercicio básico para activar los glúteos.",
                    grupoMuscular = "Glúteos",
                    dificultad = "Básico",
                    dias = "Martes, Viernes",
                    urlEjercicio = "https://example.com/puente_gluteos.jpg",
                    videoUrl = "https://youtube.com/watch?v=example10",
                    seccion = "Parte Inferior",
                    calorias = 60
                ),

                // Cardio
                Ejercicio(
                    nombre = "Burpees",
                    descripcion = "Ejercicio de alta intensidad para quemar calorías.",
                    grupoMuscular = "Full Body",
                    dificultad = "Avanzado",
                    dias = "Miércoles, Sábado, Domingo",
                    urlEjercicio = "https://example.com/burpees.jpg",
                    videoUrl = "https://youtube.com/watch?v=example11",
                    seccion = "Cardio",
                    calorias = 250
                ),
                Ejercicio(
                    nombre = "Salto a la comba",
                    descripcion = "Ejercicio cardiovascular con cuerda de saltar.",
                    grupoMuscular = "Full Body",
                    dificultad = "Medio",
                    dias = "Miércoles, Sábado, Domingo",
                    urlEjercicio = "https://example.com/salto_comba.jpg",
                    videoUrl = "https://youtube.com/watch?v=example12",
                    seccion = "Cardio",
                    calorias = 220
                )
            )

            database.ejercicioDao().insertAll(ejercicios)

            // Estadísticas demo
            database.estadisticaDao().insert(
                Estadistica(
                    usuarioId = demoUserId,
                    fecha = System.currentTimeMillis() - (86400000 * 7), // 7 días atrás
                    ejerciciosCompletados = 5,
                    tiempoTotal = 1800, // 30 minutos
                    caloriasQuemadas = 300
                )
            )

            database.estadisticaDao().insert(
                Estadistica(
                    usuarioId = demoUserId,
                    fecha = System.currentTimeMillis() - (86400000 * 5), // 5 días atrás
                    ejerciciosCompletados = 8,
                    tiempoTotal = 2700, // 45 minutos
                    caloriasQuemadas = 450
                )
            )

            database.estadisticaDao().insert(
                Estadistica(
                    usuarioId = demoUserId,
                    fecha = System.currentTimeMillis() - (86400000 * 2), // 2 días atrás
                    ejerciciosCompletados = 6,
                    tiempoTotal = 3600, // 60 minutos
                    caloriasQuemadas = 500
                )
            )
        }
    }
}
