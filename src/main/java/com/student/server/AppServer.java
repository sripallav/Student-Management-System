package com.student.server;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Built-in Core Java HTTP Server.
 */
public class AppServer {

    private final int port;
    private HttpServer server;

    public AppServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // API Endpoints
        server.createContext("/api/students", new StudentApiHandler());
        server.createContext("/api/stats", new StatsApiHandler());
        server.createContext("/api/health", new StatsApiHandler());

        // Static Frontend Asset Files (HTML, CSS, JS)
        server.createContext("/", new StaticFileHandler());

        // Thread Pool Executor for handling concurrent HTTP requests
        server.setExecutor(Executors.newFixedThreadPool(16));
        server.start();

        System.out.println("==========================================================");
        System.out.println(" Student Management System Server Started Successfully! ");
        System.out.println(" Running at: http://localhost:" + port);
        System.out.println(" REST API:   http://localhost:" + port + "/api/students");
        System.out.println("==========================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("[AppServer] Server stopped.");
        }
    }
}
