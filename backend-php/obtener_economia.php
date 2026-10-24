<?php
$hostname = 'localhost';
$database = 'gestion_asociacion'; 
$username = 'root';
$password = '';

$conexion = new mysqli($hostname, $username, $password, $database);
if ($conexion->connect_errno) { die(json_encode(["error" => "Error de conexion"])); }
$conexion->set_charset("utf8");

$mes = isset($_GET['mes']) ? (int)$_GET['mes'] : 3;
$anio = isset($_GET['anio']) ? (int)$_GET['anio'] : 2025;

// 1. Resumen Mensual
$sqlResumen = "SELECT 
    COUNT(*) as total_actividades,
    COALESCE(SUM(dinero_recibido), 0) as total_ingresos,
    COALESCE(SUM(dinero_pagado_monitores), 0) as total_gastos,
    COALESCE(SUM(dinero_recibido - dinero_pagado_monitores), 0) as beneficio_neto
    FROM actividades 
    WHERE tipo = 'HINCHABLES' AND YEAR(fecha) = $anio AND MONTH(fecha) = $mes";

$resultResumen = $conexion->query($sqlResumen);
$resumen = $resultResumen->fetch_assoc();

// 2. Resumen Anual (NUEVO)
$sqlAnual = "SELECT 
    COUNT(*) as total_actividades_anual,
    COALESCE(SUM(dinero_recibido), 0) as total_ingresos_anual,
    COALESCE(SUM(dinero_pagado_monitores), 0) as total_gastos_anual,
    COALESCE(SUM(dinero_recibido - dinero_pagado_monitores), 0) as beneficio_anual
    FROM actividades 
    WHERE tipo = 'HINCHABLES' AND YEAR(fecha) = $anio";

$resultAnual = $conexion->query($sqlAnual);
$anual = $resultAnual->fetch_assoc();

// 3. Detalle de la tabla
$sqlDetalle = "SELECT 
    id_actividad, fecha, nombre as descripcion, dinero_recibido as monto, 'ingreso' as tipo 
    FROM actividades 
    WHERE tipo = 'HINCHABLES' AND YEAR(fecha) = $anio AND MONTH(fecha) = $mes";

$resultDetalle = $conexion->query($sqlDetalle);
$detalles = [];
while($row = $resultDetalle->fetch_assoc()) { $detalles[] = $row; }

header('Content-Type: application/json');
echo json_encode([
    "resumen" => [
        "salidas" => (int)$resumen['total_actividades'],
        "ingresos" => (float)$resumen['total_ingresos'],
        "gastos" => (float)$resumen['total_gastos'],
        "beneficio" => (float)$resumen['beneficio_neto']
    ],
    "anual" => [
        "salidas" => (int)$anual['total_actividades_anual'],
        "ingresos" => (float)$anual['total_ingresos_anual'],
        "gastos" => (float)$anual['total_gastos_anual'],
        "beneficio" => (float)$anual['beneficio_anual']
    ],
    "detalles" => $detalles
]);
?>