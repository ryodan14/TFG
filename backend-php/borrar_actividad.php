<?php
$server = "localhost"; $user = "root"; $pass = "";$db = "gestion_asociacion";
;
$con = mysqli_connect($server, $user, $pass, $db);

if (!$con) {
    die("Error de conexión");
}

if (isset($_POST['id_actividad'])) {
    $id = $_POST['id_actividad'];
    $sql = "DELETE FROM actividades WHERE id_actividad = '$id'";
    
    if (mysqli_query($con, $sql)) {
        echo "Eliminado correctamente";
    } else {
        http_response_code(500);
        echo "Error al eliminar";
    }
}
mysqli_close($con);
?>