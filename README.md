# TFG – App móvil de gestión de la asociación Ainara Aisi Taldea

Aplicación Android para gestionar una asociación: actividades y calendario, participantes, hinchables, contabilidad, facturas y usuarios. La app se comunica con un backend en PHP que accede a una base de datos MySQL (`gestion_asociacion`).

```
.
├── android-app/   App Android (Kotlin)
└── backend-php/   API en PHP (JSON sobre HTTP) + MySQL
```

## Requisitos

- Android Studio (compileSdk 36, minSdk 30)
- Apache + PHP + MySQL/MariaDB (p. ej. XAMPP)

## Puesta en marcha

1. **Base de datos:** crea la base `gestion_asociacion` e importa el esquema de tablas
   (añádelo en `database/schema.sql`; no está incluido en este repositorio).
2. **Backend:** copia el contenido de `backend-php/` a la carpeta `TFG` de tu servidor web
   (p. ej. `C:\xampp\htdocs\TFG\`). Las credenciales de MySQL están en `conexion.php`
   (por defecto `root` sin contraseña, solo para desarrollo).
3. **App:** abre `android-app/` en Android Studio y ejecútala en el emulador.
   La app llama a `http://10.0.2.2/TFG/...`, que es el `localhost` del PC visto desde el emulador.
   En un móvil físico, cambia esa dirección por la IP de tu servidor.

## Notas

- Proyecto de TFG: las credenciales por defecto son solo para entorno local.
- No subas datos reales de participantes ni contraseñas al repositorio.
