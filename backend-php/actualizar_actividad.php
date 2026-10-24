<?php
$host = "localhost"; $user = "root"; $pass = ""; $db = "gestion_asociacion";
;
$conexion = new mysqli($host, $user, $pass, $db);
$conexion->set_charset("utf8");

if (isset($_POST['id_actividad'])) {
    $id = $conexion->real_escape_string($_POST['id_actividad']);
    $nombre = $conexion->real_escape_string($_POST['nombre']);
    $tipo = $conexion->real_escape_string($_POST['tipo']);
    $fecha = $conexion->real_escape_string($_POST['fecha']);
    $hora_inicio = $conexion->real_escape_string($_POST['hora_inicio']);
    $hora_fin = $conexion->real_escape_string($_POST['hora_fin']);
    $monitores = $conexion->real_escape_string($_POST['monitores']);
    $observaciones = $conexion->real_escape_string($_POST['observaciones']);

    $sql = "UPDATE actividades SET nombre='$nombre', tipo='$tipo', fecha='$fecha', 
            hora_inicio='$hora_inicio', hora_fin='$hora_fin', descripcion='$monitores', 
            observaciones='$observaciones' WHERE id_actividad='$id'";

    if ($conexion->query($sql) === TRUE) {
        echo "Exito";
    } else {
        echo "Error: " . $conexion->error;
    }
}
$conexion->close();
?>