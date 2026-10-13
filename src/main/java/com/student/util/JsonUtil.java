package com.student.util;

import com.student.model.Student;

import java.util.*;

/**
 * Lightweight, zero-dependency JSON utility for serialization and deserialization.
 */
public class JsonUtil {

    public static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    public static String studentToJson(Student s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(s.getId() == null ? "null" : s.getId()).append(",");
        sb.append("\"rollNumber\":\"").append(escapeJson(s.getRollNumber())).append("\",");
        sb.append("\"firstName\":\"").append(escapeJson(s.getFirstName())).append("\",");
        sb.append("\"lastName\":\"").append(escapeJson(s.getLastName())).append("\",");
        sb.append("\"fullName\":\"").append(escapeJson(s.getFullName())).append("\",");
        sb.append("\"email\":\"").append(escapeJson(s.getEmail())).append("\",");
        sb.append("\"phone\":\"").append(escapeJson(s.getPhone())).append("\",");
        sb.append("\"gender\":\"").append(escapeJson(s.getGender())).append("\",");
        sb.append("\"course\":\"").append(escapeJson(s.getCourse())).append("\",");
        sb.append("\"yearLevel\":").append(s.getYearLevel()).append(",");
        sb.append("\"gpa\":").append(s.getGpa()).append(",");
        sb.append("\"city\":\"").append(escapeJson(s.getCity())).append("\",");
        sb.append("\"status\":\"").append(escapeJson(s.getStatus())).append("\",");
        sb.append("\"createdAt\":").append(s.getCreatedAt() != null ? "\"" + escapeJson(s.getCreatedAt()) + "\"" : "null").append(",");
        sb.append("\"updatedAt\":").append(s.getUpdatedAt() != null ? "\"" + escapeJson(s.getUpdatedAt()) + "\"" : "null");
        sb.append("}");
        return sb.toString();
    }

    public static String studentListToJson(List<Student> students) {
        if (students == null) return "[]";
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < students.size(); i++) {
            sb.append(studentToJson(students.get(i)));
            if (i < students.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public static String mapToJson(Map<String, ?> map) {
        if (map == null) return "{}";
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        int count = 0;
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            if (count > 0) sb.append(",");
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":");
            Object val = entry.getValue();
            if (val == null) {
                sb.append("null");
            } else if (val instanceof Number || val instanceof Boolean) {
                sb.append(val);
            } else if (val instanceof Student) {
                sb.append(studentToJson((Student) val));
            } else if (val instanceof List) {
                List<?> list = (List<?>) val;
                if (!list.isEmpty() && list.get(0) instanceof Student) {
                    @SuppressWarnings("unchecked")
                    List<Student> sList = (List<Student>) list;
                    sb.append(studentListToJson(sList));
                } else {
                    sb.append("[");
                    for (int j = 0; j < list.size(); j++) {
                        if (j > 0) sb.append(",");
                        Object item = list.get(j);
                        if (item instanceof Number || item instanceof Boolean) {
                            sb.append(item);
                        } else {
                            sb.append("\"").append(escapeJson(String.valueOf(item))).append("\"");
                        }
                    }
                    sb.append("]");
                }
            } else if (val instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, ?> subMap = (Map<String, ?>) val;
                sb.append(mapToJson(subMap));
            } else {
                sb.append("\"").append(escapeJson(String.valueOf(val))).append("\"");
            }
            count++;
        }
        sb.append("}");
        return sb.toString();
    }

    public static String successResponse(String message, String dataJson) {
        return "{\"success\":true,\"message\":\"" + escapeJson(message) + "\",\"data\":" + (dataJson == null ? "null" : dataJson) + "}";
    }

    public static String errorResponse(String message) {
        return "{\"success\":false,\"error\":\"" + escapeJson(message) + "\"}";
    }

    /**
     * Parses simple flat JSON string into key-value map.
     */
    public static Map<String, String> parseJsonObject(String json) {
        Map<String, String> map = new LinkedHashMap<>();
        if (json == null) return map;
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);
        json = json.trim();
        if (json.isEmpty()) return map;

        int len = json.length();
        int i = 0;
        while (i < len) {
            // Find key opening quote
            while (i < len && json.charAt(i) != '"') i++;
            if (i >= len) break;
            i++; // skip opening "
            StringBuilder keySb = new StringBuilder();
            while (i < len && json.charAt(i) != '"') {
                if (json.charAt(i) == '\\' && i + 1 < len) {
                    i++;
                }
                keySb.append(json.charAt(i));
                i++;
            }
            if (i < len) i++; // skip closing "
            String key = keySb.toString();

            // Find colon
            while (i < len && json.charAt(i) != ':') i++;
            if (i < len) i++; // skip :

            // Skip whitespace
            while (i < len && Character.isWhitespace(json.charAt(i))) i++;
            if (i >= len) break;

            // Read value
            StringBuilder valSb = new StringBuilder();
            if (json.charAt(i) == '"') {
                i++; // skip opening "
                while (i < len) {
                    if (json.charAt(i) == '\\' && i + 1 < len) {
                        i++;
                        valSb.append(json.charAt(i));
                        i++;
                        continue;
                    }
                    if (json.charAt(i) == '"') {
                        i++;
                        break;
                    }
                    valSb.append(json.charAt(i));
                    i++;
                }
            } else {
                // Number, boolean, null
                while (i < len && json.charAt(i) != ',' && json.charAt(i) != '}') {
                    valSb.append(json.charAt(i));
                    i++;
                }
            }

            map.put(key, valSb.toString().trim());

            // Skip until next comma
            while (i < len && json.charAt(i) != ',') i++;
            if (i < len && json.charAt(i) == ',') i++;
        }
        return map;
    }

    /**
     * Converts JSON map values to Student model.
     */
    public static Student parseStudent(Map<String, String> data) {
        Student s = new Student();
        if (data.containsKey("id") && !data.get("id").equalsIgnoreCase("null") && !data.get("id").isEmpty()) {
            try {
                s.setId(Integer.parseInt(data.get("id")));
            } catch (NumberFormatException ignored) {}
        }
        s.setRollNumber(data.getOrDefault("rollNumber", "").trim());
        s.setFirstName(data.getOrDefault("firstName", "").trim());
        s.setLastName(data.getOrDefault("lastName", "").trim());
        s.setEmail(data.getOrDefault("email", "").trim());
        s.setPhone(data.getOrDefault("phone", "").trim());
        s.setGender(data.getOrDefault("gender", "Other").trim());
        s.setCourse(data.getOrDefault("course", "").trim());
        try {
            s.setYearLevel(Integer.parseInt(data.getOrDefault("yearLevel", "1")));
        } catch (NumberFormatException e) {
            s.setYearLevel(1);
        }
        try {
            s.setGpa(Double.parseDouble(data.getOrDefault("gpa", "3.00")));
        } catch (NumberFormatException e) {
            s.setGpa(3.00);
        }
        s.setCity(data.getOrDefault("city", "").trim());
        s.setStatus(data.getOrDefault("status", "Active").trim());
        return s;
    }
}
