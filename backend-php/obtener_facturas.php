<?php
header('Content-Type: application/json; charset=utf-8');
$conn = new mysqli("localhost", "root", "", "gestion_asociacion");

$tipo = isset($_GET['tipo']) ? $_GET['tipo'] : 'Todos';
$estado = isset($_GET['estado']) ? $_GET['estado'] : 'Todos';

// Consulta base con alias para que coincida con el modelo Kotlin
$sql = "SELECT id_factura as id, numero_factura as numero, tipo, cliente, 
               fecha_emision as fecha, total, estado FROM facturas WHERE 1=1";

if (strcasecmp($tipo, 'Todos') != 0) {
    $sql .= " AND tipo = '" . strtoupper($tipo) . "'";
}
if (strcasecmp($estado, 'Todos') != 0) {
    $sql .= " AND estado = '" . strtoupper($estado) . "'";
}

$result = $conn->query($sql);
$datos = array();
while($row = $result->fetch_assoc()) {
    $datos[] = $row;
}

echo json_encode($datos);
$conn->close();
?>