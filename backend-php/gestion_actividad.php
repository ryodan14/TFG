<?php
$hostname = 'localhost';
$database = 'gestion_asociacion'; 
$username = 'root';
$password = '';

$conexion = new mysqli($hostname, $username, $password, $database);
$conexion->set_charset("utf8");

$accion = $_POST['accion']; 

if ($accion == 'insertar') {
    $fecha = $_POST['fecha'];
    $nombre = $_POST['nombre'];
    $ingresos = $_POST['ingresos'];
    $gastos = $_POST['gastos'];
    $sql = "INSERT INTO actividades (fecha, nombre, tipo, dinero_recibido, dinero_pagado_monitores, estado) 
            VALUES ('$fecha', '$nombre', 'HINCHABLES', $ingresos, $gastos, 'REALIZADA')";
} 

if ($accion == 'editar') {
    $id = $_POST['id_actividad'];
    $fecha = $_POST['fecha'];
    $nombre = $_POST['nombre'];
    $ingresos = $_POST['ingresos'];
    $gastos = $_POST['gastos'];
    $sql = "UPDATE actividades SET fecha='$fecha', nombre='$nombre', 
            dinero_recibido=$ingresos, dinero_pagado_monitores=$gastos WHERE id_actividad=$id";
}

if ($accion == 'eliminar') {
    $id = $_POST['id_actividad'];
    $sql = "DELETE FROM actividades WHERE id_actividad=$id";
}

if ($conexion->query($sql)) {
    echo json_encode(["status" => "success"]);
} else {
    echo json_encode(["status" => "error", "message" => $conexion->error]);
}
?>s