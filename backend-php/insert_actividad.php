<?php
$server = "localhost";
$user = "root";
$pass = "";
$db = "gestion_asociacion";

$con = mysqli_connect($server, $user, $pass, $db);

// Recibimos los datos del Kotlin (usando los nombres de los params)
$nombre = $_POST['nombre'];
$tipo = $_POST['tipo'];
$fecha = $_POST['fecha'];
$hora_inicio = $_POST['hora_inicio'];
$hora_fin = $_POST['hora_fin'];
$monitores = $_POST['monitores']; // Este viene del campo etMonitores
$observaciones = $_POST['observaciones'];

// SQL ajustado a tu captura de pantalla (id_actividad es AUTO_INCREMENT)
$sql = "INSERT INTO actividades (fecha, hora_inicio, hora_fin, tipo, nombre, descripcion, observaciones, estado) 
        VALUES ('$fecha', '$hora_inicio', '$hora_fin', '$tipo', '$nombre', '$monitores', '$observaciones', 'PLANIFICADA')";

if (mysqli_query($con, $sql)) {
    echo "success";
} else {
    header("HTTP/1.1 500 Internal Server Error");
    echo "Error: " . mysqli_error($con);
}

mysqli_close($con);
?>