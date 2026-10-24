<?php
$conn = new mysqli("localhost", "root", "", "gestion_asociacion");

if (isset($_GET['id'])) {
    $id = $_GET['id'];
    $sql = "DELETE FROM facturas WHERE id_factura = '$id'";
    
    if ($conn->query($sql) === TRUE) {
        echo "Éxito";
    } else {
        echo "Error";
    }
}
$conn->close();
?>