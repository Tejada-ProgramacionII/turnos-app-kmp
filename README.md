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

Tests (`commonTest`, corren en la JVM con el `MockEngine` de Ktor): `./gradlew :composeApp:testAndroidHostTest`.

## Conexión con los servicios

Las URLs base están en `composeApp/.../config/ApiConfig.kt`:

| Servicio | URL desde el emulador |
|---|---|
| turnos-reservas-service | `http://10.0.2.2:8081` |
| catalogo-sincronizacion-service | `http://10.0.2.2:8080` |

`10.0.2.2` es el `localhost` de la máquina vista desde el emulador. Turnos tiene que levantarse en el 8081 (`SERVER_PORT=8081`) para no chocar con el catálogo.

- **http en desarrollo:** Android bloquea el tráfico sin cifrar. `androidApp/src/main/res/xml/network_security_config.xml` lo permite **solo** para `10.0.2.2`; cualquier otro host sigue exigiendo https.
- **Permiso de red local (Android 17):** desde Android 17, las apps con `targetSdk` 37 no pueden conectarse a IPs privadas (`10.x`, `172.16–31.x`, `192.168.x`; en desarrollo, `10.0.2.2`, donde corren turnos y el catálogo) sin el permiso de ejecución `ACCESS_LOCAL_NETWORK` ([documentación](https://developer.android.com/privacy-and-security/local-network-permission)). Sin él, las conexiones dan *timeout*. La app lo declara en el manifest y lo pide al abrir; Android lo muestra como "dispositivos cercanos". Si se niega, muestra un aviso con un botón para volver a pedirlo (o, si Android ya no deja mostrar el diálogo, para abrir los ajustes de la app).

  Para concederlo sin tocar la pantalla (por ejemplo, antes de un test instrumentado):

  ```sh
  adb shell pm grant ar.edu.um.programacion2.turnosapp android.permission.ACCESS_LOCAL_NETWORK
  ```
- **Sesión:** el JWT de `POST /api/authenticate` se guarda solo en memoria (`SessionStore`) y un plugin de Ktor lo manda como `Authorization: Bearer` en cada pedido. Al cerrar la app hay que volver a iniciar sesión.

### Endpoints que usa la app

| Clase (`composeApp`) | Método | Endpoint |
|---|---|---|
| `TurnosApi` | `registrar`, `iniciarSesion`, `cuenta` | `POST /api/register`, `POST /api/authenticate`, `GET /api/account` (turnos) |
| `CatalogoApi` | `buscarProfesionales`, `consultarAgenda` | `GET /api/profesionales`, `GET /api/profesionales/{id}` (catálogo) |
| `DisponibilidadApi` | `consultar` | `GET /api/disponibilidad` (turnos) |

Todos devuelven un `ResultadoApi`: `Exito` o un error tipado según el status (`Invalido` 400, `NoAutorizado` 401, `NoEncontrado` 404, `Conflicto` 409, `NoDisponible` 503, `ErrorDeIntegracion` 502, `ErrorHttp` para el resto), `RespuestaInesperada` si el cuerpo no se puede leer y `SinConexion` si falla la red. Las fechas y horas usan kotlinx-datetime (`LocalDate`, `LocalTime`, `DayOfWeek`).
