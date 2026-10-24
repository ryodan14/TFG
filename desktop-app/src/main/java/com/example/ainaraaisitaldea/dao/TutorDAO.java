package com.example.ainaraaisitaldea.dao;

import com.example.ainaraaisitaldea.model.Tutor;
import com.example.ainaraaisitaldea.database.ConnectionFactory;

import java.sql.*;

public class TutorDAO {

    public boolean crear(Tutor tutor) {
        String sql = "INSERT INTO tutores (nombre, primer_apellido, segundo_apellido, dni, " +
                "telefono, correo, direccion, municipio, codigo_postal, observaciones) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, tutor.getNombre());
            stmt.setString(2, tutor.getPrimerApellido());
            stmt.setString(3, tutor.getSegundoApellido());
            stmt.setString(4, tutor.getDni().toUpperCase());
            stmt.setString(5, tutor.getTelefono());
            stmt.setString(6, tutor.getCorreo());
            stmt.setString(7, tutor.getDireccion());
            stmt.setString(8, tutor.getMunicipio());
            stmt.setString(9, tutor.getCodigoPostal());
            stmt.setString(10, tutor.getObservaciones());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        tutor.setIdTutor(rs.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al crear tutor: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public Tutor buscarPorDni(String dni) {
        String sql = "SELECT * FROM tutores WHERE UPPER(TRIM(dni)) = UPPER(TRIM(?))";

        System.out.println("🔍 TutorDAO.buscarPorDni: Buscando DNI '" + dni + "'");

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dni);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Tutor tutor = mapearTutor(rs);
                    System.out.println("✅ Tutor encontrado: ID=" + tutor.getIdTutor() + ", Nombre=" + tutor.getNombreCompleto());
                    return tutor;
                } else {
                    System.out.println("❌ No se encontró tutor con DNI '" + dni + "'");
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al buscar tutor por DNI: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Busca un tutor por su ID
     */
    public Tutor obtenerPorId(int idTutor) {
        String sql = "SELECT * FROM tutores WHERE id_tutor = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idTutor);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearTutor(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener tutor por ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public boolean actualizar(Tutor tutor) {
        String sql = "UPDATE tutores SET nombre = ?, primer_apellido = ?, segundo_apellido = ?, " +
                "dni = ?, telefono = ?, correo = ?, direccion = ?, municipio = ?, " +
                "codigo_postal = ?, observaciones = ? WHERE id_tutor = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tutor.getNombre());
            stmt.setString(2, tutor.getPrimerApellido());
            stmt.setString(3, tutor.getSegundoApellido());
            stmt.setString(4, tutor.getDni().toUpperCase());
            stmt.setString(5, tutor.getTelefono());
            stmt.setString(6, tutor.getCorreo());
            stmt.setString(7, tutor.getDireccion());
            stmt.setString(8, tutor.getMunicipio());
            stmt.setString(9, tutor.getCodigoPostal());
            stmt.setString(10, tutor.getObservaciones());
            stmt.setInt(11, tutor.getIdTutor());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar tutor: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
    private Tutor mapearTutor(ResultSet rs) throws SQLException {
        Tutor tutor = new Tutor();
        tutor.setIdTutor(rs.getInt("id_tutor"));
        tutor.setNombre(rs.getString("nombre"));
        tutor.setPrimerApellido(rs.getString("primer_apellido"));
        tutor.setSegundoApellido(rs.getString("segundo_apellido"));
        tutor.setDni(rs.getString("dni"));
        tutor.setTelefono(rs.getString("telefono"));
        tutor.setCorreo(rs.getString("correo"));
        tutor.setDireccion(rs.getString("direccion"));
        tutor.setMunicipio(rs.getString("municipio"));
        tutor.setCodigoPostal(rs.getString("codigo_postal"));
        tutor.setObservaciones(rs.getString("observaciones"));
        return tutor;
    }

}