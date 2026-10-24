package com.example.ainaraaisitaldea.service;

import com.example.ainaraaisitaldea.dao.NinoDAO;
import com.example.ainaraaisitaldea.dao.TutorDAO;
import com.example.ainaraaisitaldea.model.Nino;
import com.example.ainaraaisitaldea.model.Tutor;

import java.util.List;

public class NinoService {

    private final NinoDAO ninoDAO;
    private final TutorDAO tutorDAO;

    public NinoService() {
        this.ninoDAO = new NinoDAO();
        this.tutorDAO = new TutorDAO();
    }


    /**
     * Crea un niño junto con su tutor. Si el tutor ya existe (por DNI), lo vincula.
     * Si no existe, crea el tutor primero.
     */
    public boolean crearNinoConTutor(Nino nino, Tutor tutor) {
        try {
            // Validar datos del niño
            if (!validarNino(nino)) {
                return false;
            }

            // Validar datos del tutor
            if (!validarTutor(tutor)) {
                return false;
            }

            // Buscar si el tutor ya existe por DNI
            System.out.println("🔍 Buscando tutor con DNI: " + tutor.getDni());
            Tutor tutorExistente = tutorDAO.buscarPorDni(tutor.getDni());

            if (tutorExistente != null) {
                // Tutor existe, usar su ID
                System.out.println("♻️  Tutor encontrado (ID: " + tutorExistente.getIdTutor() + ") - Reutilizando");
                nino.setIdTutor(tutorExistente.getIdTutor());
            } else {
                // Tutor no existe, crear nuevo
                System.out.println("➕ Tutor no existe - Creando nuevo");
                boolean tutorCreado = tutorDAO.crear(tutor);
                if (!tutorCreado) {
                    System.err.println("❌ Error al crear el tutor");
                    return false;
                }
                System.out.println("✅ Tutor creado con ID: " + tutor.getIdTutor());
                nino.setIdTutor(tutor.getIdTutor());
            }

            // Crear el niño
            System.out.println("➕ Creando niño con id_tutor: " + nino.getIdTutor());
            boolean ninoCreado = ninoDAO.crear(nino);

            if (ninoCreado) {
                System.out.println("✅ Niño creado correctamente con ID: " + nino.getIdNino());
            } else {
                System.err.println("❌ Error al crear el niño");
            }

            return ninoCreado;

        } catch (Exception e) {
            System.err.println("❌ Error al crear niño con tutor: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Guarda un niño (alias para compatibilidad con el código existente)
     */
    public boolean guardar(Nino nino) {
        if (nino.getTutor() == null) {
            System.err.println("El tutor es obligatorio");
            return false;
        }
        return crearNinoConTutor(nino, nino.getTutor());
    }

    /**
     * Actualiza los datos de un niño Y su tutor
     */
    public boolean actualizar(Nino nino) {
        if (!validarNino(nino)) {
            return false;
        }

        // Actualizar tutor si está presente
        if (nino.getTutor() != null && nino.getTutor().getIdTutor() != null) {
            if (!validarTutor(nino.getTutor())) {
                return false;
            }
            tutorDAO.actualizar(nino.getTutor());
        }

        // Actualizar niño
        return ninoDAO.actualizar(nino);
    }

    /**
     * Elimina (desactiva) un niño
     */
    public boolean eliminar(int idNino) {
        return ninoDAO.eliminar(idNino);
    }

    /**
     * Lista todos los niños activos
     */
    public List<Nino> listar() {
        return ninoDAO.listar();
    }

    /**
     * Lista niños con filtros aplicados
     */
    public List<Nino> listarConFiltros(List<String> tiposActividad, String curso, String busqueda) {
        return ninoDAO.listarConFiltros(tiposActividad, curso, busqueda);
    }

    /**
     * Obtiene un niño por su ID
     */
    public Nino obtenerPorId(int idNino) {
        return ninoDAO.obtenerPorId(idNino);
    }

    /**
     * Obtiene el tutor de un niño
     */
    public Tutor obtenerTutorDeNino(int idTutor) {
        return tutorDAO.obtenerPorId(idTutor);
    }
    /**
     * Elimina (desactiva) TODOS los niños
     */
    public int eliminarTodos() {
        return ninoDAO.eliminarTodos();
    }
    /**
     * Valida los datos del niño
     */
    private boolean validarNino(Nino nino) {
        if (nino.getNombre() == null || nino.getNombre().trim().isEmpty()) {
            System.err.println("El nombre del niño es obligatorio");
            return false;
        }

        if (nino.getPrimerApellido() == null || nino.getPrimerApellido().trim().isEmpty()) {
            System.err.println("El primer apellido del niño es obligatorio");
            return false;
        }

        if (nino.getFechaNacimiento() == null) {
            System.err.println("La fecha de nacimiento es obligatoria");
            return false;
        }

        if (nino.getColegioInstituto() == null || nino.getColegioInstituto().trim().isEmpty()) {
            System.err.println("El colegio/instituto es obligatorio");
            return false;
        }

        if (nino.getCurso() == null || nino.getCurso().trim().isEmpty()) {
            System.err.println("El curso es obligatorio");
            return false;
        }

        if (nino.getVinculoTutor() == null) {
            System.err.println("El vínculo con el tutor es obligatorio");
            return false;
        }

        // Validar que puede_salir_solo solo sea true si el curso lo permite
        if (nino.isPuedeSalirSolo() && !Nino.cursoPermiteSalirSolo(nino.getCurso())) {
            System.err.println("El niño no puede salir solo en el curso " + nino.getCurso());
            return false;
        }

        return true;
    }

    /**
     * Valida los datos del tutor
     */
    private boolean validarTutor(Tutor tutor) {
        if (tutor.getNombre() == null || tutor.getNombre().trim().isEmpty()) {
            System.err.println("El nombre del tutor es obligatorio");
            return false;
        }

        if (tutor.getPrimerApellido() == null || tutor.getPrimerApellido().trim().isEmpty()) {
            System.err.println("El primer apellido del tutor es obligatorio");
            return false;
        }

        if (tutor.getDni() == null || tutor.getDni().trim().isEmpty()) {
            System.err.println("El DNI del tutor es obligatorio");
            return false;
        }

        if (tutor.getTelefono() == null || tutor.getTelefono().trim().isEmpty()) {
            System.err.println("El teléfono del tutor es obligatorio");
            return false;
        }

        if (tutor.getDireccion() == null || tutor.getDireccion().trim().isEmpty()) {
            System.err.println("La dirección del tutor es obligatoria");
            return false;
        }

        if (tutor.getMunicipio() == null || tutor.getMunicipio().trim().isEmpty()) {
            System.err.println("El municipio del tutor es obligatorio");
            return false;
        }

        if (tutor.getCodigoPostal() == null || tutor.getCodigoPostal().trim().isEmpty()) {
            System.err.println("El código postal del tutor es obligatorio");
            return false;
        }

        return true;
    }

    public boolean actualizarNinoYTutor(Nino nino, Tutor tutor) {
        if (ninoDAO.actualizarNinoYTutor(nino, tutor)) {
            return true;
        }
        return false;
    }
}