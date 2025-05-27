package com.amarina.powergym.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.amarina.powergym.R
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
import java.util.Locale

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
                    .addCallback(PowerGymDatabaseCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Actualiza los nombres y descripciones de los ejercicios al idioma actual
          */
        fun updateExerciseTranslations(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = getInstance(context)
                    val resources = context.resources
                    Log.d(
                        "PowerGymDatabase",
                        "Updating exercises to locale: ${Locale.getDefault().language}"
                    )

                    // Obtener todos los ejercicios
                    val allExercises = database.ejercicioDao().obtenerTodosEjercicios()

                    // Actualizar cada ejercicio con el nombre y la descripción traducidos
                    for (exercise in allExercises) {
                        try {
                            // Mapeo directo entre claves de ejercicio e IDs de recursos
                            val (nameResId, descResId) = getResourceIdsForExercise(exercise.nombre)

                            if (nameResId != 0 && descResId != 0) {
                                // Obtener cadenas traducidas directamente usando IDs de recursos
                                val translatedName = resources.getString(nameResId)
                                val translatedDesc = resources.getString(descResId)

                                Log.d(
                                    "PowerGymDatabase",
                                    "Updating '${exercise.nombre}' to '$translatedName'"
                                )

                                // Usar actualizar traducción de EjercicioDao
                                database.ejercicioDao().actualizarTraduccion(
                                    exercise.id,
                                    translatedName,
                                    translatedDesc
                                )
                                // Actualizar cualquier estadística que haga referencia a este ejercicio
                                database.estadisticaDao().actualizarNombreEjercicio(
                                    exercise.id,
                                    translatedName
                                )
                            } else {
                                Log.e(
                                    "PowerGymDatabase",
                                    "Invalid resource IDs for exercise: ${exercise.nombre}"
                                )
                            }

                            // También actualizar la traducción del grupo muscular
                            val muscleGroupResId =
                                getResourceIdForMuscleGroup(exercise.grupoMuscular)
                            if (muscleGroupResId != 0) {
                                val translatedMuscleGroup = resources.getString(muscleGroupResId)

                                Log.d(
                                    "PowerGymDatabase",
                                    "Updating muscle group '${exercise.grupoMuscular}' to '$translatedMuscleGroup'"
                                )

                                // Actualizar grupo muscular en el ejercicio
                                database.ejercicioDao().actualizarGrupoMuscular(
                                    exercise.id,
                                    translatedMuscleGroup
                                )

                                // Actualizar grupo muscular en estadística
                                database.estadisticaDao().actualizarGrupoMuscular(
                                    exercise.id,
                                    translatedMuscleGroup
                                )
                            }

                            // También actualizar la dificultad
                            val difficultyResId = getDifficultyResourceId(exercise.dificultad)
                            if (difficultyResId != 0) {
                                val translatedDifficulty = resources.getString(difficultyResId)

                                Log.d(
                                    "PowerGymDatabase",
                                    "Updating difficulty '${exercise.dificultad}' to '$translatedDifficulty'"
                                )

                                // Actualizar dificultad en el ejercicio
                                database.ejercicioDao().actualizarDificultad(
                                    exercise.id,
                                    translatedDifficulty
                                )
                            }

                            // También actualizar los días
                            val translatedDays = translateDays(exercise.dias, resources)

                            Log.d(
                                "PowerGymDatabase",
                                "Updating days '${exercise.dias}' to '$translatedDays'"
                            )

                            // Actualizar días en el ejercicio
                            database.ejercicioDao().actualizarDias(
                                exercise.id,
                                translatedDays
                            )
                        } catch (e: Exception) {
                            Log.e(
                                "PowerGymDatabase",
                                "Error updating exercise ${exercise.id}: ${e.message}"
                            )
                        }
                    }

                    Log.d("PowerGymDatabase", "Exercise translations updated successfully")
                } catch (e: Exception) {
                    Log.e("PowerGymDatabase", "Error updating translations: ${e.message}")
                }
            }
        }
        
        /**
         * Obtener IDs de recursos para un nombre de ejercicio sin usar reflexión de recursos
         * Devuelve un Par de (idRecursoNombre, idRecursoDescripción)
         */
        private fun getResourceIdsForExercise(exerciseName: String): Pair<Int, Int> {
            // Crear una versión simplificada en minúsculas del nombre para comparación
            val simpleName = exerciseName.lowercase().trim()

            return when {
                // Press Militar
                simpleName.contains("press militar",true) ->
                    Pair(
                        R.string.exercise_military_press_name,
                        R.string.exercise_military_press_desc
                    )

                // Elevaciones laterales
                simpleName.contains("elevaciones laterales",true) ->
                    Pair(
                        R.string.exercise_lateral_raises_name,
                        R.string.exercise_lateral_raises_desc
                    )

                // Press de banca
                simpleName.contains("press de banca")  ->
                    Pair(
                        R.string.exercise_press_de_banca_name,
                        R.string.exercise_press_de_banca_desc
                    )

                // Pecho fly / Aperturas
                simpleName.contains("aperturas") ->
                    Pair(
                        R.string.exercise_aperturas_con_mancuernas_name,
                        R.string.exercise_aperturas_con_mancuernas_desc
                    )

                // Pull-ups / Dominadas
                simpleName.contains("dominadas",true) ->
                    Pair(R.string.exercise_dominadas_name, R.string.exercise_dominadas_desc)

                // Remo con barra
                simpleName.contains("remo con barra",true) ->
                    Pair(
                        R.string.exercise_remo_con_barra_name,
                        R.string.exercise_remo_con_barra_desc
                    )

                // Sentadillas
                simpleName.contains("sentadillas",true)  ->
                    Pair(R.string.exercise_sentadillas_name, R.string.exercise_sentadillas_desc)

                // Extensiones de pierna
                simpleName.contains("extensiones de pierna",true) ->
                    Pair(
                        R.string.exercise_extensiones_de_pierna_name,
                        R.string.exercise_extensiones_de_pierna_desc
                    )

                // Empuje de cadera
                simpleName.contains("hip thrust",true)->
                    Pair(R.string.exercise_hip_thrust_name, R.string.exercise_hip_thrust_desc)

                // Puente de glúteos
                simpleName.contains("glute bridge",true) ->
                    Pair(
                        R.string.exercise_puente_de_gluteos_name,
                        R.string.exercise_puente_de_gluteos_desc
                    )

                // Burpees
                simpleName.contains("burpees",true)->
                    Pair(R.string.exercise_burpees_name, R.string.exercise_burpees_desc)

                // Salto a la comba
                simpleName.contains("salto a la comba",true)  ->
                    Pair(
                        R.string.exercise_salto_a_la_comba_name,
                        R.string.exercise_salto_a_la_comba_desc
                    )

                // Estiramiento de rodilla
                simpleName.contains("knee stretch",true) ->
                    Pair(R.string.exercise_knee_stretch_name, R.string.exercise_knee_stretch_desc)

                // Extensiones de brazo sentado
                simpleName.contains("seated arm extension")  ->
                    Pair(
                        R.string.exercise_seated_arm_extensions_name,
                        R.string.exercise_seated_arm_extensions_desc
                    )

                // Ejercicios varios
                simpleName.contains("varios",true)->
                    Pair(
                        R.string.exercise_various_exercises_name,
                        R.string.exercise_various_exercises_desc
                    )

                // Banda elástica
                simpleName.contains("ejercicios con banda",true) ->
                    Pair(
                        R.string.exercise_resistance_band_name,
                        R.string.exercise_resistance_band_desc
                    )

                // Elevaciones laterales de brazos sentado
                simpleName.contains("elevacion lat",true) ->
                    Pair(
                        R.string.exercise_elevacion_lateral_de_brazos_sentado_name,
                        R.string.exercise_elevacion_lateral_de_brazos_sentado_desc
                    )

                // Ejercicios respiratorios
                simpleName.contains("respiratorios",true)  ->
                    Pair(
                        R.string.exercise_respiratory_exercises_name,
                        R.string.exercise_respiratory_exercises_desc
                    )

                // Flexiones de muñeca

                        simpleName.contains("flexiones de muñeca",true)->
                    Pair(
                        R.string.exercise_flexiones_de_muneca_name,
                        R.string.exercise_flexiones_de_muneca_desc
                    )

                // Ejercicios de equilibrio
                simpleName.contains("equilibrio",true) ->
                    Pair(
                        R.string.exercise_ejercicios_de_equilibrio_con_apoyo_name,
                        R.string.exercise_ejercicios_de_equilibrio_con_apoyo_desc
                    )

                // Estiramientos suaves
                simpleName.contains("estiramientos",true)  ->
                    Pair(
                        R.string.exercise_estiramientos_suaves_name,
                        R.string.exercise_estiramientos_suaves_desc
                    )

                // Yoga en silla
                simpleName.contains("yoga",true) ->
                    Pair(
                        R.string.exercise_yoga_adaptado_en_silla_name,
                        R.string.exercise_yoga_adaptado_en_silla_desc
                    )

                // Remo con polea
                simpleName.contains("cable row") ||
                        simpleName.contains("remo con polea") ->
                    Pair(
                        R.string.exercise_cable_row_name,
                        R.string.exercise_cable_row_desc
                    )

                // Si no se encuentra coincidencia, devolver valores 0 para indicar que no hay coincidencia
                else -> {
                    Pair(0, 0)
                }
            }
        }



        /**
         * Devuelve el recurso de string que representa el grupo muscular recibido.
         *
         * El texto puede llegar en diferentes idiomas o con pequeñas variaciones
         * (plural, acentos, mayúsculas…). Comprobamos
         * de forma explícita todas las traducciones y sinónimos más habituales
         * usando contains() en minúsculas.
         */
        private fun getResourceIdForMuscleGroup(muscleGroup: String): Int {
            val name = muscleGroup.lowercase().trim()

            return when {
                // Demo
                name.contains("demo") -> R.string.muscle_group_demo

                // Caso especial «core + legs»
                (name.contains("core") && (name.contains("leg") || name.contains("pierna"))) ||
                        name.contains("core y piernas") ||
                        name.contains("core and legs") -> R.string.muscle_group_core_legs

                // Piernas
                name.contains("leg") || name.contains("pierna") ||
                        name.contains("beine") || name.contains("jambe") ||
                        name.contains("脚") -> R.string.muscle_group_legs

                // Brazos
                name.contains("arm") || name.contains("brazo") ||
                        name.contains("arme") || name.contains("腕") -> R.string.muscle_group_arms

                // Core
                name.contains("core") || name.contains("rumpf") ||
                        name.contains("noyau") || name.contains("núcleo") ||
                        name.contains("コア") -> R.string.muscle_group_core

                // Hombros
                name.contains("shoulder") || name.contains("hombro") ||
                        name.contains("schulter") || name.contains("épaule") ||
                        name.contains("肩") -> R.string.muscle_group_shoulders

                // Respiratorio
                name.contains("respiratory") || name.contains("respirator") ||
                        name.contains("breathing") || name.contains("atmung") ||
                        name.contains("呼吸") -> R.string.muscle_group_respiratory

                // Antebrazos
                name.contains("forearm") || name.contains("antebrazo") ||
                        name.contains("unterarm") || name.contains("avant-bras") ||
                        name.contains("前腕") -> R.string.muscle_group_forearms

                // Pecho
                name.contains("chest") || name.contains("pecho") ||
                        name.contains("brust") || name.contains("poitrine") ||
                        name.contains("胸") -> R.string.muscle_group_chest

                // Espalda
                name.contains("back") || name.contains("espalda") ||
                        name.contains("rücken") || name.contains("dos") ||
                        name.contains("背") -> R.string.muscle_group_back

                // Glúteos
                name.contains("glute") || name.contains("glúteo") ||
                        name.contains("gesäß") || name.contains("fessier") ||
                        name.contains("臀") -> R.string.muscle_group_glutes

                // Cuerpo completo
                name.contains("full body") || name.contains("whole body") ||
                        name.contains("cuerpo completo") || name.contains("ganzkörper") ||
                        name.contains("corps entier") || name.contains("全身") -> R.string.muscle_group_full_body

                // Múltiple
                name.contains("multiple") || name.contains("múltiple") ||
                        name.contains("mehrere") || name.contains("複数") -> R.string.muscle_group_multiple

                // Valor predeterminado
                else -> R.string.muscle_group_multiple
            }
        }

        /**
         * Obtiene el ID del recurso para una dificultad
         */
        private fun getDifficultyResourceId(difficulty: String): Int {
            val simpleName = difficulty.lowercase().trim()

            return when {
                simpleName.contains("basic") ||
                        simpleName.contains("básico") ||
                        simpleName.contains("basico") ||
                        simpleName.contains("débutant") ||
                        simpleName.contains("anfänger") ||
                        simpleName.contains("初級") -> R.string.difficulty_basic

                simpleName.contains("intermediate") ||
                        simpleName.contains("intermedio") ||
                        simpleName.contains("intermédiaire") ||
                        simpleName.contains("mittelstufe") ||
                        simpleName.contains("中級") -> R.string.difficulty_intermediate

                simpleName.contains("advanced") ||
                        simpleName.contains("avanzado") ||
                        simpleName.contains("avancé") ||
                        simpleName.contains("fortgeschritten") ||
                        simpleName.contains("上級") -> R.string.difficulty_advanced

                simpleName.contains("adaptable") ||
                        simpleName.contains("anpassbar") -> R.string.difficulty_adaptable

                else -> 0
            }
        }

        /**
         * Traduce los días de entrenamiento al idioma actual
         */
        private fun translateDays(days: String, resources: android.content.res.Resources): String {
            if (days.isEmpty()) return days

            return days.split(",").map { day ->
                val trimmedDay = day.trim()
                translateSingleDay(trimmedDay, resources)
            }.joinToString(", ")
        }

        /**
         * Traduce un día individual
         */
        private fun translateSingleDay(
            day: String,
            resources: android.content.res.Resources
        ): String {
            val simpleName = day.lowercase().trim()

            // First check for exact matches with existing translated strings
            try {
                if (day == resources.getString(R.string.days_all)) {
                    return resources.getString(R.string.days_all)
                }
                if (day == resources.getString(R.string.days_monday_wednesday_friday)) {
                    return resources.getString(R.string.days_monday_wednesday_friday)
                }
                if (day == resources.getString(R.string.days_monday_thursday)) {
                    return resources.getString(R.string.days_monday_thursday)
                }
                if (day == resources.getString(R.string.days_tuesday_friday)) {
                    return resources.getString(R.string.days_tuesday_friday)
                }
                if (day == resources.getString(R.string.days_tuesday_thursday)) {
                    return resources.getString(R.string.days_tuesday_thursday)
                }
                if (day == resources.getString(R.string.days_wednesday_saturday)) {
                    return resources.getString(R.string.days_wednesday_saturday)
                }
                if (day == resources.getString(R.string.days_wednesday_saturday_sunday)) {
                    return resources.getString(R.string.days_wednesday_saturday_sunday)
                }
                if (day == resources.getString(R.string.days_tuesday_thursday_saturday)) {
                    return resources.getString(R.string.days_tuesday_thursday_saturday)
                }
                if (day == resources.getString(R.string.days_all_week)) {
                    return resources.getString(R.string.days_all_week)
                }
            } catch (e: Exception) {
                // Continue with pattern matching if exact match fails
            }

            // Pattern matching for individual days and common combinations
            return when {
                // Individual days
                simpleName.contains("monday") || simpleName.contains("lunes") ||
                        simpleName.contains("montag") || simpleName.contains("lundi") ||
                        simpleName.contains("月曜") -> resources.getString(R.string.day_monday)

                simpleName.contains("tuesday") || simpleName.contains("martes") ||
                        simpleName.contains("dienstag") || simpleName.contains("mardi") ||
                        simpleName.contains("火曜") -> resources.getString(R.string.day_tuesday)

                simpleName.contains("wednesday") || simpleName.contains("miércoles") ||
                        simpleName.contains("miercoles") || simpleName.contains("mittwoch") ||
                        simpleName.contains("mercredi") || simpleName.contains("水曜") -> resources.getString(
                    R.string.day_wednesday
                )

                simpleName.contains("thursday") || simpleName.contains("jueves") ||
                        simpleName.contains("donnerstag") || simpleName.contains("jeudi") ||
                        simpleName.contains("木曜") -> resources.getString(R.string.day_thursday)

                simpleName.contains("friday") || simpleName.contains("viernes") ||
                        simpleName.contains("freitag") || simpleName.contains("vendredi") ||
                        simpleName.contains("金曜") -> resources.getString(R.string.day_friday)

                simpleName.contains("saturday") || simpleName.contains("sábado") ||
                        simpleName.contains("sabado") || simpleName.contains("samstag") ||
                        simpleName.contains("samedi") || simpleName.contains("土曜") -> resources.getString(
                    R.string.day_saturday
                )

                simpleName.contains("sunday") || simpleName.contains("domingo") ||
                        simpleName.contains("sonntag") || simpleName.contains("dimanche") ||
                        simpleName.contains("日曜") -> resources.getString(R.string.day_sunday)

                // Complex day combinations (check for patterns in original string)
                (simpleName.contains("monday") && simpleName.contains("wednesday") && simpleName.contains(
                    "friday"
                )) ||
                        (simpleName.contains("lunes") && simpleName.contains("miércoles") && simpleName.contains(
                            "viernes"
                        )) ||
                        (simpleName.contains("montag") && simpleName.contains("mittwoch") && simpleName.contains(
                            "freitag"
                        )) ||
                        (simpleName.contains("lundi") && simpleName.contains("mercredi") && simpleName.contains(
                            "vendredi"
                        )) ->
                    resources.getString(R.string.days_monday_wednesday_friday)

                (simpleName.contains("tuesday") && simpleName.contains("thursday") && !simpleName.contains(
                    "saturday"
                )) ||
                        (simpleName.contains("martes") && simpleName.contains("jueves") && !simpleName.contains(
                            "sábado"
                        )) ->
                    resources.getString(R.string.days_tuesday_thursday)

                (simpleName.contains("monday") && simpleName.contains("thursday") && !simpleName.contains(
                    "tuesday"
                )) ||
                        (simpleName.contains("lunes") && simpleName.contains("jueves") && !simpleName.contains(
                            "martes"
                        )) ->
                    resources.getString(R.string.days_monday_thursday)

                (simpleName.contains("tuesday") && simpleName.contains("friday") && !simpleName.contains(
                    "thursday"
                )) ||
                        (simpleName.contains("martes") && simpleName.contains("viernes") && !simpleName.contains(
                            "jueves"
                        )) ->
                    resources.getString(R.string.days_tuesday_friday)

                (simpleName.contains("wednesday") && simpleName.contains("saturday") && !simpleName.contains(
                    "sunday"
                )) ||
                        (simpleName.contains("miércoles") && simpleName.contains("sábado") && !simpleName.contains(
                            "domingo"
                        )) ->
                    resources.getString(R.string.days_wednesday_saturday)

                (simpleName.contains("tuesday") && simpleName.contains("thursday") && simpleName.contains(
                    "saturday"
                )) ||
                        (simpleName.contains("martes") && simpleName.contains("jueves") && simpleName.contains(
                            "sábado"
                        )) ->
                    resources.getString(R.string.days_tuesday_thursday_saturday)

                (simpleName.contains("wednesday") && simpleName.contains("saturday") && simpleName.contains(
                    "sunday"
                )) ||
                        (simpleName.contains("miércoles") && simpleName.contains("sábado") && simpleName.contains(
                            "domingo"
                        )) ->
                    resources.getString(R.string.days_wednesday_saturday_sunday)

                // All days patterns
                simpleName.contains("all days") || simpleName.contains("todos los días") ||
                        simpleName.contains("alle tage") || simpleName.contains("tous les jours") ||
                        simpleName.contains("毎日") -> resources.getString(R.string.days_all)

                // Return original if no translation found
                else -> day
            }
        }
    }

    private class PowerGymDatabaseCallback(private val context: Context) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database, context)
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            // Actualizar las traducciones de ejercicios
            // con el contexto de idioma actual
            updateExerciseTranslations(context)
        }

        suspend fun populateDatabase(database: PowerGymDatabase, context: Context) {
            val resources = context.resources


            // Usuario de prueba
            val demoUserId = database.userDao().insertar(
                Usuario(
                    email = "a@a.com",
                    password = "demo123",
                    nombre = "Usuario Demo",
                    rol = "usuario"
                )
            ).toInt()

            val cableRowId = database.ejercicioDao().insertar(
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_cable_row_name),
                    descripcion = resources.getString(R.string.exercise_cable_row_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_back),
                    dificultad = resources.getString(R.string.difficulty_intermediate),
                    dias = resources.getString(R.string.days_tuesday_friday),
                    imagenEjercicio = "https://training.fit/wp-content/uploads/2020/02/rudern-kabelzug-800x448.png",
                    videoUrl = "https://www.youtube.com/watch?v=GZbfZ033f74",
                    seccion = resources.getString(R.string.section_upper_body),
                    calorias = 110,
                    frecuencia = 15,
                    porcentaje = 0.35f
                )
            ).toInt()

            database.preferenciaDao().insertar(
                Preferencia(
                    usuarioId = demoUserId,
                    notificacionesHabilitadas = true,
                    temaOscuro = true,
                    recordatorios = true,
                    frecuencia = "DIARIA"
                )
            )
            val ejerciciosAdaptados = listOf(
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_knee_stretch_name),
                    descripcion = resources.getString(R.string.exercise_knee_stretch_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_legs),
                    dificultad = resources.getString(R.string.difficulty_basic),
                    dias = resources.getString(R.string.days_monday_wednesday_friday),
                    imagenEjercicio = "https://img.youtube.com/vi/rfMWJcWXhZo/maxresdefault.jpg",
                    videoUrl = "https://www.youtube.com/watch?v=rfMWJcWXhZo",
                    seccion = resources.getString(R.string.section_elderly),
                    calorias = 40,
                    frecuencia = 8,
                    porcentaje = 0.2f
                ),

                Ejercicio(
                    nombre = resources.getString(R.string.exercise_various_exercises_name),
                    descripcion = resources.getString(R.string.exercise_various_exercises_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_core),
                    dificultad = resources.getString(R.string.difficulty_basic),
                    dias = resources.getString(R.string.days_monday_wednesday_friday),
                    imagenEjercicio = "https://img.youtube.com/vi/DYOTthcWj8g/maxresdefault.jpg",
                    videoUrl = "https://youtu.be/DYOTthcWj8g",
                    seccion = resources.getString(R.string.section_elderly),
                    calorias = 30,
                    frecuencia = 12,
                    porcentaje = 0.1f
                ),
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_resistance_band_name),
                    descripcion = resources.getString(R.string.exercise_resistance_band_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_multiple),
                    dificultad = resources.getString(R.string.difficulty_adaptable),
                    dias = resources.getString(R.string.days_monday_wednesday_friday),
                    imagenEjercicio = "https://img.youtube.com/vi/YPILISq2SW8/maxresdefault.jpg",
                    videoUrl = "https://www.youtube.com/watch?v=YPILISq2SW8",
                    seccion = resources.getString(R.string.section_reduced_mobility),
                    calorias = 50,
                    frecuencia = 15,
                    porcentaje = 0.25f
                ),

                Ejercicio(
                    nombre = resources.getString(R.string.exercise_respiratory_exercises_name),
                    descripcion = resources.getString(R.string.exercise_respiratory_exercises_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_respiratory),
                    dificultad = resources.getString(R.string.difficulty_basic),
                    dias = resources.getString(R.string.days_all),
                    imagenEjercicio = "https://static.thenounproject.com/png/4372426-200.png",
                    videoUrl = "https://www.youtube.com/watch?v=rqGS9UvXrJU",
                    seccion = resources.getString(R.string.section_reduced_mobility),
                    calorias = 20,
                    frecuencia = 20,
                    porcentaje = 0.1f
                ),
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_flexiones_de_muneca_name),
                    descripcion = resources.getString(R.string.exercise_flexiones_de_muneca_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_forearms),
                    dificultad = resources.getString(R.string.difficulty_basic),
                    dias = resources.getString(R.string.days_tuesday_thursday_saturday),
                    imagenEjercicio = "https://static.vecteezy.com/system/resources/previews/004/331/543/non_2x/hand-holding-gym-barbell-linear-icon-thin-line-illustration-fitness-and-workout-contour-symbol-isolated-outline-drawing-vector.jpg",
                    videoUrl = "https://www.youtube.com/watch?v=T-QSZZPFn0g",
                    seccion = resources.getString(R.string.section_reduced_mobility),
                    calorias = 15,
                    frecuencia = 15,
                    porcentaje = 0.1f
                ),
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_ejercicios_de_equilibrio_con_apoyo_name),
                    descripcion = resources.getString(R.string.exercise_ejercicios_de_equilibrio_con_apoyo_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_core_legs),
                    dificultad = resources.getString(R.string.difficulty_basic),
                    dias = resources.getString(R.string.days_monday_wednesday_friday),
                    imagenEjercicio = "https://www.performancehealthacademy.com/media/catalog/product/cache/16/image/9df78eab33525d08d6e5fb8d27136e95/S/c/Screen-Shot-2013-10-15-at-10.36.08-AM.png",
                    videoUrl = "https://www.youtube.com/watch?v=jcbXETCpEcM",
                    seccion = resources.getString(R.string.section_rehabilitation),
                    calorias = 35,
                    frecuencia = 10,
                    porcentaje = 0.15f
                ),
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_estiramientos_suaves_name),
                    descripcion = resources.getString(R.string.exercise_estiramientos_suaves_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_multiple),
                    dificultad = resources.getString(R.string.difficulty_basic),
                    dias = resources.getString(R.string.days_all),
                    imagenEjercicio = "https://media.istockphoto.com/id/468978675/es/vector/cuerpo-de-estiramiento-ejercicio-stick-figura-pictograma-icono.jpg",
                    videoUrl = "https://www.youtube.com/watch?v=kfjVFQWWiZw",
                    seccion = resources.getString(R.string.section_rehabilitation),
                    calorias = 25,
                    frecuencia = 15,
                    porcentaje = 0.1f
                ),
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_yoga_adaptado_en_silla_name),
                    descripcion = resources.getString(R.string.exercise_yoga_adaptado_en_silla_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_multiple),
                    dificultad = resources.getString(R.string.difficulty_basic),
                    dias = resources.getString(R.string.days_tuesday_thursday_saturday),
                    imagenEjercicio = "https://i.ytimg.com/vi/KEjiXtb2hRg/hqdefault.jpg",
                    videoUrl = "https://www.youtube.com/watch?v=KEjiXtb2hRg",
                    seccion = resources.getString(R.string.section_rehabilitation),
                    calorias = 45,
                    frecuencia = 12,
                    porcentaje = 0.2f
                )
            )

            val ejercicios = listOf(
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_military_press_name),
                    descripcion = resources.getString(R.string.exercise_military_press_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_shoulders),
                    dificultad = resources.getString(R.string.difficulty_intermediate),
                    dias = resources.getString(R.string.days_monday_thursday),
                    imagenEjercicio = "https://images.pexels.com/photos/2261485/pexels-photo-2261485.jpeg",
                    videoUrl = "https://www.youtube.com/watch?v=5yWaNOvgFCM",
                    seccion = resources.getString(R.string.section_upper_body),
                    calorias = 120,
                    frecuencia = 12,
                    porcentaje = 0.4f
                ),
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_lateral_raises_name),
                    descripcion = resources.getString(R.string.exercise_lateral_raises_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_shoulders),
                    dificultad = resources.getString(R.string.difficulty_basic),
                    dias = resources.getString(R.string.days_monday_thursday),
                    imagenEjercicio = "https://cdn.shopify.com/s/files/1/0269/5551/3900/files/Dumbbell-Lateral-Raise_31c81eee-81c4-4ffe-890d-ee13dd5bbf20_600x600.png",
                    videoUrl = "https://www.youtube.com/watch?v=xyK8UiC-BUw",
                    seccion = resources.getString(R.string.section_upper_body),
                    calorias = 80,
                    frecuencia = 15,
                    porcentaje = 0.3f
                ),
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_press_de_banca_name),
                    descripcion = resources.getString(R.string.exercise_press_de_banca_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_chest),
                    dificultad = resources.getString(R.string.difficulty_intermediate),
                    dias = resources.getString(R.string.days_tuesday_friday),
                    imagenEjercicio = "https://s3assets.skimble.com/assets/2289478/image_iphone.jpg",
                    videoUrl = "https://www.youtube.com/watch?v=gRVjAtPip0Y",
                    seccion = resources.getString(R.string.section_upper_body),
                    calorias = 150,
                    frecuencia = 10,
                    porcentaje = 0.45f
                ),

                Ejercicio(
                    nombre = resources.getString(R.string.exercise_dominadas_name),
                    descripcion = resources.getString(R.string.exercise_dominadas_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_back),
                    dificultad = resources.getString(R.string.difficulty_advanced),
                    dias = resources.getString(R.string.days_wednesday_saturday),
                    imagenEjercicio = "https://anabolicaliens.com/cdn/shop/articles/199990.png",
                    videoUrl = "https://www.youtube.com/shorts/eDP_OOhMTZ4",
                    seccion = resources.getString(R.string.section_upper_body),
                    calorias = 200,
                    frecuencia = 8,
                    porcentaje = 0.5f
                ),
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_remo_con_barra_name),
                    descripcion = resources.getString(R.string.exercise_remo_con_barra_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_back),
                    dificultad = resources.getString(R.string.difficulty_intermediate),
                    dias = resources.getString(R.string.days_wednesday_saturday),
                    imagenEjercicio = "https://training.fit/wp-content/uploads/2020/02/rudern-langhantel-800x448.png",
                    videoUrl = "https://www.youtube.com/watch?v=7B5Exks1KJE",
                    seccion = resources.getString(R.string.section_upper_body),
                    calorias = 130,
                    frecuencia = 12,
                    porcentaje = 0.4f
                ),
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_sentadillas_name),
                    descripcion = resources.getString(R.string.exercise_sentadillas_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_legs),
                    dificultad = resources.getString(R.string.difficulty_intermediate),
                    dias = resources.getString(R.string.days_monday_thursday),
                    imagenEjercicio = "https://images.pexels.com/photos/1954524/pexels-photo-1954524.jpeg",
                    videoUrl = "https://www.youtube.com/watch?v=gcNh17Ckjgg",
                    seccion = resources.getString(R.string.section_lower_body),
                    calorias = 180,
                    frecuencia = 15,
                    porcentaje = 0.45f
                ),
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_extensiones_de_pierna_name),
                    descripcion = resources.getString(R.string.exercise_extensiones_de_pierna_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_legs),
                    dificultad = resources.getString(R.string.difficulty_basic),
                    dias = resources.getString(R.string.days_monday_thursday),
                    imagenEjercicio = "https://i.ytimg.com/vi/m0iMPJbp03w/hqdefault.jpg",
                    videoUrl = "https://www.youtube.com/shorts/m0iMPJbp03w",
                    seccion = resources.getString(R.string.section_lower_body),
                    calorias = 90,
                    frecuencia = 15,
                    porcentaje = 0.3f
                ),
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_hip_thrust_name),
                    descripcion = resources.getString(R.string.exercise_hip_thrust_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_glutes),
                    dificultad = resources.getString(R.string.difficulty_intermediate),
                    dias = resources.getString(R.string.days_tuesday_friday),
                    imagenEjercicio = "https://i.ytimg.com/vi/5S8SApGU_Lk/hqdefault.jpg",
                    videoUrl = "https://www.youtube.com/watch?v=5S8SApGU_Lk",
                    seccion = resources.getString(R.string.section_lower_body),
                    calorias = 140,
                    frecuencia = 12,
                    porcentaje = 0.4f
                ),
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_puente_de_gluteos_name),
                    descripcion = resources.getString(R.string.exercise_puente_de_gluteos_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_glutes),
                    dificultad = resources.getString(R.string.difficulty_basic),
                    dias = resources.getString(R.string.days_tuesday_friday),
                    imagenEjercicio = "https://i.ytimg.com/vi/jFQIewEEf9o/hqdefault.jpg",
                    videoUrl = "https://www.youtube.com/watch?v=jFQIewEEf9o",
                    seccion = resources.getString(R.string.section_lower_body),
                    calorias = 60,
                    frecuencia = 20,
                    porcentaje = 0.25f
                ),
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_burpees_name),
                    descripcion = resources.getString(R.string.exercise_burpees_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_full_body),
                    dificultad = resources.getString(R.string.difficulty_advanced),
                    dias = resources.getString(R.string.days_wednesday_saturday_sunday),
                    imagenEjercicio = "https://images.pexels.com/photos/6456176/pexels-photo-6456176.jpeg",
                    videoUrl = "https://www.youtube.com/watch?v=TU8QYVW0gDU",
                    seccion = resources.getString(R.string.section_cardio),
                    calorias = 250,
                    frecuencia = 15,
                    porcentaje = 0.6f
                ),
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_salto_a_la_comba_name),
                    descripcion = resources.getString(R.string.exercise_salto_a_la_comba_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_full_body),
                    dificultad = resources.getString(R.string.difficulty_intermediate),
                    dias = resources.getString(R.string.days_wednesday_saturday_sunday),
                    imagenEjercicio = "https://images.pexels.com/photos/2780762/pexels-photo-2780762.jpeg",
                    videoUrl = "https://www.youtube.com/watch?v=AnQwqHgZdoQ&pp=ygURc2FsdGFyIGEgbGEgY29tYmE%3D",
                    seccion = resources.getString(R.string.section_cardio),
                    calorias = 220,
                    frecuencia = 30,
                    porcentaje = 0.5f
                )
            )

            database.ejercicioDao().insertarTodos(ejercicios)
            database.ejercicioDao().insertarTodos(ejerciciosAdaptados)

            // Guardar referencia a uno de los ejercicios insertados para estadísticas
            val burpeesEjercicio = database.ejercicioDao().obtenerEjercicioPorNombre(
                resources.getString(R.string.exercise_burpees_name)
            )

            database.estadisticaDao().insertarEstadistica(
                Estadistica(
                    userId = demoUserId,
                    ejercicioId = cableRowId,
                    fecha = System.currentTimeMillis() - (86400000 * 7),
                    ejerciciosCompletados = 5,
                    caloriasQuemadas = 300,
                    tiempoTotal = System.currentTimeMillis() + 3600000,
                    nombreEjercicio = resources.getString(R.string.exercise_cable_row_name),
                    grupoMuscular = resources.getString(R.string.muscle_group_back)
                )
            )

            val newExercises = listOf(

                Ejercicio(
                    nombre = resources.getString(R.string.exercise_balance_support_name),
                    descripcion = resources.getString(R.string.exercise_balance_support_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_core_legs),
                    dificultad = resources.getString(R.string.difficulty_basic),
                    dias = resources.getString(R.string.days_tuesday_friday),
                    imagenEjercicio = "https://www.performancehealthacademy.com/media/catalog/product/cache/16/image/9df78eab33525d08d6e5fb8d27136e95/S/c/Screen-Shot-2013-10-15-at-10.36.08-AM.png",
                    videoUrl = "https://www.youtube.com/watch?v=4YOBIEOobCE",
                    seccion = resources.getString(R.string.section_rehabilitation),
                    calorias = 30,
                    frecuencia = 8,
                    porcentaje = 0.15f
                ),

            )

            database.ejercicioDao().insertarTodos(newExercises)

            database.estadisticaDao().insertarEstadistica(
                Estadistica(
                    userId = demoUserId,
                    ejercicioId = burpeesEjercicio?.id ?: cableRowId,
                    fecha = System.currentTimeMillis() - (86400000 * 2),
                    ejerciciosCompletados = 6,
                    caloriasQuemadas = 500,
                    tiempoTotal = System.currentTimeMillis() + 5400000,
                    nombreEjercicio = resources.getString(R.string.exercise_burpees_name),
                    grupoMuscular = resources.getString(R.string.muscle_group_full_body)
                )
            )
        }
    }
}
