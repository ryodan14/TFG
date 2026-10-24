package com.example.ainaraaisitaldea.util;

import java.util.regex.Pattern;

/**
 * Utilidades para validación de datos de entrada
 */
public class ValidationUtil {

    // Patrones de validación
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern TELEFONO_PATTERN =
            Pattern.compile("^[+]?[0-9]{9,15}$");

    private static final Pattern NIF_PATTERN =
            Pattern.compile("^[A-Z]{1}[0-9]{8}$");
    private static final Pattern PATTERN_CP =
            Pattern.compile("^[0-9]{5}$");

    /**
     * Valida email
     */
    public static boolean validarEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Valida teléfono (formato flexible)
     */
    public static boolean validarTelefono(String telefono) {
        if (telefono == null) return false;
        String limpio = telefono.replaceAll("[\\s-]", "");
        return TELEFONO_PATTERN.matcher(limpio).matches();
    }

    /**
     * Valida NIF/CIF
     */
    public static boolean validarNIF(String nif) {
        return nif != null && NIF_PATTERN.matcher(nif.trim().toUpperCase()).matches();
    }

    /**
     * Valida que un string no esté vacío
     */
    public static boolean noVacio(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    /**
     * Valida rango de número decimal
     */
    public static boolean validarRangoDecimal(double valor, double min, double max) {
        return valor >= min && valor <= max;
    }

    /**
     * Normaliza teléfono eliminando espacios y guiones
     */
    public static String normalizarTelefono(String telefono) {
        if (telefono == null) return "";
        return telefono.replaceAll("[\\s-]", "");
    }

    /**
     * Normaliza texto: trim y capitalización
     */
    public static String normalizarTexto(String texto) {
        if (texto == null) return "";
        return texto.trim();
    }

    /**
     * Capitaliza primera letra de cada palabra
     */
    public static String capitalizarPalabras(String texto) {
        if (texto == null || texto.isEmpty()) return "";

        String[] palabras = texto.toLowerCase().split("\\s+");
        StringBuilder resultado = new StringBuilder();

        for (String palabra : palabras) {
            if (!palabra.isEmpty()) {
                resultado.append(Character.toUpperCase(palabra.charAt(0)))
                        .append(palabra.substring(1))
                        .append(" ");
            }
        }

        return resultado.toString().trim();
    }

    /**
     * Validar código postal español (5 dígitos)
     */
    public static boolean validarCodigoPostal(String cp) {
        if (cp == null || cp.trim().isEmpty()) {
            return false;
        }
        return PATTERN_CP.matcher(cp.trim()).matches();
    }

    /**
     * Limpiar y formatear teléfono
     */
    public static String limpiarTelefono(String telefono) {
        if (telefono == null) return null;
        return telefono.replaceAll("[^0-9]", "");
    }

    /**
     * Validar que un campo no esté vacío
     */
    public static boolean campoNoVacio(String campo) {
        return campo != null && !campo.trim().isEmpty();
    }

    /**
     * Validar longitud mínima
     */
    public static boolean validarLongitudMinima(String campo, int minimo) {
        return campo != null && campo.trim().length() >= minimo;
    }

    /**
     * Validar longitud máxima
     */
    public static boolean validarLongitudMaxima(String campo, int maximo) {
        return campo != null && campo.trim().length() <= maximo;
    }

    /**
     * Validar que el campo contenga solo letras y espacios
     */
    public static boolean validarSoloLetras(String campo) {
        if (campo == null || campo.trim().isEmpty()) {
            return false;
        }
        return campo.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$");
    }

    /**
     * Validar que el campo contenga solo números
     */
    public static boolean validarSoloNumeros(String campo) {
        if (campo == null || campo.trim().isEmpty()) {
            return false;
        }
        return campo.matches("^[0-9]+$");
    }
}