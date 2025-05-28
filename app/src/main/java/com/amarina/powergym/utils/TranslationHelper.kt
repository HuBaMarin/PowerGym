package com.amarina.powergym.utils

import android.content.Context
import com.amarina.powergym.R

/**
 * Utility class for translating exercise-related terms across different languages
 */
object TranslationHelper {

    /**
     * Translate muscle group names for display purposes
     */
    fun translateMuscleGroup(muscleGroup: String, context: Context): String {
        val simpleName = muscleGroup.lowercase().trim()

        return when {
            // Demo
            simpleName.contains("demo") -> context.getString(R.string.muscle_group_demo)

            // Core + legs
            (simpleName.contains("core") && (simpleName.contains("leg") || simpleName.contains("pierna"))) ||
                    simpleName.contains("core y piernas") ||
                    simpleName.contains("core and legs") -> context.getString(R.string.muscle_group_core_legs)

            // Legs
            simpleName.contains("leg") || simpleName.contains("pierna") ||
                    simpleName.contains("beine") || simpleName.contains("jambe") ||
                    simpleName.contains("脚") -> context.getString(R.string.muscle_group_legs)

            // Arms
            simpleName.contains("arm") || simpleName.contains("brazo") ||
                    simpleName.contains("arme") || simpleName.contains("腕") -> context.getString(R.string.muscle_group_arms)

            // Core
            simpleName.contains("core") || simpleName.contains("rumpf") ||
                    simpleName.contains("noyau") || simpleName.contains("núcleo") ||
                    simpleName.contains("コア") -> context.getString(R.string.muscle_group_core)

            // Shoulders
            simpleName.contains("shoulder") || simpleName.contains("hombro") ||
                    simpleName.contains("schulter") || simpleName.contains("épaule") ||
                    simpleName.contains("肩") -> context.getString(R.string.muscle_group_shoulders)

            // Respiratory
            simpleName.contains("respiratory") || simpleName.contains("respirator") ||
                    simpleName.contains("breathing") || simpleName.contains("atmung") ||
                    simpleName.contains("呼吸") -> context.getString(R.string.muscle_group_respiratory)

            // Forearms
            simpleName.contains("forearm") || simpleName.contains("antebrazo") ||
                    simpleName.contains("unterarm") || simpleName.contains("avant-bras") ||
                    simpleName.contains("前腕") -> context.getString(R.string.muscle_group_forearms)

            // Chest
            simpleName.contains("chest") || simpleName.contains("pecho") ||
                    simpleName.contains("brust") || simpleName.contains("poitrine") ||
                    simpleName.contains("胸") -> context.getString(R.string.muscle_group_chest)

            // Back
            simpleName.contains("back") || simpleName.contains("espalda") ||
                    simpleName.contains("rücken") || simpleName.contains("dos") ||
                    simpleName.contains("背") -> context.getString(R.string.muscle_group_back)

            // Glutes
            simpleName.contains("glute") || simpleName.contains("glúteo") ||
                    simpleName.contains("gesäß") || simpleName.contains("fessier") ||
                    simpleName.contains("臀") -> context.getString(R.string.muscle_group_glutes)

            // Multiple -> Full body
            simpleName.contains("multiple") || simpleName.contains("múltiple") ||
                    simpleName.contains("mehrere") || simpleName.contains("複数") ||
            // Full body
            simpleName.contains("full body") || simpleName.contains("whole body") ||
                    simpleName.contains("cuerpo completo") || simpleName.contains("ganzkörper") ||
                    simpleName.contains("corps entier") || simpleName.contains("全身") ->
                context.getString(R.string.muscle_group_full_body)

            // Default - return original if no translation found
            else -> muscleGroup
        }
    }

    /**
     * Translate section names for display purposes
     */
    fun translateSection(section: String, context: Context): String {
        val simpleName = section.lowercase().trim()

        return when {
            // Demo
            simpleName.contains("demo") -> context.getString(R.string.section_demo)

            // Elderly
            simpleName.contains("elderly") || simpleName.contains("tercera edad") ||
                    simpleName.contains("mayores") || simpleName.contains("senioren") ||
                    simpleName.contains("personnes âgées") || simpleName.contains("高齢者") ->
                context.getString(R.string.section_elderly)

            // Reduced mobility
            simpleName.contains("reduced mobility") || simpleName.contains("movilidad reducida") ||
                    simpleName.contains("eingeschränkte mobilität") || simpleName.contains("mobilité réduite") ||
                    simpleName.contains("可動域制限") -> context.getString(R.string.section_reduced_mobility)

            // Rehabilitation
            simpleName.contains("rehabilitation") || simpleName.contains("rehabilitación") ||
                    simpleName.contains("rehabilitierung") || simpleName.contains("rééducation") ||
                    simpleName.contains("リハビリテーション") -> context.getString(R.string.section_rehabilitation)

            // Upper body
            simpleName.contains("upper body") || simpleName.contains("tren superior") ||
                    simpleName.contains("parte superior") || simpleName.contains("oberkörper") ||
                    simpleName.contains("haut du corps") || simpleName.contains("上半身") ->
                context.getString(R.string.section_upper_body)

            // Lower body
            simpleName.contains("lower body") || simpleName.contains("tren inferior") ||
                    simpleName.contains("parte inferior") || simpleName.contains("unterkörper") ||
                    simpleName.contains("bas du corps") || simpleName.contains("下半身") ->
                context.getString(R.string.section_lower_body)

            // Cardio
            simpleName.contains("cardio") || simpleName.contains("cardiovascular") ||
                    simpleName.contains("kardio") || simpleName.contains("cardio-vasculaire") ||
                    simpleName.contains("有酸素") -> context.getString(R.string.section_cardio)

            // Default - return original if no translation found
            else -> section
        }
    }

    /**
     * Translate difficulty levels for display purposes
     */
    fun translateDifficulty(difficulty: String, context: Context): String {
        val simpleName = difficulty.lowercase().trim()

        return when {
            simpleName.contains("basic") || simpleName.contains("básico") ||
                    simpleName.contains("basico") || simpleName.contains("débutant") ||
                    simpleName.contains("anfänger") || simpleName.contains("初級") ->
                context.getString(R.string.difficulty_basic)

            simpleName.contains("intermediate") || simpleName.contains("intermedio") ||
                    simpleName.contains("intermédiaire") || simpleName.contains("mittelstufe") ||
                    simpleName.contains("中級") -> context.getString(R.string.difficulty_intermediate)

            simpleName.contains("advanced") || simpleName.contains("avanzado") ||
                    simpleName.contains("avancé") || simpleName.contains("fortgeschritten") ||
                    simpleName.contains("上級") -> context.getString(R.string.difficulty_advanced)

            simpleName.contains("adaptable") || simpleName.contains("anpassbar") ->
                context.getString(R.string.difficulty_adaptable)

            else -> difficulty
        }
    }
}
