<?php
header('Content-Type: application/json; charset=utf-8');
$conn = new mysqli("localhost", "root", "", "gestion_asociacion");
$conn->set_charset("utf8");

// Recibimos los datos de Android
$datos = json_decode(file_get_contents("php://input"));

if($datos) {
    $sql = "INSERT INTO ninos (nombre, primer_apellido, segundo_apellido, fecha_nacimiento, colegio_instituto, curso, alergias, puede_salir_solo, autorizacion_fotos, id_tutor, vinculo_tutor, observaciones) 
            VALUES ('$datos->nombre', '$datos->apellido1', '$datos->apellido2', '$datos->fecha_nacimiento', '$datos->colegio', '$datos->curso', '$datos->alergias', '$datos->sale_solo', '$datos->fotos', '$datos->id_tutor', '$datos->vinculo_tutor', '$datos->observaciones')";
    
    if ($conn->query($sql) === TRUE) {
        echo json_encode(["mensaje" => "Niño añadido correctamente"]);
    } else {
        echo json_encode(["error" => "Error: " . $conn->error]);
    }
}
$conn->close();
?>