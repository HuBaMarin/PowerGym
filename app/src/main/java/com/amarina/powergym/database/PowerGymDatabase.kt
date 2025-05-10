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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

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


    enum class Dificultad(val clave: String) {

        BASICO("basico"),
        INTERMEDIO("intermedio"),
        AVANZADO("avanzado")
    }

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
         * Updates exercise names and descriptions to the current locale
         * Call this method when language is changed in the app
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
                    
                    // Get all exercises
                    val allExercises = database.ejercicioDao().obtenerTodosEjercicios()
                    
                    // Update each exercise with the translated name and description
                    for (exercise in allExercises) {
                        try {
                            // Direct mapping between exercise keys and resource IDs
                            val (nameResId, descResId) = getResourceIdsForExercise(exercise.nombre)

                            if (nameResId != 0 && descResId != 0) {
                                // Get translated strings directly using resource IDs
                                val translatedName = resources.getString(nameResId)
                                val translatedDesc = resources.getString(descResId)

                                Log.d(
                                    "PowerGymDatabase",
                                    "Updating '${exercise.nombre}' to '$translatedName'"
                                )

                                // Use the newly added method to update the translation
                                database.ejercicioDao().actualizarTraduccion(
                                    exercise.id,
                                    translatedName,
                                    translatedDesc
                                )
                                // Update any statistics referencing this exercise
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

                            // Also update muscle group translation
                            val muscleGroupResId =
                                getResourceIdForMuscleGroup(exercise.grupoMuscular)
                            if (muscleGroupResId != 0) {
                                val translatedMuscleGroup = resources.getString(muscleGroupResId)

                                Log.d(
                                    "PowerGymDatabase",
                                    "Updating muscle group '${exercise.grupoMuscular}' to '$translatedMuscleGroup'"
                                )

                                // Update muscle group in the exercise
                                database.ejercicioDao().actualizarGrupoMuscular(
                                    exercise.id,
                                    translatedMuscleGroup
                                )

                                // Update muscle group in any statistics referencing this exercise
                                database.estadisticaDao().actualizarGrupoMuscular(
                                    exercise.id,
                                    translatedMuscleGroup
                                )
                            }
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
         * Get resource IDs for an exercise name without using resource reflection
         * Returns a Pair of (nameResourceId, descriptionResourceId)
         */
        private fun getResourceIdsForExercise(exerciseName: String): Pair<Int, Int> {
            // Create a simplified, lowercase version of the name for comparison
            val simpleName = exerciseName.lowercase().trim()

            return when {
                // Military Press
                simpleName.contains("military press") ||
                        simpleName.contains("press militar") ||
                        simpleName.contains("overhead press") ->
                    Pair(
                        R.string.exercise_military_press_name,
                        R.string.exercise_military_press_desc
                    )

                // Lateral raises
                simpleName.contains("lateral raise") ||
                        simpleName.contains("elevaciones laterales") ||
                        simpleName.contains("elevation lateral") ||
                        simpleName.contains("seitenheben") ->
                    Pair(
                        R.string.exercise_lateral_raises_name,
                        R.string.exercise_lateral_raises_desc
                    )

                // Bench press
                simpleName.contains("press de banca") ||
                        simpleName.contains("bench press") ||
                        simpleName.contains("développé couché") ||
                        simpleName.contains("bankdrücken") ->
                    Pair(
                        R.string.exercise_press_de_banca_name,
                        R.string.exercise_press_de_banca_desc
                    )

                // Chest fly / Aperturas
                simpleName.contains("aperturas") ||
                        simpleName.contains("chest fly") ||
                        simpleName.contains("butterfly") ||
                        simpleName.contains("ouvertures") ||
                        simpleName.contains("pec fly") ||
                        simpleName.contains("brustfliegen") ->
                    Pair(
                        R.string.exercise_aperturas_con_mancuernas_name,
                        R.string.exercise_aperturas_con_mancuernas_desc
                    )

                // Pull-ups / Dominadas
                simpleName.contains("dominadas") ||
                        simpleName.contains("pull up") ||
                        simpleName.contains("pullup") ||
                        simpleName.contains("chin up") ||
                        simpleName.contains("klimmzüge") ||
                        simpleName.contains("traction") ->
                    Pair(R.string.exercise_dominadas_name, R.string.exercise_dominadas_desc)

                // Barbell row
                simpleName.contains("remo con barra") ||
                        simpleName.contains("barbell row") ||
                        simpleName.contains("bent over row") ||
                        simpleName.contains("rameur") ||
                        simpleName.contains("rudern") ->
                    Pair(
                        R.string.exercise_remo_con_barra_name,
                        R.string.exercise_remo_con_barra_desc
                    )

                // Squats
                simpleName.contains("sentadillas") ||
                        simpleName.contains("squat") ||
                        simpleName.contains("kniebeuge") ||
                        simpleName.contains("accroupissement") ->
                    Pair(R.string.exercise_sentadillas_name, R.string.exercise_sentadillas_desc)

                // Leg extensions
                simpleName.contains("extensiones de pierna") ||
                        simpleName.contains("leg extension") ||
                        simpleName.contains("beinstrecker") ||
                        simpleName.contains("extension de jambe") ->
                    Pair(
                        R.string.exercise_extensiones_de_pierna_name,
                        R.string.exercise_extensiones_de_pierna_desc
                    )

                // Hip thrust
                simpleName.contains("hip thrust") ||
                        simpleName.contains("empuje de cadera") ||
                        simpleName.contains("relevé de bassin") ||
                        simpleName.contains("beckenbrücke") ->
                    Pair(R.string.exercise_hip_thrust_name, R.string.exercise_hip_thrust_desc)

                // Glute bridge
                simpleName.contains("puente de gluteos") ||
                        simpleName.contains("glute bridge") ||
                        simpleName.contains("pont fessier") ||
                        simpleName.contains("gesäßbrücke") ||
                        simpleName.contains("ponte de gluteos") ||
                        simpleName.contains("puente de glúteos") ->
                    Pair(
                        R.string.exercise_puente_de_gluteos_name,
                        R.string.exercise_puente_de_gluteos_desc
                    )

                // Burpees
                simpleName.contains("burpees") ||
                        simpleName.contains("burpee") ->
                    Pair(R.string.exercise_burpees_name, R.string.exercise_burpees_desc)

                // Jump rope
                simpleName.contains("salto a la comba") ||
                        simpleName.contains("jump rope") ||
                        simpleName.contains("seilspringen") ||
                        simpleName.contains("corde à sauter") ||
                        simpleName.contains("saltar la cuerda") ->
                    Pair(
                        R.string.exercise_salto_a_la_comba_name,
                        R.string.exercise_salto_a_la_comba_desc
                    )

                // Knee stretch
                simpleName.contains("knee stretch") ||
                        simpleName.contains("estiramiento de rodilla") ||
                        simpleName.contains("estiramientos de rodilla") ||
                        simpleName.contains("kniestrecker") ||
                        simpleName.contains("étirement du genou") ->
                    Pair(R.string.exercise_knee_stretch_name, R.string.exercise_knee_stretch_desc)

                // Seated arm extensions
                simpleName.contains("seated arm extension") ||
                        simpleName.contains("extension de brazo sentado") ||
                        simpleName.contains("extension de brazos sentado") ||
                        simpleName.contains("sitzende armstrecker") ||
                        simpleName.contains("extension de bras assis") ->
                    Pair(
                        R.string.exercise_seated_arm_extensions_name,
                        R.string.exercise_seated_arm_extensions_desc
                    )

                // Various exercises
                simpleName.contains("various exercise") ||
                        simpleName.contains("ejercicios varios") ||
                        simpleName.contains("ejercicios de core") ||
                        simpleName.contains("verschiedene übungen") ||
                        simpleName.contains("exercices divers") ||
                        simpleName.contains("core exercise") ->
                    Pair(
                        R.string.exercise_various_exercises_name,
                        R.string.exercise_various_exercises_desc
                    )

                // Resistance band
                simpleName.contains("resistance band") ||
                        simpleName.contains("banda elastica") ||
                        simpleName.contains("banda de resistencia") ||
                        simpleName.contains("theraband") ||
                        simpleName.contains("elastique") ||
                        simpleName.contains("widerstandsband") ->
                    Pair(
                        R.string.exercise_resistance_band_name,
                        R.string.exercise_resistance_band_desc
                    )

                // Seated lateral arm raises
                simpleName.contains("elevacion lateral") ||
                        simpleName.contains("lateral raise seated") ||
                        simpleName.contains("elevaciones laterales sentado") ||
                        simpleName.contains("elevación lateral de brazos") ||
                        simpleName.contains("sitzende seitheben") ||
                        simpleName.contains("élévation latérale assis") ->
                    Pair(
                        R.string.exercise_elevacion_lateral_de_brazos_sentado_name,
                        R.string.exercise_elevacion_lateral_de_brazos_sentado_desc
                    )

                // Respiratory exercises
                simpleName.contains("respiratory") ||
                        simpleName.contains("respirator") ||
                        simpleName.contains("breathing") ||
                        simpleName.contains("ejercicios respiratorios") ||
                        simpleName.contains("respiración") ||
                        simpleName.contains("atemübung") ||
                        simpleName.contains("exercice respiratoire") ->
                    Pair(
                        R.string.exercise_respiratory_exercises_name,
                        R.string.exercise_respiratory_exercises_desc
                    )

                // Wrist flexions
                simpleName.contains("flexiones de muneca") ||
                        simpleName.contains("wrist flex") ||
                        simpleName.contains("handgelenk") ||
                        simpleName.contains("flexion de poignet") ||
                        simpleName.contains("flexión de muñeca") ->
                    Pair(
                        R.string.exercise_flexiones_de_muneca_name,
                        R.string.exercise_flexiones_de_muneca_desc
                    )

                // Balance exercises
                simpleName.contains("equilibrio") ||
                        simpleName.contains("balance") ||
                        simpleName.contains("ejercicios de equilibrio") ||
                        simpleName.contains("gleichgewichtsübung") ||
                        simpleName.contains("exercice d'équilibre") ->
                    Pair(
                        R.string.exercise_ejercicios_de_equilibrio_con_apoyo_name,
                        R.string.exercise_ejercicios_de_equilibrio_con_apoyo_desc
                    )

                // Gentle stretches
                simpleName.contains("estiramientos") ||
                        simpleName.contains("stretch") ||
                        simpleName.contains("stretching") ||
                        simpleName.contains("dehnung") ||
                        simpleName.contains("étirement") ||
                        simpleName.contains("estiramiento") ->
                    Pair(
                        R.string.exercise_estiramientos_suaves_name,
                        R.string.exercise_estiramientos_suaves_desc
                    )

                // Chair yoga
                simpleName.contains("yoga") ||
                        simpleName.contains("silla") ||
                        simpleName.contains("chair") ||
                        simpleName.contains("stuhl") ||
                        simpleName.contains("chaise") ->
                    Pair(
                        R.string.exercise_yoga_adaptado_en_silla_name,
                        R.string.exercise_yoga_adaptado_en_silla_desc
                    )

                // Cable row
                simpleName.contains("cable row") ||
                        simpleName.contains("remo con cable") ->
                    Pair(
                        R.string.exercise_cable_row_name,
                        R.string.exercise_cable_row_desc
                    )

                // If no match is found, return demo strings as default
                else -> {
                    
                    Pair(R.string.exercise_demo_name, R.string.exercise_demo_desc)
                }
            }
        }



        /**
         * Get resource ID for a muscle group name
         * Returns the resource ID for the given muscle group
         */
        private fun getResourceIdForMuscleGroup(muscleGroup: String): Int {
            // Create a simplified, lowercase version of the muscle group for comparison
            val simpleName = muscleGroup.lowercase().trim()

            return when {
                simpleName.contains("demo") -> R.string.muscle_group_back

                simpleName.contains("legs") ||
                        simpleName.contains("pierna") ||
                        simpleName.contains("beine") ||
                        simpleName.contains("jambe") ||
                        simpleName.contains("脚") -> R.string.muscle_group_legs

                simpleName.contains("arms") ||
                        simpleName.contains("brazo") ||
                        simpleName.contains("arme") ||
                        simpleName.contains("腕") -> R.string.muscle_group_arms

                simpleName.contains("core") ||
                        simpleName.contains("rumpf") ||
                        simpleName.contains("noyau") ||
                        simpleName.contains("núcleo") ||
                        simpleName.contains("コア") -> R.string.muscle_group_core

                simpleName.contains("multiple") ||
                        simpleName.contains("múltiple") ||
                        simpleName.contains("mehrere") ||
                        simpleName.contains("複数") -> R.string.muscle_group_multiple

                simpleName.contains("shoulder") ||
                        simpleName.contains("hombro") ||
                        simpleName.contains("schulter") ||
                        simpleName.contains("épaule") ||
                        simpleName.contains("肩") -> R.string.muscle_group_shoulders

                simpleName.contains("respiratory") ||
                        simpleName.contains("respirat") ||
                        simpleName.contains("breathing") ||
                        simpleName.contains("atmung") ||
                        simpleName.contains("呼吸") -> R.string.muscle_group_respiratory

                simpleName.contains("forearm") ||
                        simpleName.contains("antebrazo") ||
                        simpleName.contains("unterarm") ||
                        simpleName.contains("avant-bras") ||
                        simpleName.contains("前腕") -> R.string.muscle_group_forearms

                simpleName.contains("core") && (simpleName.contains("leg") ||
                        simpleName.contains("pierna") ||
                        simpleName.contains("beine") ||
                        simpleName.contains("jambe") ||
                        simpleName.contains("脚")) -> R.string.muscle_group_core_legs

                simpleName.contains("chest") ||
                        simpleName.contains("pecho") ||
                        simpleName.contains("brust") ||
                        simpleName.contains("poitrine") ||
                        simpleName.contains("胸") -> R.string.muscle_group_chest

                simpleName.contains("back") ||
                        simpleName.contains("espalda") ||
                        simpleName.contains("rücken") ||
                        simpleName.contains("dos") ||
                        simpleName.contains("背中") -> R.string.muscle_group_back

                simpleName.contains("glute") ||
                        simpleName.contains("glúteo") ||
                        simpleName.contains("gesäß") ||
                        simpleName.contains("fessier") ||
                        simpleName.contains("臀部") -> R.string.muscle_group_glutes

                simpleName.contains("full body") ||
                        simpleName.contains("whole body") ||
                        simpleName.contains("cuerpo completo") ||
                        simpleName.contains("ganzkörper") ||
                        simpleName.contains("corps entier") ||
                        simpleName.contains("全身") -> R.string.muscle_group_full_body

                else -> R.string.muscle_group_multiple // Default to multiple as a fallback
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
            // Update exercise translations each time database is opened
            // with the current locale context
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
                    dificultad = Dificultad.INTERMEDIO.clave,
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
                    dificultad = Dificultad.BASICO.clave,
                    dias = resources.getString(R.string.days_monday_wednesday_friday),
                    imagenEjercicio = "https://img.youtube.com/vi/rfMWJcWXhZo/maxresdefault.jpg",
                    videoUrl = "https://www.youtube.com/watch?v=rfMWJcWXhZo",
                    seccion = resources.getString(R.string.section_elderly),
                    calorias = 40,
                    frecuencia = 8,
                    porcentaje = 0.2f
                ),
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_seated_arm_extensions_name),
                    descripcion = resources.getString(R.string.exercise_seated_arm_extensions_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_arms),
                    dificultad = Dificultad.BASICO.clave,
                    dias = resources.getString(R.string.days_tuesday_thursday),
                    imagenEjercicio = "https://i.ytimg.com/vi/ASXzHrWwJxI/hqdefault.jpg",
                    videoUrl = "https://www.youtube.com/watch?v=ASXzHrWwJxI",
                    seccion = resources.getString(R.string.section_elderly),
                    calorias = 35,
                    frecuencia = 10,
                    porcentaje = 0.15f
                ),
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_various_exercises_name),
                    descripcion = resources.getString(R.string.exercise_various_exercises_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_core),
                    dificultad = Dificultad.BASICO.clave,
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
                    nombre = resources.getString(R.string.exercise_elevacion_lateral_de_brazos_sentado_name),
                    descripcion = resources.getString(R.string.exercise_elevacion_lateral_de_brazos_sentado_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_shoulders),
                    dificultad = Dificultad.BASICO.clave,
                    dias = resources.getString(R.string.days_tuesday_thursday),
                    imagenEjercicio = "https://i.ytimg.com/vi/wFYnizn--6o/hqdefault.jpg",
                    videoUrl = "https://www.youtube.com/watch?v=wFYnizn--6o",
                    seccion = resources.getString(R.string.section_reduced_mobility),
                    calorias = 40,
                    frecuencia = 12,
                    porcentaje = 0.2f
                ),
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_respiratory_exercises_name),
                    descripcion = resources.getString(R.string.exercise_respiratory_exercises_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_respiratory),
                    dificultad = Dificultad.BASICO.clave,
                    dias = resources.getString(R.string.days_all),
                    imagenEjercicio = "https://i.ytimg.com/vi/rqGS9UvXrJU/hqdefault.jpg",
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
                    dificultad = Dificultad.BASICO.clave,
                    dias = resources.getString(R.string.days_tuesday_thursday_saturday),
                    imagenEjercicio = "https://i.ytimg.com/vi/T-QSZZPFn0g/hqdefault.jpg",
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
                    dificultad = Dificultad.BASICO.clave,
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
                    dificultad = Dificultad.BASICO.clave,
                    dias = resources.getString(R.string.days_all),
                    imagenEjercicio = "https://i.ytimg.com/vi/1lMhH8chKpE/hqdefault.jpg",
                    videoUrl = "https://www.youtube.com/watch?v=1lMhH8chKpE",
                    seccion = resources.getString(R.string.section_rehabilitation),
                    calorias = 25,
                    frecuencia = 15,
                    porcentaje = 0.1f
                ),
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_yoga_adaptado_en_silla_name),
                    descripcion = resources.getString(R.string.exercise_yoga_adaptado_en_silla_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_multiple),
                    dificultad = Dificultad.BASICO.clave,
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
                    dificultad = Dificultad.INTERMEDIO.clave,
                    dias = resources.getString(R.string.days_monday_thursday),
                    imagenEjercicio = "https://i.ytimg.com/vi/o5M9RZ-vWrc/hqdefault.jpg",
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
                    dificultad = Dificultad.BASICO.clave,
                    dias = resources.getString(R.string.days_monday_thursday),
                    imagenEjercicio = "https://i.ytimg.com/vi/3VcKaXpzqRo/hqdefault.jpg",
                    videoUrl = "https://www.youtube.com/shorts/TuY1eKCo9l0",
                    seccion = resources.getString(R.string.section_upper_body),
                    calorias = 80,
                    frecuencia = 15,
                    porcentaje = 0.3f
                ),
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_press_de_banca_name),
                    descripcion = resources.getString(R.string.exercise_press_de_banca_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_chest),
                    dificultad = Dificultad.INTERMEDIO.clave,
                    dias = resources.getString(R.string.days_tuesday_friday),
                    imagenEjercicio = "https://i.ytimg.com/vi/gRVjAtPip0Y/hqdefault.jpg",
                    videoUrl = "https://www.youtube.com/watch?v=gRVjAtPip0Y",
                    seccion = resources.getString(R.string.section_upper_body),
                    calorias = 150,
                    frecuencia = 10,
                    porcentaje = 0.45f
                ),
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_aperturas_con_mancuernas_name),
                    descripcion = resources.getString(R.string.exercise_aperturas_con_mancuernas_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_chest),
                    dificultad = Dificultad.BASICO.clave,
                    dias = resources.getString(R.string.days_tuesday_friday),
                    imagenEjercicio = "https://i.ytimg.com/vi/eozdVDA78K0/hqdefault.jpg",
                    videoUrl = "https://www.youtube.com/shorts/rk8YayRoTRQ",
                    seccion = resources.getString(R.string.section_upper_body),
                    calorias = 100,
                    frecuencia = 12,
                    porcentaje = 0.35f
                ),
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_dominadas_name),
                    descripcion = resources.getString(R.string.exercise_dominadas_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_back),
                    dificultad = Dificultad.AVANZADO.clave,
                    dias = resources.getString(R.string.days_wednesday_saturday),
                    imagenEjercicio = "https://i.ytimg.com/vi/eDP_OOhMTZ4/hqdefault.jpg",
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
                    dificultad = Dificultad.INTERMEDIO.clave,
                    dias = resources.getString(R.string.days_wednesday_saturday),
                    imagenEjercicio = "https://i.ytimg.com/vi/7B5Exks1KJE/hqdefault.jpg",
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
                    dificultad = Dificultad.INTERMEDIO.clave,
                    dias = resources.getString(R.string.days_monday_thursday),
                    imagenEjercicio = "https://i.ytimg.com/vi/gcNh17Ckjgg/hqdefault.jpg",
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
                    dificultad = Dificultad.BASICO.clave,
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
                    dificultad = Dificultad.INTERMEDIO.clave,
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
                    dificultad = Dificultad.BASICO.clave,
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
                    dificultad = Dificultad.AVANZADO.clave,
                    dias = resources.getString(R.string.days_wednesday_saturday_sunday),
                    imagenEjercicio = "https://i.ytimg.com/vi/TU8QYVW0gDU/hqdefault.jpg",
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
                    dificultad = Dificultad.INTERMEDIO.clave,
                    dias = resources.getString(R.string.days_wednesday_saturday_sunday),
                    imagenEjercicio = "https://i.ytimg.com/vi/AnQwqHgZdoQ/hqdefault.jpg",
                    videoUrl = "https://www.youtube.com/watch?v=AnQwqHgZdoQ&pp=ygURc2FsdGFyIGEgbGEgY29tYmE%3D",
                    seccion = resources.getString(R.string.section_cardio),
                    calorias = 220,
                    frecuencia = 30,
                    porcentaje = 0.5f
                )
            )

            database.ejercicioDao().insertarTodos(ejercicios)
            database.ejercicioDao().insertarTodos(ejerciciosAdaptados)

            // Store reference to one of the inserted exercises for statistics
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

            // Add our new exercises
            val newExercises = listOf(
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_seated_arm_curl_name),
                    descripcion = resources.getString(R.string.exercise_seated_arm_curl_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_arms),
                    dificultad = Dificultad.BASICO.clave,
                    dias = resources.getString(R.string.days_monday_wednesday_friday),
                    imagenEjercicio = "https://morelifehealth.com/hubfs/Screenshot%202021-12-08%20at%2014.40.50.png",
                    videoUrl = "https://www.youtube.com/watch?v=kDqklk1ZESo",
                    seccion = resources.getString(R.string.section_reduced_mobility),
                    calorias = 40,
                    frecuencia = 12,
                    porcentaje = 0.2f
                ),
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_wall_pushup_name),
                    descripcion = resources.getString(R.string.exercise_wall_pushup_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_chest),
                    dificultad = Dificultad.BASICO.clave,
                    dias = resources.getString(R.string.days_tuesday_thursday),
                    imagenEjercicio = "https://morelifehealth.com/hubfs/Screenshot%202021-12-07%20at%2011.40.51.png",
                    videoUrl = "https://www.youtube.com/watch?v=OBPuG7EM8Cg",
                    seccion = resources.getString(R.string.section_upper_body),
                    calorias = 50,
                    frecuencia = 10,
                    porcentaje = 0.25f
                ),
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_seated_marching_name),
                    descripcion = resources.getString(R.string.exercise_seated_marching_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_legs),
                    dificultad = Dificultad.BASICO.clave,
                    dias = resources.getString(R.string.days_monday_wednesday_friday),
                    imagenEjercicio = "https://www.flintrehab.com/wp-content/uploads/2023/01/seated-marching-stroke-exercise.jpg",
                    videoUrl = "https://www.youtube.com/watch?v=DfDIQ5IwLDM",
                    seccion = resources.getString(R.string.section_lower_body),
                    calorias = 35,
                    frecuencia = 15,
                    porcentaje = 0.15f
                ),
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_balance_support_name),
                    descripcion = resources.getString(R.string.exercise_balance_support_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_core_legs),
                    dificultad = Dificultad.BASICO.clave,
                    dias = resources.getString(R.string.days_tuesday_friday),
                    imagenEjercicio = "https://www.performancehealthacademy.com/media/catalog/product/cache/16/image/9df78eab33525d08d6e5fb8d27136e95/S/c/Screen-Shot-2013-10-15-at-10.36.08-AM.png",
                    videoUrl = "https://www.youtube.com/watch?v=4YOBIEOobCE",
                    seccion = resources.getString(R.string.section_rehabilitation),
                    calorias = 30,
                    frecuencia = 8,
                    porcentaje = 0.15f
                ),
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_shoulder_rolls_name),
                    descripcion = resources.getString(R.string.exercise_shoulder_rolls_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_shoulders),
                    dificultad = Dificultad.BASICO.clave,
                    dias = resources.getString(R.string.days_all),
                    imagenEjercicio = "https://morelifehealth.com/hubfs/Screenshot%202021-12-08%20at%2014.39.52.png",
                    videoUrl = "https://www.youtube.com/watch?v=JmzxJZRCzVk",
                    seccion = resources.getString(R.string.section_upper_body),
                    calorias = 20,
                    frecuencia = 20,
                    porcentaje = 0.1f
                ),
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_knee_extension_name),
                    descripcion = resources.getString(R.string.exercise_knee_extension_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_legs),
                    dificultad = Dificultad.BASICO.clave,
                    dias = resources.getString(R.string.days_tuesday_thursday),
                    imagenEjercicio = "https://www.hingehealth.com/resources/static/f544c3dfa0c41156255696af1dcf6be4/leg-strengthening-exercises-for-seniors-1.webp",
                    videoUrl = "https://www.youtube.com/watch?v=5HZGS-8JcE8",
                    seccion = resources.getString(R.string.section_lower_body),
                    calorias = 40,
                    frecuencia = 12,
                    porcentaje = 0.2f
                ),
                Ejercicio(
                    nombre = resources.getString(R.string.exercise_tabletop_circle_name),
                    descripcion = resources.getString(R.string.exercise_tabletop_circle_desc),
                    grupoMuscular = resources.getString(R.string.muscle_group_arms),
                    dificultad = Dificultad.BASICO.clave,
                    dias = resources.getString(R.string.days_monday_wednesday_friday),
                    imagenEjercicio = "https://www.flintrehab.com/wp-content/uploads/2023/01/tabletop-circle-stroke-exercise.jpg",
                    videoUrl = "https://www.youtube.com/watch?v=lvwixnrxPJE",
                    seccion = resources.getString(R.string.section_rehabilitation),
                    calorias = 25,
                    frecuencia = 15,
                    porcentaje = 0.15f
                )
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