<?php
header('Content-Type: application/json; charset=utf-8');
$conn = new mysqli("localhost", "root", "", "gestion_asociacion");
$conn->set_charset("utf8");

$datos = json_decode(file_get_contents("php://input"));

if($datos) {
    $sql = "UPDATE ninos SET 
            nombre='$datos->nombre', primer_apellido='$datos->apellido1', segundo_apellido='$datos->apellido2', 
            fecha_nacimiento='$datos->fecha_nacimiento', colegio_instituto='$datos->colegio', curso='$datos->curso', 
            alergias='$datos->alergias', puede_salir_solo='$datos->sale_solo', autorizacion_fotos='$datos->fotos', 
            id_tutor='$datos->id_tutor', vinculo_tutor='$datos->vinculo_tutor', observaciones='$datos->observaciones' 
            WHERE id_nino='$datos->id'";
    
    if ($conn->query($sql) === TRUE) {
        echo json_encode(["mensaje" => "Niño actualizado"]);
    } else {
        echo json_encode(["error" => "Error: " . $conn->error]);
    }
}
$conn->close();
?>