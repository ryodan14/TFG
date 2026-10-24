package com.example.ainaraaisitaldea.service;

import com.example.ainaraaisitaldea.dao.FacturaDAO;
import com.example.ainaraaisitaldea.model.Factura;
import com.example.ainaraaisitaldea.model.enums.TipoDocumento;
import com.example.ainaraaisitaldea.util.ValidationUtil;
import java.sql.SQLException;
import java.util.List;

/**
 * Servicio de gestión de facturas y presupuestos
 */
public class FacturaService {

    private final FacturaDAO facturaDAO;

    public FacturaService() {
        this.facturaDAO = new FacturaDAO();
    }

    /**
     * Crear factura/presupuesto con validaciones
     */
    public Factura crearFactura(Factura factura) throws SQLException {

        // Validaciones
        if (!ValidationUtil.noVacio(factura.getCliente())) {
            throw new IllegalArgumentException("El cliente es obligatorio");
        }

        if (factura.getFechaEmision() == null) {
            throw new IllegalArgumentException("La fecha de emisión es obligatoria");
        }

        if (!ValidationUtil.noVacio(factura.getConcepto())) {
            throw new IllegalArgumentException("El concepto es obligatorio");
        }

        if (factura.getBaseImponible() == null || factura.getBaseImponible() <= 0) {
            throw new IllegalArgumentException("La base imponible debe ser mayor que cero");
        }

        // Calcular totales
        factura.calcularTotal();

        // Generar número automático si no existe
        if (factura.getNumeroFactura() == null || factura.getNumeroFactura().isEmpty()) {
            String numero = facturaDAO.generarNumeroFactura(factura.getTipo());
            factura.setNumeroFactura(numero);
        }

        return facturaDAO.crear(factura);
    }

    /**
     * Actualizar factura
     */
    public void actualizarFactura(Factura factura) throws SQLException {

        if (!ValidationUtil.noVacio(factura.getCliente())) {
            throw new IllegalArgumentException("El cliente es obligatorio");
        }

        if (!ValidationUtil.noVacio(factura.getConcepto())) {
            throw new IllegalArgumentException("El concepto es obligatorio");
        }

        if (factura.getBaseImponible() == null || factura.getBaseImponible() <= 0) {
            throw new IllegalArgumentException("La base imponible debe ser mayor que cero");
        }

        factura.calcularTotal();
        facturaDAO.actualizar(factura);
    }

    /**
     * Listar por tipo
     */
    public List<Factura> listarPorTipo(TipoDocumento tipo) throws SQLException {
        return facturaDAO.listarPorTipo(tipo);
    }

    /**
     * Listar todas
     */
    public List<Factura> listarTodas() throws SQLException {
        return facturaDAO.listarTodas();
    }

    /**
     * Obtener por ID
     */
    public Factura obtenerPorId(Integer idFactura) throws SQLException {
        return facturaDAO.obtenerPorId(idFactura);
    }

    /**
     * Eliminar factura
     */
    public void eliminarFactura(Integer idFactura) throws SQLException {
        facturaDAO.eliminar(idFactura);
    }
}
