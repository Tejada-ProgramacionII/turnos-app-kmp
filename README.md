# turnos-app-kmp

Aplicación Kotlin Multiplatform (Android) del sistema distribuido de turnos.

## Estructura

| Módulo | Qué tiene |
|---|---|
| `composeApp` | Código compartido (KMP): la UI con Compose Multiplatform en `src/commonMain`. `src/androidMain` queda para lo que sea propio de Android. |
| `androidApp` | La aplicación Android: `MainActivity` y el manifest. Depende de `composeApp`. |

Se separan en dos módulos porque desde AGP 9 el plugin de Kotlin Multiplatform no se puede aplicar junto con `com.android.application`: el módulo compartido usa `com.android.kotlin.multiplatform.library` ([guía de JetBrains](https://kotlinlang.org/docs/multiplatform/multiplatform-project-agp-9-migration.html)).

Versiones (en `gradle/libs.versions.toml`): Kotlin 2.4.21, Compose Multiplatform 1.12.2, Material 3 1.9.0, AGP 9.4.1, Gradle 9.8.2 (wrapper). `compileSdk`/`targetSdk` 37, `minSdk` 24.

## Requisitos

- JDK 21 o superior (probado con JDK 21).
- Android SDK con Platform 37, Build-Tools, Platform-Tools y Emulator.
- `ANDROID_HOME` apuntando al SDK y sus herramientas en el `PATH`, por ejemplo en `~/.zshrc`:

  ```sh
  export ANDROID_HOME=$HOME/Library/Android/sdk
  export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator
  ```

- Un emulador creado (acá: `Pixel_8`, API 37, arm64).

## Cómo correrla en el emulador

```sh
# 1. Levantar el emulador (o abrirlo desde Android Studio)
emulator -avd Pixel_8 &
adb wait-for-device

# 2. Compilar e instalar
./gradlew :androidApp:installDebug

# 3. Abrir la app
adb shell am start -n ar.edu.um.programacion2.turnosapp/.MainActivity
```

Solo compilar el APK: `./gradlew :androidApp:assembleDebug` (queda en `androidApp/build/outputs/apk/debug/`).
