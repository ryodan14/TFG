package com.example.ainaraaisitaldea.model;

import java.time.LocalDate;
import java.time.Period;

public class Nino {

    public enum VinculoTutor {
        MADRE("Madre"),
        PADRE("Padre"),
        TUTOR_LEGAL("Tutor Legal");

        private final String descripcion;

        VinculoTutor(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public static VinculoTutor fromString(String texto) {
            if (texto == null || texto.trim().isEmpty()) return null;
            String t = texto.trim();
            if ("Madre".equalsIgnoreCase(t)) return MADRE;
            if ("Padre".equalsIgnoreCase(t)) return PADRE;
            if ("Tutor Legal".equalsIgnoreCase(t)) return TUTOR_LEGAL;
            return null;
        }
    }

    private Integer idNino;
    private String nombre;
    private String primerApellido;
    private String segundoApellido;
    private LocalDate fechaNacimiento;
    private String colegioInstituto;
    private String curso;
    private boolean tieneAlergias;
    private String observaciones;
    private boolean puedeSalirSolo;
    private boolean autorizacionFotos;
    private Integer idTutor;
    private VinculoTutor vinculoTutor;

    // Datos del tutor (para visualización)
    private String nombreCompletoTutor;
    private String telefonoTutor;
    private String correoTutor;
    private String dniTutor;

    // Objeto tutor completo (para edición)
    private Tutor tutor;

    public Nino() {
        this.autorizacionFotos = true;
        this.puedeSalirSolo = false;
    }

    // Getters y Setters
    public Integer getIdNino() {
        return idNino;
    }

    public void setIdNino(Integer idNino) {
        this.idNino = idNino;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPrimerApellido() {
        return primerApellido;
    }

    public void setPrimerApellido(String primerApellido) {
        this.primerApellido = primerApellido;
    }

    public String getSegundoApellido() {
        return segundoApellido;
    }

    public void setSegundoApellido(String segundoApellido) {
        this.segundoApellido = segundoApellido;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getColegioInstituto() {
        return colegioInstituto;
    }

    public void setColegioInstituto(String colegioInstituto) {
        this.colegioInstituto = colegioInstituto;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public boolean isTieneAlergias() {
        return tieneAlergias;
    }

    public void setTieneAlergias(boolean tieneAlergias) {
        this.tieneAlergias = tieneAlergias;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public boolean isPuedeSalirSolo() {
        return puedeSalirSolo;
    }

    public void setPuedeSalirSolo(boolean puedeSalirSolo) {
        this.puedeSalirSolo = puedeSalirSolo;
    }

    public boolean isAutorizacionFotos() {
        return autorizacionFotos;
    }

    public void setAutorizacionFotos(boolean autorizacionFotos) {
        this.autorizacionFotos = autorizacionFotos;
    }

    public Integer getIdTutor() {
        return idTutor;
    }

    public void setIdTutor(Integer idTutor) {
        this.idTutor = idTutor;
    }

    public VinculoTutor getVinculoTutor() {
        return vinculoTutor;
    }

    public void setVinculoTutor(VinculoTutor vinculoTutor) {
        this.vinculoTutor = vinculoTutor;
    }

    public String getNombreCompletoTutor() {
        return nombreCompletoTutor;
    }

    public void setNombreCompletoTutor(String nombreCompletoTutor) {
        this.nombreCompletoTutor = nombreCompletoTutor;
    }

    public String getTelefonoTutor() {
        return telefonoTutor;
    }

    public void setTelefonoTutor(String telefonoTutor) {
        this.telefonoTutor = telefonoTutor;
    }

    public String getCorreoTutor() {
        return correoTutor;
    }

    public void setCorreoTutor(String correoTutor) {
        this.correoTutor = correoTutor;
    }

    public String getDniTutor() {
        return dniTutor;
    }

    public void setDniTutor(String dniTutor) {
        this.dniTutor = dniTutor;
    }

    public Tutor getTutor() {
        return tutor;
    }

    public void setTutor(Tutor tutor) {
        this.tutor = tutor;
        if (tutor != null) {
            this.idTutor = tutor.getIdTutor();
        }
    }

    // Métodos auxiliares
    public String getNombreCompleto() {
        StringBuilder sb = new StringBuilder();
        if (nombre != null) sb.append(nombre);
        if (primerApellido != null) sb.append(" ").append(primerApellido);
        if (segundoApellido != null && !segundoApellido.isEmpty()) {
            sb.append(" ").append(segundoApellido);
        }
        return sb.toString().trim();
    }

    public int getEdad() {
        if (fechaNacimiento == null) return 0;
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    public String getTieneAlergiasTexto() {
        return tieneAlergias ? "Sí" : "No";
    }

    public String getPuedeSalirSoloTexto() {
        return puedeSalirSolo ? "Sí" : "No";
    }

    public String getAutorizacionFotosTexto() {
        return autorizacionFotos ? "Sí" : "No";
    }

    /**
     * Verifica si el curso permite salir solo (5º Primaria o superior)
     */
    public static boolean cursoPermiteSalirSolo(String curso) {
        if (curso == null) return false;

        return curso.equals("5º Primaria") ||
                curso.equals("6º Primaria") ||
                curso.contains("ESO");
    }

    /**
     * Obtiene el orden numérico del curso para ordenamiento
     */
    public static int getOrdenCurso(String curso) {
        if (curso == null) return 999;

        switch (curso) {
            case "1º Primaria": return 1;
            case "2º Primaria": return 2;
            case "3º Primaria": return 3;
            case "4º Primaria": return 4;
            case "5º Primaria": return 5;
            case "6º Primaria": return 6;
            case "1º ESO": return 7;
            case "2º ESO": return 8;
            case "3º ESO": return 9;
            case "4º ESO": return 10;
            default: return 999;
        }
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " - " + curso;
    }
}