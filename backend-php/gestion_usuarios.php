<?php
include 'conexion.php';
$accion = $_REQUEST['accion'] ?? '';
$id = $_POST['id_usuario'] ?? '';

switch($accion) {
    case 'obtener':
        $res = mysqli_query($conexion, "SELECT * FROM usuarios");
        $users = [];
        while($row = mysqli_fetch_assoc($res)) $users[] = $row;
        echo json_encode(["usuarios" => $users]);
        break;
	case 'insertar':
		$u = $_POST['username'];
		$n = $_POST['nombre_completo'];
		$e = $_POST['email'];
		$r = $_POST['rol']; // Añadido para el formulario
		$sql = "INSERT INTO usuarios (username, password, nombre_completo, email, rol, activo, fecha_creacion) 
				VALUES ('$u','1234','$n','$e','$r',1,NOW())";
		echo mysqli_query($conexion, $sql) ? "Exito" : "Error";
		break;
    case 'actualizar':
        $u = $_POST['username']; $n = $_POST['nombre_completo'];
        $sql = "UPDATE usuarios SET username='$u', nombre_completo='$n' WHERE id_usuario='$id'";
        echo mysqli_query($conexion, $sql) ? "Exito" : "Error";
        break;
    case 'desactivar':
        echo mysqli_query($conexion, "UPDATE usuarios SET activo=0 WHERE id_usuario='$id'") ? "Exito" : "Error";
        break;
    case 'resetear':
        echo mysqli_query($conexion, "UPDATE usuarios SET password='1234' WHERE id_usuario='$id'") ? "Exito" : "Error";
        break;
}
?>