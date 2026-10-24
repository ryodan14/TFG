<?php
$conn = new mysqli("localhost", "root", "", "gestion_asociacion");

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $tipo = $_POST['tipo'];
    $concepto = $conn->real_escape_string($_POST['concepto']);
    $cantidad = $_POST['cantidad'];
    $obs = $conn->real_escape_string($_POST['observaciones']);
    $fecha = date("Y-m-d");

    $sql = "INSERT INTO movimientos_economicos (fecha, tipo, concepto, cantidad, observaciones) 
            VALUES ('$fecha', '$tipo', '$concepto', '$cantidad', '$obs')";

    if ($conn->query($sql) === TRUE) {
        echo "Éxito";
    } else {
        echo "Error: " . $conn->error;
    }
}
$conn->close();
?>