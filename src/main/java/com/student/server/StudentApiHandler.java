package com.student.server;

import com.student.dao.StudentDAO;
import com.student.dao.StudentDAOImpl;
import com.student.model.Student;
import com.student.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * REST API Handler for Student resource (/api/students).
 */
public class StudentApiHandler implements HttpHandler {

    private final StudentDAO studentDAO = new StudentDAOImpl();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Enable CORS
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");

        String method = exchange.getRequestMethod().toUpperCase();

        if (method.equals("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getRawQuery();

        try {
            switch (method) {
                case "GET":
                    handleGet(exchange, path, query);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "PUT":
                    handlePut(exchange, path);
                    break;
                case "DELETE":
                    handleDelete(exchange, path);
                    break;
                default:
                    sendJsonResponse(exchange, 405, JsonUtil.errorResponse("Method Not Allowed: " + method));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, JsonUtil.errorResponse("Internal Server Error: " + e.getMessage()));
        }
    }

    private void handleGet(HttpExchange exchange, String path, String query) throws IOException {
        Integer id = extractIdFromPath(path);

        if (id != null) {
            // GET /api/students/{id}
            Student student = studentDAO.getStudentById(id);
            if (student != null) {
                sendJsonResponse(exchange, 200, JsonUtil.successResponse("Student retrieved", JsonUtil.studentToJson(student)));
            } else {
                sendJsonResponse(exchange, 404, JsonUtil.errorResponse("Student not found with ID: " + id));
            }
        } else {
            // GET /api/students or /api/students?q=...&course=...&status=...
            Map<String, String> qParams = parseQueryParams(query);
            String q = qParams.get("q");
            String course = qParams.get("course");
            String status = qParams.get("status");

            List<Student> list;
            if ((q != null && !q.isEmpty()) || (course != null && !course.isEmpty()) || (status != null && !status.isEmpty())) {
                list = studentDAO.searchStudents(q, course, status);
            } else {
                list = studentDAO.getAllStudents();
            }

            String listJson = JsonUtil.studentListToJson(list);
            sendJsonResponse(exchange, 200, JsonUtil.successResponse("Fetched " + list.size() + " students", listJson));
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Map<String, String> data = JsonUtil.parseJsonObject(body);

        String rollNumber = data.getOrDefault("rollNumber", "").trim();
        String firstName = data.getOrDefault("firstName", "").trim();
        String lastName = data.getOrDefault("lastName", "").trim();
        String email = data.getOrDefault("email", "").trim();
        String phone = data.getOrDefault("phone", "").trim();
        String course = data.getOrDefault("course", "").trim();

        if (rollNumber.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || course.isEmpty()) {
            sendJsonResponse(exchange, 400, JsonUtil.errorResponse("Missing required fields: Roll Number, First Name, Last Name, Email, and Course are required."));
            return;
        }

        if (studentDAO.rollNumberExists(rollNumber, null)) {
            sendJsonResponse(exchange, 400, JsonUtil.errorResponse("A student with Roll Number '" + rollNumber + "' already exists."));
            return;
        }

        if (studentDAO.emailExists(email, null)) {
            sendJsonResponse(exchange, 400, JsonUtil.errorResponse("A student with Email '" + email + "' already exists."));
            return;
        }

        Student student = JsonUtil.parseStudent(data);
        boolean success = studentDAO.addStudent(student);

        if (success) {
            sendJsonResponse(exchange, 201, JsonUtil.successResponse("Student created successfully", JsonUtil.studentToJson(student)));
        } else {
            sendJsonResponse(exchange, 500, JsonUtil.errorResponse("Failed to save student record to database."));
        }
    }

    private void handlePut(HttpExchange exchange, String path) throws IOException {
        Integer id = extractIdFromPath(path);
        if (id == null) {
            sendJsonResponse(exchange, 400, JsonUtil.errorResponse("Student ID is required in URL path (e.g., /api/students/12)."));
            return;
        }

        Student existing = studentDAO.getStudentById(id);
        if (existing == null) {
            sendJsonResponse(exchange, 404, JsonUtil.errorResponse("Student not found with ID: " + id));
            return;
        }

        String body = readRequestBody(exchange);
        Map<String, String> data = JsonUtil.parseJsonObject(body);

        String rollNumber = data.getOrDefault("rollNumber", existing.getRollNumber()).trim();
        String email = data.getOrDefault("email", existing.getEmail()).trim();

        if (studentDAO.rollNumberExists(rollNumber, id)) {
            sendJsonResponse(exchange, 400, JsonUtil.errorResponse("Another student already has Roll Number: " + rollNumber));
            return;
        }

        if (studentDAO.emailExists(email, id)) {
            sendJsonResponse(exchange, 400, JsonUtil.errorResponse("Another student already has Email: " + email));
            return;
        }

        Student updated = JsonUtil.parseStudent(data);
        updated.setId(id);

        boolean success = studentDAO.updateStudent(updated);
        if (success) {
            Student saved = studentDAO.getStudentById(id);
            sendJsonResponse(exchange, 200, JsonUtil.successResponse("Student updated successfully", JsonUtil.studentToJson(saved)));
        } else {
            sendJsonResponse(exchange, 500, JsonUtil.errorResponse("Failed to update student in database."));
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        Integer id = extractIdFromPath(path);
        if (id == null) {
            sendJsonResponse(exchange, 400, JsonUtil.errorResponse("Student ID is required in URL path (e.g., /api/students/12)."));
            return;
        }

        Student existing = studentDAO.getStudentById(id);
        if (existing == null) {
            sendJsonResponse(exchange, 404, JsonUtil.errorResponse("Student not found with ID: " + id));
            return;
        }

        boolean success = studentDAO.deleteStudent(id);
        if (success) {
            sendJsonResponse(exchange, 200, JsonUtil.successResponse("Student #" + id + " (" + existing.getFullName() + ") deleted successfully", null));
        } else {
            sendJsonResponse(exchange, 500, JsonUtil.errorResponse("Failed to delete student from database."));
        }
    }

    private Integer extractIdFromPath(String path) {
        if (path == null) return null;
        String[] parts = path.split("/");
        if (parts.length >= 4 && parts[1].equals("api") && parts[2].equals("students")) {
            try {
                return Integer.parseInt(parts[3]);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.trim().isEmpty()) return map;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String val = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(key, val);
            }
        }
        return map;
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int n;
            while ((n = is.read(buf)) != -1) {
                baos.write(buf, 0, n);
            }
            return baos.toString(StandardCharsets.UTF_8);
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
