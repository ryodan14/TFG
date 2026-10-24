<?php
header('Content-Type: application/json; charset=utf-8');
$conn = new mysqli("localhost", "root", "", "gestion_asociacion");

if ($conn->connect_error) { 
    die(json_encode(["error" => "Conexion Fallida"])); 
}

// Totales
$sqlTotales = "SELECT 
    SUM(CASE WHEN tipo = 'INGRESO' THEN cantidad ELSE 0 END) as ingresos,
    SUM(CASE WHEN tipo = 'GASTO' THEN cantidad ELSE 0 END) as gastos
    FROM movimientos_economicos";

$resTotales = $conn->query($sqlTotales);
$totales = $resTotales->fetch_assoc();

$ing = $totales['ingresos'] ?? 0;
$gas = $totales['gastos'] ?? 0;
$ben = $ing - $gas;

// Lista
$resLista = $conn->query("SELECT * FROM movimientos_economicos ORDER BY fecha DESC LIMIT 30");
$movs = [];
while($row = $resLista->fetch_assoc()) {
    $movs[] = $row;
}

echo json_encode([
    "ingresos" => number_format((float)$ing, 2, '.', ''),
    "gastos" => number_format((float)$gas, 2, '.', ''),
    "beneficio" => number_format((float)$ben, 2, '.', ''),
    "lista" => $movs
]);

$conn->close();
?>