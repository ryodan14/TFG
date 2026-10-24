<?php
include 'conexion.php';

$accion = $_POST['accion'];
$id = $_POST['id'];
$tipo = $_POST['tipo'];
$concepto = $_POST['concepto'];
$cantidad = $_POST['cantidad'];
$obs = $_POST['observaciones'];

if ($accion == "insertar") {
    $sql = "INSERT INTO movimientos (tipo, concepto, cantidad, observaciones, fecha) 
            VALUES ('$tipo', '$concepto', '$cantidad', '$obs', NOW())";
} else if ($accion == "actualizar") {
    $sql = "UPDATE movimientos SET concepto='$concepto', cantidad='$cantidad', observaciones='$obs' 
            WHERE id_movimiento='$id'";
} else if ($accion == "eliminar") {
    $sql = "DELETE FROM movimientos WHERE id_movimiento='$id'";
}

if (mysqli_query($conexion, $sql)) {
    echo "Éxito";
} else {
    echo "Error: " . mysqli_error($conexion);
}
?>