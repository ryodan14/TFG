<?php
// insertar_factura.php
$conn = new mysqli("localhost", "root", "", "gestion_asociacion");

if ($conn->connect_error) {
    die("Error de conexión: " . $conn->connect_error);
}

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $numero   = $conn->real_escape_string($_POST['numero_factura']);
    $cliente  = $conn->real_escape_string($_POST['cliente']);
    $concepto = $conn->real_escape_string($_POST['concepto']);
    $base     = $_POST['base_imponible'];
    $iva_p    = $_POST['iva_porcentaje'];
    $tipo     = $_POST['tipo']; // Aquí llega "FACTURA" o "PRESUPUESTO"
    $estado   = $_POST['estado'];

    // Calculamos totales
    $iva_cant = ($base * $iva_p) / 100;
    $total    = $base + $iva_cant;
    $fecha    = date("Y-m-d");

    $sql = "INSERT INTO facturas (numero_factura, cliente, concepto, base_imponible, iva_porcentaje, iva_cantidad, total, tipo, estado, fecha_emision) 
            VALUES ('$numero', '$cliente', '$concepto', '$base', '$iva_p', '$iva_cant', '$total', '$tipo', '$estado', '$fecha')";

    if ($conn->query($sql) === TRUE) {
        echo "Éxito";
    } else {
        echo "Error: " . $conn->error;
    }
}

$conn->close();
?>