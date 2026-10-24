# TFG – Gestión de la asociación Ainara Aisi Taldea

Sistema de gestión para una asociación: actividades y calendario, participantes (niños y tutores), hinchables, contabilidad y facturas, y usuarios. Consta de tres partes que comparten la misma base de datos MySQL (`gestion_asociacion`).

```
.
├── android-app/   App Android (Kotlin) – consulta y edita datos vía PHP
├── backend-php/   API en PHP que usa la app Android (JSON sobre HTTP)
└── desktop-app/   App de escritorio (Java 17 + JavaFX + Maven) con acceso directo a MySQL
```

## Requisitos

- MySQL / MariaDB con una base de datos llamada `gestion_asociacion`
- Apache + PHP (p. ej. XAMPP) para el backend
- Android Studio (compileSdk 36, minSdk 30) para la app móvil
- JDK 17 y Maven (o el wrapper `mvnw`) para la app de escritorio

## Puesta en marcha

### 1. Base de datos
Crea la base `gestion_asociacion` e importa el esquema de tablas del proyecto
(el esquema SQL no está incluido en este repositorio; añádelo en `database/schema.sql`).

### 2. Backend PHP
Copia el contenido de `backend-php/` a la carpeta `TFG` de tu servidor web
(p. ej. `C:\xampp\htdocs\TFG\`). Las credenciales de MySQL se configuran en
`conexion.php` (por defecto `localhost`, usuario `root`, sin contraseña: solo para desarrollo).

### 3. App Android
Abre `android-app/` en Android Studio. La app llama a `http://10.0.2.2/TFG/...`,
que es la dirección del `localhost` del PC desde el emulador de Android.
Para un dispositivo físico, cambia esa dirección por la IP de tu servidor.

### 4. App de escritorio
```bash
cd desktop-app
./mvnw javafx:run
```
La conexión se configura en `src/main/resources/config/database.properties`.

## Notas

- Proyecto de desarrollo / TFG: las credenciales por defecto son solo para entorno local.
- No subas datos reales de participantes ni contraseñas al repositorio.
