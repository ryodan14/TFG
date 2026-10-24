<?php
ini_set('display_errors', 1);
error_reporting(E_ALL);

// Configuración de la base de datos
$server = "localhost";
$user = "root";
$pass = "";
$db = "gestion_asociacion";

// 2. Intentar conectar
$con = mysqli_connect($server, $user, $pass, $db);

// 3. Comprobar si la conexión falló
if (!$con) {
    $errorArray = array();
    $errorArray[] = array("error" => "Fallo al conectar: " . mysqli_connect_error());
    echo json_encode($errorArray);
    exit();
}

// 4. Configurar caracteres UTF-8
mysqli_set_charset($con, "utf8");

// 5. La consulta SQL FILTRADA
// Explicación del filtro:
// - MONTH(fecha) = MONTH(CURRENT_DATE()): Solo el mes actual.
// - YEAR(fecha) = YEAR(CURRENT_DATE()): Solo el año actual.
// - fecha >= CURRENT_DATE(): Solo actividades de hoy en adelante (no pasadas).
$sql = "SELECT * FROM actividades 
        WHERE MONTH(fecha) = MONTH(CURRENT_DATE()) 
        AND YEAR(fecha) = YEAR(CURRENT_DATE()) 
        AND fecha >= CURRENT_DATE() 
        ORDER BY fecha ASC, hora_inicio ASC";

$result = mysqli_query($con, $sql);

$response = array();

if ($result) {
    while($row = mysqli_fetch_assoc($result)){
        $response[] = $row;
    }
} else {
    $response[] = array("error" => "Error en la consulta SQL");
}

// 6. Devolver el resultado final en JSON
header('Content-Type: application/json; charset=utf-8');
echo json_encode($response, JSON_UNESCAPED_UNICODE);
mysqli_close($con);
?>