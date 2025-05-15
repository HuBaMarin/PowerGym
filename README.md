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


## Consideraciones Técnicas Adicionales

- La aplicación utiliza corrutinas de Kotlin para operaciones asíncronas
- Se emplean recursos localizados para soportar múltiples idiomas
- Las URLs de imágenes y videos son externas, apuntando principalmente a contenido de YouTube
- Implementación de trabajo periódico mediante WorkManager para funcionalidades como recordatorios
- Sistema de logs estructurados para facilitar la depuración y el análisis
