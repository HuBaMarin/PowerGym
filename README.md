# PowerGym - Documentación Completa

## Introducción

PowerGym es una aplicación de entrenamiento físico diseñada para usuarios con diferentes niveles de
condición física. Permite realizar seguimiento de ejercicios, establecer rutinas personalizadas y
monitorizar el progreso a lo largo del tiempo. Este documento explica en detalle la estructura y
funcionamiento de la aplicación.

## Arquitectura General

La aplicación está desarrollada siguiendo el patrón de arquitectura MVVM (Model-View-ViewModel) e
implementa los principios de la arquitectura limpia (Clean Architecture), organizando el código en
capas claramente definidas para mejorar la mantenibilidad y escalabilidad.

### Componentes Principales

- **Capa de Presentación**: Activities, Fragments y ViewModels
- **Capa de Dominio**: Casos de uso e interfaces de repositorios
- **Capa de Datos**: Implementaciones de repositorios, fuentes de datos (Room, APIs)

## Flujo de Trabajo de la Aplicación

### Autenticación y Control de Usuarios

1. **Verificación de Autenticación**
    - Al iniciar la aplicación, se comprueba si el usuario está autenticado
    - Si no está autenticado, se redirige a `LoginActivity`

2. **Tipos de Usuarios**
    - **Usuarios Estándar**: Acceso a ejercicios, estadísticas y configuración personal
    - **Administradores**: Acceso adicional al panel de administración

3. **Panel de Administrador**
    - Gestión de usuarios (alta, baja, modificación)
    - Gestión de ejercicios (creación, edición, eliminación)
    - Control de estadísticas globales

### Flujo Principal (Usuario Estándar)

1. **Pantalla Principal (`PrincipalActivity`)**
    - Muestra una lista de ejercicios disponibles
    - Permite filtrado por dificultad y categoría
    - Acceso rápido a estadísticas personales

2. **Configuración**
    - Personalización de preferencias de usuario
    - Ajustes de notificaciones y recordatorios
    - Selección de tema (claro/oscuro)

3. **Seguimiento de Ejercicios**
    - Vista detallada de ejercicios (`EjercicioDetailActivity`)
    - Registro de series, repeticiones y tiempo
    - Representación visual del ejercicio (imagen/vídeo)

4. **Estadísticas**
    - Visualización de progreso (`StatisticsActivity`)
    - Gráficas de rendimiento y evolución
    - Resumen de actividad reciente

### Procesamiento Concurrente

La aplicación implementa procesamiento concurrente para operaciones que requieren cálculos
intensivos o acceso a datos, utilizando:

- **Corrutinas de Kotlin**: Para operaciones asíncronas
- **LiveData**: Para actualización reactiva de la UI
- **Flow**: Para secuencias asíncronas de datos

## Estructura de la Base de Datos

La aplicación utiliza Room, una librería de persistencia de Android que proporciona una capa de
abstracción sobre SQLite. El archivo principal `PowerGymDatabase.kt` define la estructura completa
de la base de datos.

### Anotaciones y Configuración

```kotlin
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
```

Esta anotación define:

- Las entidades (tablas) que contiene la base de datos
- La versión actual (1)
- La opción de no exportar el esquema

## Entidades

La base de datos consta de cuatro entidades principales:

### Usuario

Almacena la información de los usuarios registrados:

- Email
- Contraseña
- Nombre
- Rol
- Token de único uso (para credenciales de administrador)

### Ejercicio

Contiene todos los ejercicios disponibles en la aplicación con la siguiente información:

- Nombre
- Descripción
- Grupo muscular
- Dificultad
- Días recomendados
- Imagen del ejercicio
- URL del video demostrativo
- Sección
- Calorías estimadas
- Frecuencia recomendada
- Porcentaje de intensidad

### Estadistica

Registra el historial de ejercicios realizados por los usuarios:

- ID del usuario
- ID del ejercicio
- Fecha de realización
- Ejercicios completados
- Calorías quemadas
- Tiempo total de entrenamiento
- Nombre del ejercicio
- Grupo muscular trabajado

### Preferencia

Guarda las configuraciones personalizadas de cada usuario:

- ID del usuario
- Notificaciones habilitadas
- Tema oscuro
- Recordatorios
- Frecuencia de recordatorios

## DAOs (Objetos de Acceso a Datos)

La base de datos expone cuatro DAOs para interactuar con las entidades:

```kotlin
abstract fun userDao(): UsuarioDao
abstract fun ejercicioDao(): EjercicioDao
abstract fun estadisticaDao(): EstadisticaDao
abstract fun preferenciaDao(): PreferenciaDao
```

Estos DAOs contienen métodos para realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar) en
cada entidad.

## Niveles de Dificultad

La aplicación clasifica los ejercicios en tres niveles de dificultad mediante un enum:

```kotlin
enum class Dificultad(val clave: String) {
    BASICO("basico"),
    INTERMEDIO("intermedio"),
    AVANZADO("avanzado")
}
```

## Patrón Singleton para la Base de Datos

La base de datos implementa el patrón Singleton para garantizar una única instancia:

```kotlin
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
}
```

Esto asegura que:

- Solo existe una instancia de la base de datos en toda la aplicación
- El acceso es thread-safe gracias al bloque `synchronized`
- Se utiliza una clase callback para inicializar datos

## Inicialización y Datos Predeterminados

La clase `PowerGymDatabaseCallback` se encarga de poblar la base de datos con datos iniciales cuando
esta se crea por primera vez:

```kotlin
private class PowerGymDatabaseCallback(private val context: Context) : Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
            CoroutineScope(Dispatchers.IO).launch {
                populateDatabase(database, context)
            }
        }
    }
    
    suspend fun populateDatabase(database: PowerGymDatabase, context: Context) {
        // Código de inicialización...
    }
}
```

### Credenciales de Administrador de Un Solo Uso

Para aumentar la seguridad, la aplicación implementa un sistema de credenciales de administrador que
son válidas por una única vez durante la primera ejecución de la aplicación:

```kotlin
private fun crearCredencialesAdmin(database: PowerGymDatabase) {
    // Generación de credenciales de administrador aleatorias
    val email = "admin_${UUID.randomUUID().toString().substring(0, 8)}@powergym.com"
    val password = generateSecurePassword()
    
    // Creación del usuario administrador con token de único uso
    val adminUser = Usuario(
        email = email,
        password = hashPassword(password), // Contraseña hasheada
        nombre = "Administrador",
        rol = "admin",
        oneTimeToken = UUID.randomUUID().toString() // Token único para validación
    )
    
    // Insertar en la base de datos
    database.userDao().insertUser(adminUser)
    
    // Mostrar las credenciales en el log solo durante la primera ejecución
    Log.i("PowerGym", "CREDENCIALES ADMIN (válidas por única vez):")
    Log.i("PowerGym", "Email: $email")
    Log.i("PowerGym", "Contraseña: $password")
}
```

El sistema de token de único uso garantiza que estas credenciales solo puedan utilizarse una vez,
tras lo cual el administrador debe crear sus propias credenciales permanentes. Este enfoque
proporciona mayor seguridad al eliminar las credenciales predeterminadas que podrían ser un vector
de ataque común.

### Datos de Demostración

La base de datos se inicializa con:

1. **Usuario Demo**
    - Email: a@a.com
    - Contraseña: demo123
    - Nombre: Usuario Demo
    - Rol: usuario

2. **Preferencias Predeterminadas**
    - Notificaciones: Activadas
    - Tema oscuro: Activado
    - Recordatorios: Activados
    - Frecuencia: Diaria

3. **Ejercicio de Demostración**
    - Contiene información básica para mostrar el funcionamiento de la aplicación

4. **Ejercicios Reales**
    - La base de datos se inicializa con dos categorías principales de ejercicios:
        - Ejercicios estándar: Para usuarios con movilidad completa
        - Ejercicios adaptados: Para adultos mayores, personas con movilidad reducida o en
          rehabilitación

### Categorías de Ejercicios

1. **Parte Superior**
    - Ejercicios para hombros, pecho y espalda
    - Ejemplos: press militar, aperturas con mancuernas, dominadas

2. **Parte Inferior**
    - Ejercicios para piernas y glúteos
    - Ejemplos: sentadillas, extensiones de pierna, hip thrust

3. **Cardio**
    - Ejercicios de alta intensidad para el sistema cardiovascular
    - Ejemplos: burpees, salto a la comba

4. **Ejercicios Adaptados**
    - **Adultos Mayores**: Ejercicios de baja intensidad y bajo impacto
    - **Movilidad Reducida**: Ejercicios que pueden realizarse sentado o con apoyo
    - **Rehabilitación**: Ejercicios para recuperación física

## Componentes de la UI

### Activities Principales

1. **LoginActivity**
    - Gestiona la autenticación de usuarios
    - Valida credenciales contra la base de datos
    - Dirige al usuario a su flujo correspondiente (administrador o estándar)

2. **PrincipalActivity**
    - Punto de entrada principal de la aplicación para usuarios autenticados
    - Muestra la lista de ejercicios disponibles usando un RecyclerView
    - Implementa filtros por categoría, dificultad y tipo de ejercicio

3. **AdminControlActivity**
    - Panel de control para administradores
    - Proporciona acceso a la gestión de usuarios y ejercicios
    - Incluye funciones de análisis de datos

4. **EjercicioDetailActivity**
    - Muestra información detallada de un ejercicio específico
    - Permite registrar el progreso del usuario
    - Incluye reproductor de video demostrativo

5. **StatisticsActivity**
    - Visualización de estadísticas y progreso del usuario
    - Gráficas de rendimiento y evolución
    - Exportación de datos en formato CSV

### Adapters y ViewHolders

La aplicación utiliza RecyclerViews con adapters personalizados para mostrar datos de manera
eficiente:

- **EjercicioAdapter**: Para listas de ejercicios
- **EstadisticaAdapter**: Para visualización de estadísticas
- **UserAdapter**: Para la gestión de usuarios (admin)

### Componentes de Material Design

La interfaz de usuario se basa en los componentes de Material Design 3 para proporcionar una
experiencia moderna y coherente:

- BottomNavigationView para la navegación principal
- CardView para mostrar información de ejercicios
- MaterialToolbar para operaciones comunes
- FloatingActionButton para acciones principales

## Manejo de Estado y Errores

### Estado de la Aplicación

El estado de la aplicación se gestiona mediante LiveData y/o StateFlow, permitiendo:

- Actualizaciones reactivas de la UI
- Preservación del estado durante cambios de configuración
- Gestión eficiente de recursos

### Control de Errores

La aplicación implementa un sistema robusto de manejo de errores:

- Errores de red manejados con reintentos automáticos
- Validación de datos de entrada en el cliente
- Registro de errores para depuración y análisis

## Consideraciones de Seguridad

1. **Autenticación**
    - Las contraseñas se almacenan utilizando algoritmos de hash seguros (BCrypt)
    - Implementación de tokens de un solo uso para credenciales iniciales de administrador
    - Bloqueo temporal después de múltiples intentos fallidos

2. **Protección de Datos**
    - Cifrado de datos sensibles en la base de datos
    - Validación de entrada para prevenir inyección SQL
    - Permisos adecuados para acceso a datos

3. **Consideraciones RGPD**
    - Consentimiento explícito para recopilación de datos
    - Posibilidad de exportar y eliminar datos del usuario
    - Política de privacidad clara y accesible

## Requisitos Técnicos

- **Versión mínima de Android**: API 24 (Android 7.0 Nougat)
- **Versión objetivo**: API 34 (Android 14)
- **Idiomas soportados**: Español, Inglés
- **Librerías principales**:
    - AndroidX Core/AppCompat
    - Material Components
    - Room
    - ViewModel y LiveData
    - Navigation Component
    - Glide para carga de imágenes
    - MPAndroidChart para gráficas
    - ExoPlayer para reproducción de vídeos
    - Retrofit para comunicación con APIs (versión Pro)

## Implementación de Características Avanzadas

### Procesamiento Concurrente

La aplicación utiliza corrutinas de Kotlin y trabajadores en segundo plano para:

- Carga y procesamiento de datos sin bloquear la UI
- Sincronización periódica con servicios externos (versión Pro)
- Generación de informes y análisis de rendimiento

### Arquitectura Modular

El proyecto sigue una estructura modular para facilitar el desarrollo colaborativo:

- **core**: Clases base y utilidades comunes
- **auth**: Autenticación y gestión de usuarios
- **exercise**: Funcionalidad de ejercicios
- **stats**: Estadísticas y análisis
- **admin**: Funcionalidad exclusiva para administradores

## Estructura de Archivos

El código se organiza siguiendo los principios de arquitectura limpia:

- **app/**: Módulo principal de la aplicación
    - **src/main/**
        - **java/com/powergym/**
            - **ui/**: Activities, Fragments y ViewModels
            - **domain/**: Casos de uso e interfaces
            - **data/**: Implementaciones de repositorios
                - **database/**: Base de datos Room
                    - **dao/**: Interfaces DAO
                    - **entities/**: Entidades de la base de datos
                - **network/**: Comunicación con APIs (versión Pro)
            - **util/**: Clases de utilidad
        - **res/**: Recursos (layouts, drawables, strings)

## Estadísticas Iniciales

El sistema inicializa algunas estadísticas para el usuario de demostración, mostrando datos de
entrenamiento de los últimos 7 días, lo que permite visualizar inmediatamente el funcionamiento de
los gráficos y resúmenes de actividad.

## Consideraciones Técnicas Adicionales

- La aplicación utiliza corrutinas de Kotlin para operaciones asíncronas
- Se emplean recursos localizados para soportar múltiples idiomas
- Las URLs de imágenes y videos son externas, apuntando principalmente a contenido de YouTube
- Implementación de trabajo periódico mediante WorkManager para funcionalidades como recordatorios
- Sistema de logs estructurados para facilitar la depuración y el análisis