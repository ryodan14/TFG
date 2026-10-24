package com.example.ainaraaisitaldea.util;
/**
 * Utilidades para manejo de contraseñas (versión sin encriptación)
 */
public class PasswordUtil {

    /**
     * Verifica si una contraseña coincide (comparación directa)
     * @param plainPassword Contraseña ingresada
     * @param storedPassword Contraseña almacenada
     * @return true si coinciden
     */
    public static boolean checkPassword(String plainPassword, String storedPassword) {
        if (plainPassword == null || storedPassword == null) {
            return false;
        }
        return plainPassword.equals(storedPassword);
    }

    /**
     * Valida fortaleza de contraseña
     * @param password Contraseña a validar
     * @return true si cumple requisitos mínimos
     */
    public static boolean esPasswordSegura(String password) {
        if (password == null || password.length() < 4) {
            return false;
        }
        return true; // Requisito mínimo: 4 caracteres
    }

    /**
     * Obtiene mensaje de requisitos de contraseña
     */
    public static String getMensajeRequisitos() {
        return "La contraseña debe tener al menos 4 caracteres";
    }

    /**
     * Genera "hash" (en este caso, devuelve la contraseña tal cual)
     * @param plainPassword Contraseña en texto plano
     * @return La misma contraseña
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        return plainPassword; // Sin encriptación
    }
}