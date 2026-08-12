# 🗺️ Flag Flash

[![Kotlin Version](https://img.shields.io/badge/Kotlin-1.9.22-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Android SDK Target](https://img.shields.io/badge/Target%20SDK-36%20%28Android%2016%29-green.svg?logo=android)](https://developer.android.com)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26%20%28Android%208.0%29-orange.svg?logo=android)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28.svg?logo=firebase)](https://firebase.google.com)
[![Database](https://img.shields.io/badge/Local%20DB-Room-6A1B9A.svg?logo=sqlite)](https://developer.android.com/training/data-storage/room)

**Flag Flash** es una aplicación móvil nativa para Android diseñada con un enfoque educativo, competitivo e interactivo. Los usuarios pueden poner a prueba sus conocimientos geográficos de países de todo el mundo mediante diversos modos de juego interactivos, competir en clasificaciones globales, ganar recompensas y personalizar su perfil y mascota virtual a través de una tienda integrada.

---

## 🚀 Características Principales

### 🎮 Modos de Juego Variados
*   **Modo Clásico (Adivinar):** Adivina países, capitales, banderas o escudos de armas. Soporta tres formatos de juego:
    *   `NORMAL`: Supera una ronda de preguntas de longitud definida (ej. 10 preguntas).
    *   `TIME_ATTACK`: Compite contra el reloj para responder la mayor cantidad de preguntas posibles.
    *   `SURVIVAL`: Una sola vida. ¿Hasta dónde puedes llegar sin fallar?
*   **Escribir Países:** Introduce por escrito todos los países de un continente seleccionado o del mundo entero dentro del límite de tiempo.
*   **Más / Menos (Estadísticas):** Un juego interactivo de comparación en el que debes adivinar si el siguiente país tiene **más o menos** población o superficie (área en km²) que el país actual.
*   **Multijugador Local:** Enfréntate a un amigo en tiempo real compartiendo la pantalla del mismo dispositivo móvil.
*   **Multijugador Online:** Crea o únete a salas de juego públicas o privadas mediante el backend de Firebase, compitiendo en vivo contra otros jugadores.

### 🛍️ Tienda y Mascotas Virtuales
Gana monedas y estrellas mientras juegas para usarlas en la tienda del juego:
*   **Mascotas Activas:** Adquiere acompañantes con habilidades especiales para tus partidas:
    *   🦉 **Búho:** Elimina dos opciones incorrectas de la pantalla.
    *   🐱 **Gato:** Te otorga una segunda oportunidad al perder tus vidas.
    *   🐢 **Tortuga:** Anula la penalización de un fallo.
*   **Avatars:** Desbloquea emojis personalizados para tu perfil (`🥷 Ninja`, `🤖 Robot`, `🏴‍☠️ Pirata`, `👑 Rey`, etc.).
*   **Marcos:** Marcos estéticos de perfil para presumir tus logros en el ranking global (Bronce, Plata, Oro, Diamante, Fuego y Agua).

### 🏆 Clasificación y Perfil
*   **Ranking Global:** Sube tus puntuaciones a la nube para competir por los mejores puestos del ranking internacional con el resto de jugadores.
*   **Persistencia de Preferencias:** Guarda tus filtros de juego habituales (continentes, modos) para una experiencia fluida y rápida en cada inicio.

---

## 🛠️ Stack Tecnológico y Arquitectura

La aplicación se ha desarrollado siguiendo las mejores prácticas recomendadas por Google para el desarrollo moderno en Android:

*   **Lenguaje:** Kotlin 100%.
*   **Interfaz de Usuario:** **Jetpack Compose** con directrices de **Material Design 3**, logrando una interfaz moderna, reactiva, animada y adaptable.
*   **Arquitectura:** Patrón **MVVM (Model-View-ViewModel)** junto con **StateFlow** para una gestión de estado robusta y reactiva a los ciclos de vida.
*   **Base de Datos Local:** **Room (SQLite)** para el almacenamiento offline de datos sobre los países (nombres, capitales, continentes, etc.).
*   **Backend & Tiempo Real:** 
    *   **Firebase Authentication:** Registro e inicio de sesión seguro de usuarios.
    *   **Firebase Firestore & Realtime Database:** Sincronización del ranking global y motor multijugador online en tiempo real.
*   **Monetización:** **Google AdMob** con anuncios intersticiales gestionados de forma inteligente para no interferir en la jugabilidad.
*   **Animaciones:** **Konfetti-Compose** para celebrar victorias y animaciones nativas de Compose para transiciones fluidas.
*   **Carga de Imágenes:** **Coil (Compose Image Loader)** y **Glide** para una carga de recursos rápida y con caché integrada.

---

## 📂 Estructura del Proyecto

La estructura de paquetes principal se encuentra organizada de la siguiente manera:

```text
app/src/main/java/alragar2/isi3/uv/flagflash/
│
├── BaseDatos/            # Configuración de Room, entidades (Pais, User) y DAOs
├── authentication/       # Pantallas y lógica de registro e inicio de sesión de Firebase
├── composables/          # Componentes genéricos y modales reutilizables de UI
├── galeria/              # Sección de galería y visualización de fichas de países
├── juego/                # Lógica de juego, multijugador local y Compose Screens
│   └── compose/          # Pantallas de juego específicas (Clásico, Escribir, Más/Menos)
├── musica/               # Gestor de efectos de sonido y música de fondo
├── navigation/           # Grafo de navegación de Jetpack Compose (FlagFlashNavGraph)
├── online/               # Clases de multijugador online y repositorios de salas (Firebase)
├── ranking/              # Interfaz y obtención de rankings globales
└── resultado/            # Pantallas finales de victoria, derrota y puntuaciones
```

---

## ⚙️ Requisitos e Instalación

### Requisitos de Desarrollo
- **Android Studio** (Koala | 2024.1.1 o superior recomendado)
- **JDK 17** (Configurado en Android Studio)
- **Gradle 8.6.0** (Incluido en el Gradle Wrapper)

### Configuración del SDK
- **Compile SDK:** 36 (Android 16)
- **Target SDK:** 36 (Android 16)
- **Min SDK:** 26 (Android 8.0 Oreo)

### Instrucciones de Compilación

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/tu-usuario/FlagFlash.git
   ```
2. **Importar el proyecto:**
   Abre Android Studio y selecciona la carpeta del proyecto. Deja que Gradle descargue las dependencias y sincronice el proyecto.
3. **Configurar Firebase (Opcional para desarrollo local):**
   - Registra tu aplicación en la consola de Firebase.
   - Descarga el archivo `google-services.json` y colócalo en el directorio `/app`.
4. **Compilar y Ejecutar:**
   Selecciona un emulador con API 26 o superior o conecta tu dispositivo físico con depuración USB y pulsa **Run (Shift + F10)**.

---

## 📄 Licencia

Este proyecto está desarrollado bajo la licencia que se defina para el repositorio. Consulta el archivo `LICENSE` para más detalles.
