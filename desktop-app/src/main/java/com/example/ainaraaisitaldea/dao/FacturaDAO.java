package com.example.ainaraaisitaldea.dao;

import com.example.ainaraaisitaldea.database.ConnectionFactory;
import com.example.ainaraaisitaldea.model.Factura;
import com.example.ainaraaisitaldea.model.enums.EstadoFactura;
import com.example.ainaraaisitaldea.model.enums.TipoDocumento;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FacturaDAO {

    private final ConnectionFactory connectionFactory;

    public FacturaDAO() {
        this.connectionFactory = ConnectionFactory.getInstance();
    }

    /**
     * Crear nueva factura/presupuesto
     */
    public Factura crear(Factura factura) throws SQLException {
        String sql = "INSERT INTO facturas (numero_factura, cliente, nif_cliente, direccion_cliente, " +
                "fecha_emision, fecha_vencimiento, concepto, base_imponible, iva_porcentaje, " +
                "iva_cantidad, total, estado, tipo, observaciones) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, factura.getNumeroFactura());
            stmt.setString(2, factura.getCliente());
            stmt.setString(3, factura.getNifCliente());
            stmt.setString(4, factura.getDireccionCliente());
            stmt.setDate(5, Date.valueOf(factura.getFechaEmision()));
            stmt.setDate(6, factura.getFechaVencimiento() != null ?
                    Date.valueOf(factura.getFechaVencimiento()) : null);
            stmt.setString(7, factura.getConcepto());
            stmt.setDouble(8, factura.getBaseImponible());
            stmt.setDouble(9, factura.getIvaPorcentaje());
            stmt.setDouble(10, factura.getIvaCantidad());
            stmt.setDouble(11, factura.getTotal());
            stmt.setString(12, factura.getEstado().name());
            stmt.setString(13, factura.getTipo().name());
            stmt.setString(14, factura.getObservaciones());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    factura.setIdFactura(rs.getInt(1));
                }
            }

            return factura;
        }
    }

    /**
     * Actualizar factura
     */
    public void actualizar(Factura factura) throws SQLException {
        String sql = "UPDATE facturas SET cliente = ?, nif_cliente = ?, direccion_cliente = ?, " +
                "fecha_emision = ?, fecha_vencimiento = ?, concepto = ?, base_imponible = ?, " +
                "iva_porcentaje = ?, iva_cantidad = ?, total = ?, estado = ?, " +
                "observaciones = ? WHERE id_factura = ?";

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, factura.getCliente());
            stmt.setString(2, factura.getNifCliente());
            stmt.setString(3, factura.getDireccionCliente());
            stmt.setDate(4, Date.valueOf(factura.getFechaEmision()));
            stmt.setDate(5, factura.getFechaVencimiento() != null ?
                    Date.valueOf(factura.getFechaVencimiento()) : null);
            stmt.setString(6, factura.getConcepto());
            stmt.setDouble(7, factura.getBaseImponible());
            stmt.setDouble(8, factura.getIvaPorcentaje());
            stmt.setDouble(9, factura.getIvaCantidad());
            stmt.setDouble(10, factura.getTotal());
            stmt.setString(11, factura.getEstado().name());
            stmt.setString(12, factura.getObservaciones());
            stmt.setInt(13, factura.getIdFactura());

            stmt.executeUpdate();
        }
    }

    /**
     * Listar por tipo
     */
    public List<Factura> listarPorTipo(TipoDocumento tipo) throws SQLException {
        String sql = "SELECT * FROM facturas WHERE tipo = ? ORDER BY fecha_emision DESC";
        List<Factura> facturas = new ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tipo.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    facturas.add(mapearFactura(rs));
                }
            }
        }

        return facturas;
    }

    /**
     * Listar todas
     */
    public List<Factura> listarTodas() throws SQLException {
        String sql = "SELECT * FROM facturas ORDER BY fecha_emision DESC";
        List<Factura> facturas = new ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                facturas.add(mapearFactura(rs));
            }
        }

        return facturas;
    }

    /**
     * Obtener por ID
     */
    public Factura obtenerPorId(Integer idFactura) throws SQLException {
        String sql = "SELECT * FROM facturas WHERE id_factura = ?";

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idFactura);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearFactura(rs);
                }
            }
        }
        return null;
    }

    /**
     * Eliminar factura
     */
    public void eliminar(Integer idFactura) throws SQLException {
        String sql = "DELETE FROM facturas WHERE id_factura = ?";

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idFactura);
            stmt.executeUpdate();
        }
    }

    /**
     * Generar número de factura automático
     */
    public String generarNumeroFactura(TipoDocumento tipo) throws SQLException {
        String sql = "{CALL sp_generar_numero_factura(?, ?)}";

        try (Connection conn = connectionFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, tipo.name());
            stmt.registerOutParameter(2, Types.VARCHAR);

            stmt.execute();

            return stmt.getString(2);
        }
    }

    private Factura mapearFactura(ResultSet rs) throws SQLException {
        Factura factura = new Factura();
        factura.setIdFactura(rs.getInt("id_factura"));
        factura.setNumeroFactura(rs.getString("numero_factura"));
        factura.setCliente(rs.getString("cliente"));
        factura.setNifCliente(rs.getString("nif_cliente"));
        factura.setDireccionCliente(rs.getString("direccion_cliente"));
        factura.setFechaEmision(rs.getDate("fecha_emision").toLocalDate());

        Date fechaVenc = rs.getDate("fecha_vencimiento");
        if (fechaVenc != null) {
            factura.setFechaVencimiento(fechaVenc.toLocalDate());
        }

        factura.setConcepto(rs.getString("concepto"));
        factura.setBaseImponible(rs.getDouble("base_imponible"));
        factura.setIvaPorcentaje(rs.getDouble("iva_porcentaje"));
        factura.setIvaCantidad(rs.getDouble("iva_cantidad"));
        factura.setTotal(rs.getDouble("total"));
        factura.setEstado(EstadoFactura.valueOf(rs.getString("estado")));
        factura.setTipo(TipoDocumento.valueOf(rs.getString("tipo")));
        factura.setObservaciones(rs.getString("observaciones"));
        factura.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        factura.setFechaModificacion(rs.getTimestamp("fecha_modificacion").toLocalDateTime());
        return factura;
    }
}