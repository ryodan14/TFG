package com.example.ainaraaisitaldea.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * Factory para gestionar conexiones a MySQL/MariaDB
 * Implementa pool de conexiones básico con reintentos automáticos
 */
public class ConnectionFactory {

    private static ConnectionFactory instance;
    private static String url;
    private static String user;
    private static String password;
    private static int maxRetries = 3;
    private static int retryDelayMs = 2000;

    private ConnectionFactory() {
        loadDatabaseConfig();
    }

    /**
     * Singleton para obtener instancia única
     */
    public static synchronized ConnectionFactory getInstance() {
        if (instance == null) {
            instance = new ConnectionFactory();
        }
        return instance;
    }

    /**
     * Carga configuración desde database.properties
     */
    private void loadDatabaseConfig() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("config/database.properties")) {

            if (input == null) {
                System.out.println("No se encontró database.properties, usando valores por defecto");
                useDefaultConfig();
                return;
            }

            props.load(input);
            this.url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/gestion_asociacion");
            this.user = props.getProperty("db.user", "root");
            this.password = props.getProperty("db.password", "");
            this.maxRetries = Integer.parseInt(props.getProperty("db.maxRetries", "3"));
            this.retryDelayMs = Integer.parseInt(props.getProperty("db.retryDelay", "2000"));

        } catch (IOException e) {
            System.err.println("Error cargando configuración de BD: " + e.getMessage());
            useDefaultConfig();
        }
    }

    /**
     * Configuración por defecto si no existe archivo
     */
    private void useDefaultConfig() {
        this.url = "jdbc:mysql://localhost:3306/gestion_asociacion?useSSL=false&serverTimezone=Europe/Madrid";
        this.user = "root";
        this.password = "";
    }

    /**
     * Obtiene una conexión con reintentos automáticos
     * @return Connection activa
     * @throws SQLException si no se puede conectar después de reintentos
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = null;
        int attempts = 0;
        SQLException lastException = null;

        while (attempts < maxRetries) {
            try {
                // Registrar driver si es necesario
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Intentar conexión
                conn = DriverManager.getConnection(url, user, password);

                // Configurar conexión
                conn.setAutoCommit(true);

                System.out.println("Conexion establecida exitosamente (intento " + (attempts + 1) + ")");
                return conn;

            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver MySQL no encontrado: " + e.getMessage());

            } catch (SQLException e) {
                lastException = e;
                attempts++;

                System.err.println("Error al conectar (intento " + attempts + "/" + maxRetries + "): "
                        + e.getMessage());

                if (attempts < maxRetries) {
                    try {
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new SQLException("Interrupción durante reintento de conexion");
                    }
                }
            }
        }

        // Si llegamos aquí, fallaron todos los intentos
        throw new SQLException("No se pudo conectar a la base de datos después de "
                + maxRetries + " intentos", lastException);
    }

    /**
     * Cierra conexión de forma segura
     */
    public void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                    System.out.println("Conexion cerrada correctamente");
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexion: " + e.getMessage());
            }
        }
    }

    /**
     * Verifica si la conexión funciona
     * @return true si se puede conectar
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Test de conexion falló: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene URL de la base de datos
     */
    public String getUrl() {
        return url;
    }

    /**
     * Obtiene usuario de la base de datos
     */
    public String getUser() {
        return user;
    }
}