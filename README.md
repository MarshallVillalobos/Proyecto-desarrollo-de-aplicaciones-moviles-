# Código CH — Aplicación Móvil de Estudio y Consulta Legal

Aplicación nativa para Android desarrollada con **Kotlin** y **Jetpack Compose** bajo el patrón de arquitectura **MVVM (Model-View-ViewModel)**. Diseñada para facilitar a estudiantes de Derecho la consulta rápida de cuerpos legales chilenos, lectura adaptativa de artículos, creación de apuntes personales y gestión de textos destacados.

---

##  Integrantes del Equipo
* **Marshall Villalobos**
* **Vicente Tomás Arellano**

---

##  Funcionalidades Implementadas (7 Pantallas)

1. **Login (`LoginScreen`):** Acceso con logotipo institucional (`R.drawable.logo_app`), validación reactiva de formato de correo electrónico y longitud de contraseña desde `LoginViewModel`, retroalimentación visual con bordes e íconos de alerta por campo, y autocompletado de credenciales de prueba.
2. **Catálogo de Normas (`NormasScreen`):** Buscador interactivo en tiempo real y filtrado reactivo por categorías (*Todas*, *Civil*, *Laboral*, *Constitución*) para consultar el *Código Civil*, *Código del Trabajo* y *Constitución Política*.
3. **Listado de Artículos (`ArticulosScreen`):** Visualización de artículos por cuerpo legal con extracto en fuente Serif, insignia dinámica `"Con nota"` e indicador de marcador activo.
4. **Lectura de Artículo (`LecturaScreen`):** Control tipográfico dinámico (`A-` / `A+`) de `12sp` a `28sp` y resaltado del texto legal en color amarillo (`#FFE08A`) mediante animación fluida (`animateColorAsState`).
5. **Nueva Nota (`NuevaNotaScreen`):** Bloque de cita legal y formulario validado desde `EstudioViewModel` que impide guardar apuntes vacíos, integrando animación de confirmación (`AnimatedVisibility`) al guardar.
6. **Mis Estudios (`MisEstudiosScreen`):** Pestañas selectoras (*Notas* y *Destacados*), ventana emergente (`AlertDialog`) para confirmar la eliminación de apuntes e integración con el sistema nativo de Android para compartir/exportar notas (`Intent.ACTION_SEND`).
7. **Perfil de Usuario (`PerfilScreen`):** Tarjeta de sesión activa, control deslizador (`Slider`) de preferencia de lectura persistente, activación de recordatorios diarios mediante notificaciones locales y modal de confirmación para cerrar sesión.

---

##  Arquitectura y Tecnologías Utilizadas

El proyecto (`com.duoc.x`) separa estrictamente las responsabilidades en capas:

* **`ui/screens` y `ui/components`:** Vistas declarativas en Jetpack Compose y barras reutilizables (`CodigoChTopBar`, `CodigoChBottomBar`).
* **`ui/theme`:** Paleta de colores oficial del diseño (`BgApp`, `PrimaryNavy`, `HighlightYellow`, `DangerRed`, etc.).
* **`viewmodel`:** Gestión de estado reactivo con `StateFlow` y lógica de validación centralizada (`LoginViewModel`, `EstudioViewModel`).
* **`model`:** Modelos de datos inmutables y estados de interfaz (`LoginUiState`, `EstudioUiState`, `NormaItem`, `ArticuloItem`).
* **`data` y `repository`:** Persistencia local mediante **Room Database** (`AppDatabase`, `UsuarioDao`, `ArticuloEstudioDao`) administrada a través de `CodigoChRepository`.
* **`navigation`:** Grafo de navegación tipado con `Navigation Compose` (`NavHost` y `sealed class Screen`).
* **Recursos Nativos:**
    1. **Notificaciones Locales:** Permiso `POST_NOTIFICATIONS` solicitado en tiempo de ejecución y emisión mediante `NotificationManager`.
    2. **Compartir / Exportar Contenido:** Uso de `Intent.ACTION_SEND` nativo de Android.

---

##  Pasos para Ejecutar el Proyecto

1. Clonar el repositorio desde GitHub:
   ```bash
   git clone [https://github.com/MarshallVillalobos/Proyecto-desarrollo-de-aplicaciones-moviles-.git](https://github.com/MarshallVillalobos/Proyecto-desarrollo-de-aplicaciones-moviles-.git)
2. Abrir Android Studio y seleccionar Open para cargar la carpeta del proyecto.

3. Esperar a que finalice la sincronización automática de Gradle.

4. Conectar un dispositivo físico con Android (API 24 / Android 7.0 o superior) o iniciar un emulador configurado.

5. Presionar el botón Run 'app' (Shift + F10).

6. (Opcional) En la pantalla de Login, tocar "Crear cuenta" para autocompletar las credenciales de prueba (estudiante.derecho@duocuc.cl / duoc2025) e iniciar sesión.