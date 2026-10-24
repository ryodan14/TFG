<?php
header('Content-Type: application/json; charset=utf-8');

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "gestion_asociacion";

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die(json_encode(["error" => "Conexión fallida"]));
}

$conn->set_charset("utf8");

// Usamos ALIAS (AS) para que las claves del JSON coincidan con el Kotlin
$sql = "SELECT 
            id_nino AS id,
            nombre,
            primer_apellido AS apellido1,
            segundo_apellido AS apellido2,
            fecha_nacimiento,
            colegio_instituto,
            curso,
            alergias,
            puede_salir_solo AS sale_solo,
            autorizacion_fotos AS fotos,
            id_tutor,
            vinculo_tutor,
            observaciones
        FROM ninos";

$result = $conn->query($sql);
$participantes = array();

if ($result && $result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        $row['apellido2'] = $row['apellido2'] ?? "";
        $row['observaciones'] = $row['observaciones'] ?? "";

        // Aseguramos que sean enteros para el optInt de Android
        $row['alergias'] = (int)$row['alergias'];
        $row['sale_solo'] = (int)$row['sale_solo'];
        $row['fotos'] = (int)$row['fotos'];

        $participantes[] = $row;
    }
}

echo json_encode($participantes, JSON_UNESCAPED_UNICODE);
$conn->close();
?>