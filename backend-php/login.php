<?php
// 1. Ocultar warnings para que NO rompan la app Android
error_reporting(0);
ini_set('display_errors', 0);

// 2. Decirle al navegador/Android que esto es JSON puro
header('Content-Type: application/json; charset=utf-8');

// Conexión
$conn = new mysqli("localhost", "root", "", "gestion_asociacion");

if ($conn->connect_error) {
    echo json_encode(array("success" => false, "message" => "Error DB"));
    exit();
}

// 3. Recibir datos (Usamos 'usuario' para coincidir con tu Kotlin anterior o lo cambiamos)
// IMPORTANTE: Aquí vamos a esperar 'username' para coincidir con tu base de datos
$user = isset($_POST['username']) ? $_POST['username'] : '';
$pass = isset($_POST['password']) ? $_POST['password'] : '';

// 4. Consulta segura (evita fallos con comillas raras)
$stmt = $conn->prepare("SELECT * FROM usuarios WHERE username = ? AND password = ?");
$stmt->bind_param("ss", $user, $pass);
$stmt->execute();
$result = $stmt->get_result();
if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    
    // AQUÍ ESTÁ LA CLAVE: Añadimos "email" => $row['email']
    echo json_encode(array(
        "success" => true, 
        "nombre" => $row['nombre_completo'], 
        "email" => $row['email'] // <--- ¡ESTO ES LO QUE FALTABA!
    ));
} else {
    echo json_encode(array("success" => false, "message" => "Datos incorrectos"));
}

$stmt->close();
$conn->close();
?>