package com.student.server;

import com.student.config.DatabaseConfig;
import com.student.dao.StudentDAO;
import com.student.dao.StudentDAOImpl;
import com.student.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.Map;

/**
 * Handles dashboard statistics (/api/stats) and health checks (/api/health).
 */
public class StatsApiHandler implements HttpHandler {

    private final StudentDAO studentDAO = new StudentDAOImpl();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();

        if (path.endsWith("/health")) {
            boolean dbHealthy = false;
            try (Connection conn = DatabaseConfig.getConnection()) {
                dbHealthy = conn.isValid(2);
            } catch (Exception ignored) {}

            String resp = "{\"status\":\"" + (dbHealthy ? "UP" : "DOWN") + "\",\"database\":\"" + (dbHealthy ? "CONNECTED" : "DISCONNECTED") + "\"}";
            sendJsonResponse(exchange, dbHealthy ? 200 : 503, resp);
            return;
        }

        try {
            Map<String, Object> stats = studentDAO.getStatistics();
            String json = JsonUtil.mapToJson(stats);
            sendJsonResponse(exchange, 200, JsonUtil.successResponse("Statistics retrieved", json));
        } catch (Exception e) {
            sendJsonResponse(exchange, 500, JsonUtil.errorResponse("Failed to fetch statistics: " + e.getMessage()));
        }
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
