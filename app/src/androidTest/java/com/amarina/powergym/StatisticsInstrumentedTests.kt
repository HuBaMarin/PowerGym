package com.amarina.powergym

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amarina.powergym.ui.activity.StatisticsActivity
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.startsWith
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas instrumentadas para la funcionalidad de Estadísticas de la aplicación PowerGym.
 *
 * Estas pruebas verifican que la funcionalidad de estadísticas funciona correctamente
 * en un dispositivo real o emulador.
 */
@RunWith(AndroidJUnit4::class)
class StatisticsInstrumentedTests {

    /**
     * Comprueba que la actividad de Estadísticas se lanza correctamente y
     * muestra el encabezado esperado.
     */
    @Test
    fun testStatisticsActivityLaunch() {
        // Inicia la actividad
        ActivityScenario.launch(StatisticsActivity::class.java)
        
        // Verifica que se muestra el encabezado
        onView(withId(R.id.tvMostFrequentExercises))
            .check(matches(isDisplayed()))
        
        onView(withText("Progress"))
            .check(matches(isDisplayed()))
    }
    
    /**
     * Comprueba que la lista de ejercicios más frecuentes se muestra y está
     * poblada con datos.
     */
    @Test
    fun testMostFrequentExercisesListDisplayed() {
        ActivityScenario.launch(StatisticsActivity::class.java)
        
        // Verifica que el contenedor de la lista de ejercicios se muestra
        onView(withId(R.id.rvGruposATrabajar))
            .check(matches(isDisplayed()))
            
        // Verifica que la lista contiene al menos un elemento válido
        onView(withId(R.id.rvGruposATrabajar))
            .check(matches(hasMinimumChildCount(1)))
            
        // Valida el contenido del primer elemento
        onView(withId(R.id.rvGruposATrabajar))
            .check(matches(hasDescendant(withText(startsWith("grupoMuscular: piernas")))))

    }
}