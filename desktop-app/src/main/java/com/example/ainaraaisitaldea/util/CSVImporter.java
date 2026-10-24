package com.example.ainaraaisitaldea.util;

import com.example.ainaraaisitaldea.dao.NinoDAO;
import com.example.ainaraaisitaldea.dao.TutorDAO;
import com.example.ainaraaisitaldea.model.Nino;
import com.example.ainaraaisitaldea.model.Tutor;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CSVImporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Resultado de la importación
     */
    public static class ResultadoImportacion {
        public int exitosos = 0;
        public int errores = 0;
        public int actualizados = 0;
        public int nuevos = 0;
        public int omitidos = 0; // Registros que ya existían y no se modificaron
        public int tutoresCreados = 0;
        public int tutoresActualizados = 0;
        public int tutoresReutilizados = 0; // Tutores que ya existían y se reutilizaron
        public List<String> mensajesError = new ArrayList<>();
    }

    /**
     * Importa niños desde un archivo CSV con soporte para IDs
     * - Si ID_Nino existe: actualiza el registro
     * - Si ID_Nino no existe o está vacío: crea nuevo registro
     * - Si ID_Tutor existe: actualiza el tutor
     * - Si ID_Tutor no existe: busca por DNI o crea nuevo
     */
    public static ResultadoImportacion importarNinosCSV() {
        ResultadoImportacion resultado = new ResultadoImportacion();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos CSV", "*.csv")
        );

        File file = fileChooser.showOpenDialog(null);

        if (file == null) {
            return resultado;
        }

        NinoDAO ninoDAO = new NinoDAO();
        TutorDAO tutorDAO = new TutorDAO();

        try (BufferedReader reader = new BufferedReader(
                new FileReader(file, java.nio.charset.StandardCharsets.UTF_8))) {

            // Saltar BOM si existe
            reader.mark(1);
            if (reader.read() != 0xFEFF) {
                reader.reset();
            }

            // Leer encabezados
            String lineaEncabezados = reader.readLine();
            if (lineaEncabezados == null) {
                resultado.mensajesError.add("El archivo está vacío");
                return resultado;
            }

            System.out.println("📋 Encabezados: " + lineaEncabezados);

            // Validar encabezados
            if (!validarEncabezados(lineaEncabezados)) {
                resultado.mensajesError.add("El formato del archivo CSV no es válido. " +
                        "Asegúrese de usar un archivo exportado por esta aplicación.");
                return resultado;
            }

            // Leer datos línea por línea
            String linea;
            int numeroLinea = 1;

            while ((linea = reader.readLine()) != null) {
                numeroLinea++;

                if (linea.trim().isEmpty()) {
                    continue;
                }

                try {
                    String[] campos = parsearLineaCSV(linea);

                    System.out.println("\n📝 Línea " + numeroLinea + ": " + campos.length + " campos");

                    // Verificar que tenga al menos 22 campos (con IDs)
                    if (campos.length < 22) {
                        resultado.errores++;
                        resultado.mensajesError.add("Línea " + numeroLinea + ": Faltan campos (tiene " +
                                campos.length + ", se esperan 22)");
                        System.err.println("❌ Faltan campos en línea " + numeroLinea);
                        continue;
                    }

                    // Leer IDs (campos 0-1)
                    String idNinoStr = limpiarCampo(campos[0]);
                    String idTutorStr = limpiarCampo(campos[1]);

                    Integer idNino = idNinoStr.isEmpty() ? null : Integer.parseInt(idNinoStr);
                    Integer idTutor = idTutorStr.isEmpty() ? null : Integer.parseInt(idTutorStr);

                    System.out.println("🔢 IDs -> Niño: " + idNino + ", Tutor: " + idTutor);

                    // === PROCESAR TUTOR (campos 13-21) ===
                    Tutor tutor = new Tutor();
                    tutor.setNombre(limpiarCampo(campos[13]));
                    tutor.setPrimerApellido(limpiarCampo(campos[14]));

                    String segundoApellidoTutor = limpiarCampo(campos[15]);
                    tutor.setSegundoApellido(segundoApellidoTutor.isEmpty() ? null : segundoApellidoTutor);

                    String dni = limpiarCampo(campos[16]).toUpperCase();
                    if (dni.isEmpty()) {
                        resultado.errores++;
                        resultado.mensajesError.add("Línea " + numeroLinea + ": DNI del tutor vacío");
                        System.err.println("❌ DNI vacío");
                        continue;
                    }
                    tutor.setDni(dni);

                    tutor.setTelefono(limpiarCampo(campos[17]));

                    String correo = limpiarCampo(campos[18]);
                    tutor.setCorreo(correo.isEmpty() || "-".equals(correo) ? null : correo);

                    tutor.setDireccion(limpiarCampo(campos[19]));
                    tutor.setMunicipio(limpiarCampo(campos[20]));
                    tutor.setCodigoPostal(limpiarCampo(campos[21]));

                    // Decidir si actualizar o crear tutor
                    Integer idTutorFinal;

                    if (idTutor != null) {
                        // Intentar obtener tutor existente por ID
                        Tutor tutorExistente = tutorDAO.obtenerPorId(idTutor);

                        if (tutorExistente != null) {
                            // Tutor existe por ID -> ACTUALIZAR
                            System.out.println("♻️  Tutor ID=" + idTutor + " existe -> Actualizando");
                            tutor.setIdTutor(idTutor);

                            if (tutorDAO.actualizar(tutor)) {
                                resultado.tutoresActualizados++;
                                idTutorFinal = idTutor;
                            } else {
                                resultado.errores++;
                                resultado.mensajesError.add("Línea " + numeroLinea + ": Error al actualizar tutor ID=" + idTutor);
                                continue;
                            }
                        } else {
                            // ID no existe, buscar por DNI
                            Tutor tutorPorDni = tutorDAO.buscarPorDni(dni);

                            if (tutorPorDni != null) {
                                // Existe con otro ID -> usar el existente
                                System.out.println("♻️  Tutor con DNI " + dni + " ya existe (ID=" + tutorPorDni.getIdTutor() + ")");
                                idTutorFinal = tutorPorDni.getIdTutor();
                            } else {
                                // No existe -> CREAR
                                System.out.println("➕ Creando nuevo tutor");
                                if (tutorDAO.crear(tutor)) {
                                    resultado.tutoresCreados++;
                                    idTutorFinal = tutor.getIdTutor();
                                } else {
                                    resultado.errores++;
                                    resultado.mensajesError.add("Línea " + numeroLinea + ": Error al crear tutor");
                                    continue;
                                }
                            }
                        }
                    } else {
                        // Sin ID, buscar por DNI o crear
                        Tutor tutorPorDni = tutorDAO.buscarPorDni(dni);

                        if (tutorPorDni != null) {
                            System.out.println("♻️  Tutor con DNI " + dni + " encontrado (ID=" + tutorPorDni.getIdTutor() + ")");
                            idTutorFinal = tutorPorDni.getIdTutor();
                        } else {
                            System.out.println("➕ Creando nuevo tutor");
                            if (tutorDAO.crear(tutor)) {
                                resultado.tutoresCreados++;
                                idTutorFinal = tutor.getIdTutor();
                            } else {
                                resultado.errores++;
                                resultado.mensajesError.add("Línea " + numeroLinea + ": Error al crear tutor");
                                continue;
                            }
                        }
                    }

                    // === PROCESAR NIÑO (campos 2-12) ===
                    Nino nino = new Nino();
                    nino.setNombre(limpiarCampo(campos[2]));
                    nino.setPrimerApellido(limpiarCampo(campos[3]));

                    String segundoApellido = limpiarCampo(campos[4]);
                    nino.setSegundoApellido(segundoApellido.isEmpty() ? null : segundoApellido);

                    nino.setFechaNacimiento(LocalDate.parse(limpiarCampo(campos[5]), DATE_FORMATTER));
                    nino.setColegioInstituto(limpiarCampo(campos[6]));
                    nino.setCurso(limpiarCampo(campos[7]));

                    nino.setTieneAlergias(esAfirmativo(campos[8]));
                    nino.setPuedeSalirSolo(esAfirmativo(campos[9]));
                    nino.setAutorizacionFotos(esAfirmativo(campos[10]));

                    String obs = limpiarCampo(campos[11]);
                    nino.setObservaciones(obs.isEmpty() || "-".equals(obs) ? null : obs);

                    nino.setVinculoTutor(Nino.VinculoTutor.valueOf(limpiarCampo(campos[12])));
                    nino.setIdTutor(idTutorFinal);

                    System.out.println("👤 Niño: " + nino.getNombre() + " " + nino.getPrimerApellido());
                    System.out.println("👨‍👩‍👧 ID Tutor asignado: " + idTutorFinal);

                    // Decidir si actualizar o crear niño
                    boolean exito;

                    if (idNino != null) {
                        // Intentar actualizar
                        Nino ninoExistente = ninoDAO.obtenerPorId(idNino);

                        if (ninoExistente != null) {
                            // Existe -> ACTUALIZAR
                            System.out.println("♻️  Niño ID=" + idNino + " existe -> Actualizando");
                            nino.setIdNino(idNino);
                            exito = ninoDAO.actualizar(nino);

                            if (exito) {
                                resultado.actualizados++;
                            }
                        } else {
                            // No existe con ese ID -> CREAR
                            System.out.println("➕ Niño ID=" + idNino + " no existe -> Creando nuevo");
                            exito = ninoDAO.crear(nino);

                            if (exito) {
                                resultado.nuevos++;
                            }
                        }
                    } else {
                        // Sin ID -> CREAR
                        System.out.println("➕ Creando nuevo niño");
                        exito = ninoDAO.crear(nino);

                        if (exito) {
                            resultado.nuevos++;
                        }
                    }

                    if (exito) {
                        resultado.exitosos++;
                        System.out.println("✅ Operación exitosa");
                    } else {
                        resultado.errores++;
                        resultado.mensajesError.add("Línea " + numeroLinea + ": Error al guardar niño");
                        System.err.println("❌ Error al guardar");
                    }

                } catch (Exception e) {
                    resultado.errores++;
                    resultado.mensajesError.add("Línea " + numeroLinea + ": " + e.getMessage());
                    System.err.println("❌ Error procesando línea " + numeroLinea + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("\n=== Importación completada ===");
            System.out.println("✅ Exitosos: " + resultado.exitosos);
            System.out.println("   - Nuevos: " + resultado.nuevos);
            System.out.println("   - Actualizados: " + resultado.actualizados);
            System.out.println("⏭️  Omitidos (ya existían): " + resultado.omitidos);
            System.out.println("👨‍👩‍👧 Tutores:");
            System.out.println("   - Creados: " + resultado.tutoresCreados);
            System.out.println("   - Actualizados: " + resultado.tutoresActualizados);
            System.out.println("   - Reutilizados: " + resultado.tutoresReutilizados);
            System.out.println("❌ Errores: " + resultado.errores);

        } catch (Exception e) {
            resultado.mensajesError.add("Error al leer el archivo: " + e.getMessage());
            System.err.println("Error al importar CSV: " + e.getMessage());
            e.printStackTrace();
        }

        return resultado;
    }

    /**
     * Verifica si un niño ya existe en la base de datos comparando
     * nombre, apellidos, fecha de nacimiento y tutor
     */
    private static boolean verificarNinoExistente(NinoDAO ninoDAO, Nino nino, Integer idTutor) {
        try {
            // Obtener todos los niños (o mejor, implementar un método de búsqueda específico)
            List<Nino> todosLosNinos = ninoDAO.listar();

            for (Nino ninoExistente : todosLosNinos) {
                // Comparar datos clave
                boolean mismoNombre = nino.getNombre().equalsIgnoreCase(ninoExistente.getNombre());
                boolean mismoPrimerApellido = nino.getPrimerApellido().equalsIgnoreCase(ninoExistente.getPrimerApellido());
                boolean mismoSegundoApellido = compararSegundoApellido(nino.getSegundoApellido(), ninoExistente.getSegundoApellido());
                boolean mismaFecha = nino.getFechaNacimiento().equals(ninoExistente.getFechaNacimiento());
                boolean mismoTutor = idTutor.equals(ninoExistente.getIdTutor());

                if (mismoNombre && mismoPrimerApellido && mismoSegundoApellido && mismaFecha && mismoTutor) {
                    System.out.println("🔍 Niño duplicado encontrado: ID=" + ninoExistente.getIdNino());
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            System.err.println("Error al verificar niño existente: " + e.getMessage());
            return false; // En caso de error, permitir la creación
        }
    }

    /**
     * Compara segundo apellido considerando nulos
     */
    private static boolean compararSegundoApellido(String apellido1, String apellido2) {
        if (apellido1 == null && apellido2 == null) return true;
        if (apellido1 == null || apellido2 == null) return false;
        return apellido1.equalsIgnoreCase(apellido2);
    }

    private static String limpiarCampo(String campo) {
        if (campo == null) return "";

        String limpio = campo.trim();

        if (limpio.startsWith("\"") && limpio.endsWith("\"") && limpio.length() > 1) {
            limpio = limpio.substring(1, limpio.length() - 1);
            limpio = limpio.replace("\"\"", "\"");
        }

        return limpio.trim();
    }

    private static boolean esAfirmativo(String campo) {
        String valor = limpiarCampo(campo).toUpperCase();
        return valor.equals("SÍ") ||
                valor.equals("SI") ||
                valor.equals("YES") ||
                valor.equals("1") ||
                valor.equals("TRUE");
    }

    private static boolean validarEncabezados(String lineaEncabezados) {
        String[] encabezados = parsearLineaCSV(lineaEncabezados);

        System.out.println("📊 Número de encabezados: " + encabezados.length);

        if (encabezados.length < 22) {
            System.err.println("❌ Error: Se esperan al menos 22 columnas, se encontraron " + encabezados.length);
            return false;
        }

        String primerEncabezado = limpiarCampo(encabezados[0]);
        boolean valido = primerEncabezado.contains("ID");

        if (!valido) {
            System.err.println("❌ Error: Primera columna debe ser ID_Nino, encontrado: " + primerEncabezado);
        }

        return valido;
    }

    private static String[] parsearLineaCSV(String linea) {
        List<String> campos = new ArrayList<>();
        StringBuilder campoActual = new StringBuilder();
        boolean dentroComillas = false;

        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);

            if (c == '"') {
                if (i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    campoActual.append('"');
                    i++;
                } else {
                    dentroComillas = !dentroComillas;
                }
            } else if (c == ';' && !dentroComillas) {
                campos.add(campoActual.toString());
                campoActual = new StringBuilder();
            } else {
                campoActual.append(c);
            }
        }

        campos.add(campoActual.toString());

        return campos.toArray(new String[0]);
    }
}