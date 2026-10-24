<?php
// editar_factura.php
$conn = new mysqli("localhost", "root", "", "gestion_asociacion");

if ($conn->connect_error) {
    die("Error de conexión: " . $conn->connect_error);
}

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    // Recogemos el ID obligatorio para saber qué fila editar
    // Usamos real_escape_string por seguridad contra comillas simples
    $id       = $conn->real_escape_string($_POST['id_factura']); 
    $numero   = $conn->real_escape_string($_POST['numero_factura']);
    $cliente  = $conn->real_escape_string($_POST['cliente']);
    $concepto = $conn->real_escape_string($_POST['concepto']);
    $base     = $_POST['base_imponible'];
    $iva_p    = $_POST['iva_porcentaje'];
    $estado   = $conn->real_escape_string($_POST['estado']); // <--- NUEVO

    // Recalculamos totales
    $iva_cant = ($base * $iva_p) / 100;
    $total    = $base + $iva_cant;

    // Sentencia SQL de actualización incluyendo el ESTADO
    $sql = "UPDATE facturas SET 
                numero_factura = '$numero', 
                cliente = '$cliente', 
                concepto = '$concepto', 
                base_imponible = '$base', 
                iva_porcentaje = '$iva_p', 
                iva_cantidad = '$iva_cant', 
                total = '$total', 
                estado = '$estado' 
            WHERE id_factura = '$id'";

    if ($conn->query($sql) === TRUE) {
        echo "Éxito";
    } else {
        echo "Error: " . $conn->error;
    }
}

$conn->close();
?>