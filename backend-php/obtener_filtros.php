<?php
include 'conexion.php';

$response = array();

// Cursos existentes
$resCursos = $conexion->query("SELECT DISTINCT curso FROM ninos WHERE curso IS NOT NULL AND curso != '' ORDER BY curso ASC");
$cursos = array("Todos");
while($r = $resCursos->fetch_assoc()) {
    $cursos[] = $r['curso'];
}

// Centros existentes
$resCentros = $conexion->query("SELECT DISTINCT colegio_instituto FROM ninos WHERE colegio_instituto IS NOT NULL AND colegio_instituto != '' ORDER BY colegio_instituto ASC");
$centros = array("Todos");
while($r = $resCentros->fetch_assoc()) {
    $centros[] = $r['colegio_instituto'];
}

$response['cursos'] = $cursos;
$response['centros'] = $centros;

header('Content-Type: application/json');
echo json_encode($response);
?>