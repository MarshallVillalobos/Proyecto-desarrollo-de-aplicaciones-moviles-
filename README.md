# Código CH - App de Estudio Legal

## Descripción del Proyecto
Código CH es una aplicación móvil diseñada para el estudio y consulta de legislación actualizada. Permite a los estudiantes de derecho y profesionales leer normativas, destacar artículos clave y gestionar apuntes personales de manera eficiente y organizada.

## Integrantes
* [Marshall Villalobos] - Desarrollador 1 (Módulo de Acceso, Perfil y Configuración Base)
* [Vicente Arellano] - Desarrollador 2 (Módulo de Contenido, Lectura y Notas)

## Funcionalidades Implementadas
* **Autenticación:** Sistema de login con validaciones visuales y gestión de estado.
* **Persistencia Local:** Guardado de sesión y almacenamiento de notas y artículos destacados usando Room Database.
* **Navegación:** Flujo completo entre 7 pantallas usando `Navigation Compose` y `BottomNavigationBar`.
* **Recursos Nativos:**
    * Integración con Notificaciones (Recordatorios de estudio).
    * Uso de Intent nativo para exportar/compartir apuntes.
* **Interfaz Adaptativa:** Componentes visuales basados en Material Design 3 con soporte para personalización (tamaño de fuente) y animaciones de transición.

## Arquitectura
El proyecto está construido utilizando **Kotlin** y **Jetpack Compose**, siguiendo estrictamente el patrón arquitectónico **MVVM** (Model-View-ViewModel) para separar la lógica de negocio de la interfaz visual.

## Pasos para ejecutar
1. Clonar este repositorio: `git clone [URL_DEL_REPO]`
2. Abrir el proyecto en Android Studio (Ladybug o superior).
3. Sincronizar los archivos de Gradle.
4. Ejecutar la aplicación en un emulador (API 33+) o dispositivo físico físico con la depuración USB activada.