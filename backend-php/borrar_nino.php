<?php
header('Content-Type: application/json; charset=utf-8');
$conn = new mysqli("localhost", "root", "", "gestion_asociacion");

$datos = json_decode(file_get_contents("php://input"));

if($datos) {
    $sql = "DELETE FROM ninos WHERE id_nino='$datos->id'";
    if ($conn->query($sql) === TRUE) {
        echo json_encode(["mensaje" => "Niño borrado"]);
    } else {
        echo json_encode(["error" => "Error: " . $conn->error]);
    }
}
$conn->close();
?>