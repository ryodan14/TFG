package com.example.ainaraaisitaldea.dao;

import com.example.ainaraaisitaldea.model.Nino;
import com.example.ainaraaisitaldea.database.ConnectionFactory;
import com.example.ainaraaisitaldea.model.Tutor;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NinoDAO {

    public boolean crear(Nino nino) {
        String sql = "INSERT INTO ninos (nombre, primer_apellido, segundo_apellido, fecha_nacimiento, " +
                "colegio_instituto, curso, alergias, observaciones, puede_salir_solo, " +
                "autorizacion_fotos, id_tutor, vinculo_tutor) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, nino.getNombre());
            stmt.setString(2, nino.getPrimerApellido());
            stmt.setString(3, nino.getSegundoApellido());
            stmt.setDate(4, Date.valueOf(nino.getFechaNacimiento()));
            stmt.setString(5, nino.getColegioInstituto());
            stmt.setString(6, nino.getCurso());
            stmt.setBoolean(7, nino.isTieneAlergias());
            stmt.setString(8, nino.getObservaciones());
            stmt.setBoolean(9, nino.isPuedeSalirSolo());
            stmt.setBoolean(10, nino.isAutorizacionFotos());
            stmt.setInt(11, nino.getIdTutor());
            stmt.setString(12, nino.getVinculoTutor().name());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        nino.setIdNino(rs.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al crear niño: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public List<Nino> listar() {
        List<Nino> ninos = new ArrayList<>();
        String sql = "SELECT * FROM v_ninos_completo ORDER BY primer_apellido, nombre";

        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ninos.add(mapearNino(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar niños: " + e.getMessage());
            e.printStackTrace();
        }

        return ninos;
    }

    public List<Nino> listarConFiltros(List<String> tiposActividad, String curso, String busqueda) {
        List<Nino> ninos = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM v_ninos_completo WHERE 1=1"
        );

        List<Object> parametros = new ArrayList<>();

        // Filtro por curso
        if (curso != null && !curso.trim().isEmpty() && !"Todos".equals(curso)) {
            sql.append(" AND curso = ?");
            parametros.add(curso);
        }

        // Filtro por búsqueda (nombre, apellidos)
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            sql.append(" AND (nombre LIKE ? OR primer_apellido LIKE ? OR segundo_apellido LIKE ?)");
            String busquedaParam = "%" + busqueda + "%";
            parametros.add(busquedaParam);
            parametros.add(busquedaParam);
            parametros.add(busquedaParam);
        }

        // Filtro por tipo de actividad (según curso)
        if (tiposActividad != null && !tiposActividad.isEmpty() && !tiposActividad.contains("TODOS")) {
            List<String> condicionesCurso = new ArrayList<>();

            for (String tipo : tiposActividad) {
                if ("LUDOTEKA".equals(tipo)) {
                    // 1º a 5º Primaria (corregido)
                    condicionesCurso.add("curso IN ('1º Primaria', '2º Primaria', '3º Primaria', '4º Primaria', '5º Primaria')");
                } else if ("GAZTE_LOKALA".equals(tipo)) {
                    // 6º Primaria y 1º ESO
                    condicionesCurso.add("curso IN ('6º Primaria', '1º ESO')");
                } else if ("GAZTE_LOKALA_2".equals(tipo)) {
                    // 2º ESO en adelante
                    condicionesCurso.add("curso IN ('2º ESO', '3º ESO', '4º ESO')");
                }
            }

            if (!condicionesCurso.isEmpty()) {
                sql.append(" AND (").append(String.join(" OR ", condicionesCurso)).append(")");
            }
        }

        sql.append(" ORDER BY primer_apellido, nombre");

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ninos.add(mapearNino(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al listar niños con filtros: " + e.getMessage());
            e.printStackTrace();
        }

        return ninos;
    }

    public Nino obtenerPorId(int idNino) {
        String sql = "SELECT * FROM v_ninos_completo WHERE id_nino = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idNino);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Nino nino = mapearNino(rs);

                    // Cargar tutor completo
                    if (nino.getIdTutor() != null) {
                        TutorDAO tutorDAO = new TutorDAO();
                        Tutor tutor = tutorDAO.obtenerPorId(nino.getIdTutor());
                        nino.setTutor(tutor);
                    }

                    return nino;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener niño: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public boolean actualizar(Nino nino) {
        String sql = "UPDATE ninos SET nombre = ?, primer_apellido = ?, segundo_apellido = ?, " +
                "fecha_nacimiento = ?, colegio_instituto = ?, curso = ?, alergias = ?, " +
                "observaciones = ?, puede_salir_solo = ?, autorizacion_fotos = ?, " +
                "vinculo_tutor = ? WHERE id_nino = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nino.getNombre());
            stmt.setString(2, nino.getPrimerApellido());
            stmt.setString(3, nino.getSegundoApellido());
            stmt.setDate(4, Date.valueOf(nino.getFechaNacimiento()));
            stmt.setString(5, nino.getColegioInstituto());
            stmt.setString(6, nino.getCurso());
            stmt.setBoolean(7, nino.isTieneAlergias());
            stmt.setString(8, nino.getObservaciones());
            stmt.setBoolean(9, nino.isPuedeSalirSolo());
            stmt.setBoolean(10, nino.isAutorizacionFotos());
            stmt.setString(11, nino.getVinculoTutor().name());
            stmt.setInt(12, nino.getIdNino());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar niño: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public boolean eliminar(int idNino) {
        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            // 1. Obtener el id_tutor del niño antes de eliminarlo
            String sqlGetTutor = "SELECT id_tutor FROM ninos WHERE id_nino = ?";
            int idTutor = 0;

            try (PreparedStatement stmtGet = conn.prepareStatement(sqlGetTutor)) {
                stmtGet.setInt(1, idNino);
                try (ResultSet rs = stmtGet.executeQuery()) {
                    if (rs.next()) {
                        idTutor = rs.getInt("id_tutor");
                    }
                }
            }

            // 2. Eliminar el niño
            String sqlDeleteNino = "DELETE FROM ninos WHERE id_nino = ?";
            try (PreparedStatement stmtDelete = conn.prepareStatement(sqlDeleteNino)) {
                stmtDelete.setInt(1, idNino);
                int filasAfectadas = stmtDelete.executeUpdate();

                if (filasAfectadas == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // 3. Verificar si el tutor tiene más hijos
            String sqlCountHijos = "SELECT COUNT(*) as total FROM ninos WHERE id_tutor = ?";
            int totalHijos = 0;

            try (PreparedStatement stmtCount = conn.prepareStatement(sqlCountHijos)) {
                stmtCount.setInt(1, idTutor);
                try (ResultSet rs = stmtCount.executeQuery()) {
                    if (rs.next()) {
                        totalHijos = rs.getInt("total");
                    }
                }
            }

            // 4. Si el tutor no tiene más hijos, eliminarlo
            if (totalHijos == 0 && idTutor > 0) {
                String sqlDeleteTutor = "DELETE FROM tutores WHERE id_tutor = ?";
                try (PreparedStatement stmtDeleteTutor = conn.prepareStatement(sqlDeleteTutor)) {
                    stmtDeleteTutor.setInt(1, idTutor);
                    stmtDeleteTutor.executeUpdate();
                    System.out.println("✓ Tutor " + idTutor + " eliminado (sin hijos)");
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error en rollback: " + ex.getMessage());
                }
            }
            System.err.println("Error al eliminar niño: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error al cerrar conexión: " + e.getMessage());
                }
            }
        }
    }

    private Nino mapearNino(ResultSet rs) throws SQLException {
        Nino nino = new Nino();

        nino.setIdNino(rs.getInt("id_nino"));
        nino.setNombre(rs.getString("nombre"));
        nino.setPrimerApellido(rs.getString("primer_apellido"));
        nino.setSegundoApellido(rs.getString("segundo_apellido"));
        nino.setFechaNacimiento(rs.getDate("fecha_nacimiento").toLocalDate());
        nino.setColegioInstituto(rs.getString("colegio_instituto"));
        nino.setCurso(rs.getString("curso"));
        nino.setTieneAlergias(rs.getBoolean("alergias"));
        nino.setObservaciones(rs.getString("observaciones"));
        nino.setPuedeSalirSolo(rs.getBoolean("puede_salir_solo"));
        nino.setAutorizacionFotos(rs.getBoolean("autorizacion_fotos"));
        nino.setIdTutor(rs.getInt("id_tutor"));
        nino.setVinculoTutor(Nino.VinculoTutor.valueOf(rs.getString("vinculo_tutor")));

        // Datos del tutor para visualización
        nino.setNombreCompletoTutor(rs.getString("nombre_completo_tutor"));
        nino.setTelefonoTutor(rs.getString("telefono_tutor"));
        nino.setCorreoTutor(rs.getString("correo_tutor"));
        nino.setDniTutor(rs.getString("dni_tutor"));

        return nino;
    }

    public boolean actualizarNinoYTutor(Nino nino, Tutor tutor) {
        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            // Actualizar tutor
            TutorDAO tutorDAO = new TutorDAO();
            boolean tutorActualizado = tutorDAO.actualizar(tutor);

            if (!tutorActualizado) {
                conn.rollback();
                return false;
            }

            // Actualizar niño
            NinoDAO ninoDAO = new NinoDAO();
            boolean ninoActualizado = ninoDAO.actualizar(nino);

            if (!ninoActualizado) {
                conn.rollback();
                return false;
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error al hacer rollback: " + ex.getMessage());
                }
            }
            System.err.println("Error al actualizar niño y tutor: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error al cerrar conexión: " + e.getMessage());
                }
            }
        }
    }
    /**
     * Elimina TODOS los niños de la base de datos
     */
    public int eliminarTodos() {
        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            // 1. Eliminar todos los niños
            String sqlDeleteNinos = "DELETE FROM ninos";
            int ninosEliminados = 0;
            try (Statement stmt = conn.createStatement()) {
                ninosEliminados = stmt.executeUpdate(sqlDeleteNinos);
                System.out.println("✓ Niños eliminados: " + ninosEliminados);
            }

            // 2. Eliminar todos los tutores (ya no tienen hijos)
            String sqlDeleteTutores = "DELETE FROM tutores";
            int tutoresEliminados = 0;
            try (Statement stmt = conn.createStatement()) {
                tutoresEliminados = stmt.executeUpdate(sqlDeleteTutores);
                System.out.println("✓ Tutores eliminados: " + tutoresEliminados);
            }

            conn.commit();

            System.out.println("=== ELIMINACIÓN TOTAL COMPLETADA ===");
            return ninosEliminados;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error en rollback: " + ex.getMessage());
                }
            }
            System.err.println("Error al eliminar todos: " + e.getMessage());
            e.printStackTrace();
            return 0;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error al cerrar conexión: " + e.getMessage());
                }
            }
        }
    }
}




