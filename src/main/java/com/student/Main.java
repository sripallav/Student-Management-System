package com.student;

import com.student.config.DatabaseConfig;
import com.student.server.AppServer;

import java.awt.Desktop;
import java.net.URI;

/**
 * Main application entry point for Student Management System.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("----------------------------------------------------------");
        System.out.println("  Starting Student Management System (Core Java + MySQL)");
        System.out.println("----------------------------------------------------------");

        // 1. Initialize Database & verify connection
        try {
            DatabaseConfig.initDatabase();
        } catch (Exception e) {
            System.err.println("[Main] Failed to initialize database: " + e.getMessage());
            System.err.println("[Main] Please ensure MySQL service is running and credentials in db.properties are correct.");
        }

        // 2. Start Built-in HTTP Server
        int port = DatabaseConfig.getServerPort();
        try {
            AppServer appServer = new AppServer(port);
            appServer.start();

            // 3. Try to open default browser automatically
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI("http://localhost:" + port));
                }
            } catch (Exception ignored) {
                // Headless or browse not supported
            }

            System.out.println("\nReady! Open your browser at: http://localhost:" + port);
            System.out.println("Press Ctrl+C in this console to stop the server.\n");

        } catch (Exception e) {
            System.err.println("[Main] Failed to start HTTP server on port " + port + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
