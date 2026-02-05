# 📻 La Voz de Guamote - Aplicación Android

**Versión:** 1.0.0  
**Desarrollado para:** PUCE (Pontificia Universidad Católica del Ecuador)

---

## 📋 INFORMACIÓN DEL PROYECTO

| Componente | Versión |
|------------|---------|
| Android Studio | Otter 2 Feature Drop 2025.2.2 |
| Gradle | 8.5 |
| Android Gradle Plugin | 8.2.1 |
| Kotlin | 1.9.22 |
| Java/JDK | 17 |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 35 (Android 16) |

---

## 🚀 PASOS PARA EJECUTAR EN ANDROID STUDIO

### PASO 1: Descargar y Extraer

1. Descarga el archivo `la-voz-de-guamote-final.zip`
2. Extrae el contenido en una ubicación de fácil acceso:
   - **Windows:** `C:\Proyectos\LaVozGuamote`
   - **Mac:** `/Users/tu-usuario/Proyectos/LaVozGuamote`
   - **Linux:** `/home/tu-usuario/Proyectos/LaVozGuamote`

### PASO 2: Abrir el Proyecto

1. Abre **Android Studio**
2. Selecciona **File → Open** (o "Open" en la pantalla de bienvenida)
3. Navega hasta la carpeta donde extrajiste el proyecto
4. Selecciona la carpeta raíz del proyecto (donde está el archivo `settings.gradle`)
5. Haz clic en **OK**

### PASO 3: Esperar Sincronización de Gradle

Android Studio descargará automáticamente todas las dependencias. Verás en la parte inferior:
```
Gradle sync in progress...
```

⏳ **Esto puede tardar 5-15 minutos** la primera vez.

Si aparece un mensaje preguntando sobre Gradle wrapper, selecciona **OK** o **Use Gradle wrapper**.

### PASO 4: Verificar SDK

Si aparece un error de SDK:
1. Ve a **File → Project Structure → SDK Location**
2. Verifica que el Android SDK esté configurado
3. Si no tienes API 35, ve a **Tools → SDK Manager**
4. Descarga **Android 16.0 (Baklava) - API 35**

### PASO 5: Crear un Dispositivo Virtual (Emulador)

1. Ve a **Tools → Device Manager**
2. Haz clic en **Create Device** (o el ícono "+")
3. Selecciona **Phone → Pixel 7** (o cualquier teléfono)
4. Haz clic en **Next**
5. Descarga una imagen del sistema:
   - Selecciona **UpsideDownCake** (API 34) o **VanillaIceCream** (API 35)
   - Haz clic en el ícono de descarga ⬇️
6. Espera a que se descargue y selecciónala
7. Haz clic en **Next → Finish**

### PASO 6: Ejecutar la Aplicación

1. En la barra superior, selecciona tu emulador/dispositivo
2. Haz clic en el botón verde **Run ▶️** (o presiona `Shift + F10`)
3. Espera a que se compile e instale la aplicación

---

## 🔧 SOLUCIÓN DE PROBLEMAS COMUNES

### Error: "SDK location not found"
```
File → Project Structure → SDK Location
```
Configura la ruta del Android SDK (usualmente `C:\Users\TU_USUARIO\AppData\Local\Android\Sdk` en Windows)

### Error: "Gradle sync failed"
1. Ve a **File → Invalidate Caches / Restart**
2. Selecciona **Invalidate and Restart**
3. Espera a que se reinicie Android Studio

### Error: "JDK not found"
1. Ve a **File → Project Structure → SDK Location**
2. En "JDK location", selecciona JDK 17 (incluido con Android Studio)

### Error: "Could not resolve dependencies"
1. Verifica tu conexión a internet
2. Ve a **File → Sync Project with Gradle Files**

### El emulador no inicia
1. Verifica que la virtualización esté habilitada en tu BIOS
2. En Windows, asegúrate de tener **Hyper-V** o **HAXM** instalado

---

## 📁 ESTRUCTURA DEL PROYECTO

```
LaVozGuamote/
├── app/
│   ├── build.gradle              # Dependencias del módulo
│   ├── proguard-rules.pro        # Reglas de ProGuard
│   └── src/main/
│       ├── AndroidManifest.xml   # Configuración de la app
│       ├── java/ec/edu/puce/lavozguamote/
│       │   ├── LaVozGuamoteApp.kt       # Application class
│       │   ├── data/
│       │   │   ├── api/ApiService.kt    # Endpoints de la API
│       │   │   └── models/Models.kt     # Modelos de datos
│       │   ├── di/NetworkModule.kt      # Inyección de dependencias
│       │   ├── services/
│       │   │   └── RadioStreamingService.kt  # Servicio de radio
│       │   └── ui/
│       │       ├── MainActivity.kt
│       │       ├── splash/SplashActivity.kt
│       │       ├── home/HomeFragment.kt
│       │       ├── events/EventsFragment.kt
│       │       ├── news/NewsFragment.kt
│       │       ├── books/BooksFragment.kt
│       │       ├── profile/ProfileFragment.kt
│       │       └── adapters/Adapters.kt
│       └── res/
│           ├── layout/           # Diseños de pantallas
│           ├── drawable/         # Iconos y gráficos
│           ├── values/           # Colores, strings, temas
│           ├── menu/             # Menú de navegación
│           ├── navigation/       # Grafo de navegación
│           └── anim/             # Animaciones
├── build.gradle                  # Configuración del proyecto
├── settings.gradle               # Configuración de módulos
└── gradle.properties             # Propiedades de Gradle
```

---

## 📱 FUNCIONALIDADES

### 🏠 Pantalla de Inicio
- Reproductor de radio en vivo
- Últimas noticias
- Acceso rápido a secciones

### 📅 Eventos (Estilo Instagram)
- Feed visual de eventos
- Likes y comentarios
- Información de encargados
- Categorías con colores

### 📰 Noticias
- Galería de imágenes
- Categorías
- Vistas y fechas
- Noticias urgentes y destacadas

### 📚 Libros
- Catálogo con portadas
- Sistema de compra/desbloqueo
- Precios y ofertas
- Valoraciones

### 👤 Perfil
- Login/Registro
- Información de donaciones:
  - Banco Guayaquil
  - Fundación Acción Integral Guamote
  - RUC: 0691709523001
  - Cuenta Corriente: 0006913857
  - Teléfono: 0995224384
  - Email: fundacionaig@faig.ec
- Botones de WhatsApp, Email, Llamar

---

## ⚙️ CONFIGURACIÓN DE LA API

Para conectar con tu backend, edita el archivo `app/build.gradle`:

```gradle
defaultConfig {
    // Cambia estas URLs por las de tu servidor
    buildConfigField "String", "BASE_URL", '"https://TU-API.com/"'
    buildConfigField "String", "STREAMING_URL", '"https://TU-STREAMING.com/live"'
}
```

---

## 🎨 PERSONALIZACIÓN

### Colores (res/values/colors.xml)
- `primary`: #1565C0 (Azul principal)
- `primary_dark`: #0D47A1
- `secondary`: #FF6F00 (Naranja)

### Textos (res/values/strings.xml)
Modifica los textos de la aplicación aquí.

---

## 📞 SOPORTE

Si tienes problemas con la instalación:
1. Revisa la sección de "Solución de Problemas"
2. Verifica que tu Android Studio esté actualizado
3. Asegúrate de tener espacio suficiente en disco (mínimo 10GB)

---

## 📄 LICENCIA

Desarrollado para La Voz de Guamote
© 2024 PUCE - Pontificia Universidad Católica del Ecuador
