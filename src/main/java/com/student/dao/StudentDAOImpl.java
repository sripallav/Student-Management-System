package com.student.dao;

import com.student.config.DatabaseConfig;
import com.student.model.Student;

import java.sql.*;
import java.util.*;

/**
 * JDBC Implementation of StudentDAO.
 */
public class StudentDAOImpl implements StudentDAO {

    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id"));
        s.setRollNumber(rs.getString("roll_number"));
        s.setFirstName(rs.getString("first_name"));
        s.setLastName(rs.getString("last_name"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        s.setGender(rs.getString("gender"));
        s.setCourse(rs.getString("course"));
        s.setYearLevel(rs.getInt("year_level"));
        s.setGpa(rs.getDouble("gpa"));
        s.setCity(rs.getString("city"));
        s.setStatus(rs.getString("status"));
        Timestamp cat = rs.getTimestamp("created_at");
        s.setCreatedAt(cat != null ? cat.toString() : null);
        Timestamp uat = rs.getTimestamp("updated_at");
        s.setUpdatedAt(uat != null ? uat.toString() : null);
        return s;
    }

    @Override
    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToStudent(rs));
            }
        } catch (Exception e) {
            System.err.println("[StudentDAOImpl] Error in getAllStudents: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Student> searchStudents(String query, String course, String status) {
        List<Student> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM students WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            String q = "%" + query.trim().toLowerCase() + "%";
            sql.append("AND (LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ? OR LOWER(roll_number) LIKE ? OR LOWER(email) LIKE ? OR LOWER(city) LIKE ?) ");
            params.add(q);
            params.add(q);
            params.add(q);
            params.add(q);
            params.add(q);
        }

        if (course != null && !course.trim().isEmpty() && !course.equalsIgnoreCase("all")) {
            sql.append("AND course = ? ");
            params.add(course.trim());
        }

        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("all")) {
            sql.append("AND status = ? ");
            params.add(status.trim());
        }

        sql.append("ORDER BY id DESC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToStudent(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("[StudentDAOImpl] Error in searchStudents: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Student getStudentById(int id) {
        String sql = "SELECT * FROM students WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStudent(rs);
                }
            }
        } catch (Exception e) {
            System.err.println("[StudentDAOImpl] Error in getStudentById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Student getStudentByRollNumber(String rollNumber) {
        String sql = "SELECT * FROM students WHERE roll_number = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStudent(rs);
                }
            }
        } catch (Exception e) {
            System.err.println("[StudentDAOImpl] Error in getStudentByRollNumber: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean addStudent(Student student) {
        String sql = "INSERT INTO students (roll_number, first_name, last_name, email, phone, gender, course, year_level, gpa, city, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, student.getRollNumber());
            ps.setString(2, student.getFirstName());
            ps.setString(3, student.getLastName());
            ps.setString(4, student.getEmail());
            ps.setString(5, student.getPhone());
            ps.setString(6, student.getGender());
            ps.setString(7, student.getCourse());
            ps.setInt(8, student.getYearLevel());
            ps.setDouble(9, student.getGpa());
            ps.setString(10, student.getCity());
            ps.setString(11, student.getStatus() != null ? student.getStatus() : "Active");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        student.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (Exception e) {
            System.err.println("[StudentDAOImpl] Error in addStudent: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean updateStudent(Student student) {
        String sql = "UPDATE students SET roll_number = ?, first_name = ?, last_name = ?, email = ?, " +
                "phone = ?, gender = ?, course = ?, year_level = ?, gpa = ?, city = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getRollNumber());
            ps.setString(2, student.getFirstName());
            ps.setString(3, student.getLastName());
            ps.setString(4, student.getEmail());
            ps.setString(5, student.getPhone());
            ps.setString(6, student.getGender());
            ps.setString(7, student.getCourse());
            ps.setInt(8, student.getYearLevel());
            ps.setDouble(9, student.getGpa());
            ps.setString(10, student.getCity());
            ps.setString(11, student.getStatus());
            ps.setInt(12, student.getId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("[StudentDAOImpl] Error in updateStudent: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean deleteStudent(int id) {
        String sql = "DELETE FROM students WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("[StudentDAOImpl] Error in deleteStudent: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean rollNumberExists(String rollNumber, Integer excludeId) {
        String sql = excludeId == null ?
                "SELECT COUNT(*) FROM students WHERE roll_number = ?" :
                "SELECT COUNT(*) FROM students WHERE roll_number = ? AND id != ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNumber);
            if (excludeId != null) {
                ps.setInt(2, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            System.err.println("[StudentDAOImpl] Error in rollNumberExists: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean emailExists(String email, Integer excludeId) {
        String sql = excludeId == null ?
                "SELECT COUNT(*) FROM students WHERE email = ?" :
                "SELECT COUNT(*) FROM students WHERE email = ? AND id != ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            if (excludeId != null) {
                ps.setInt(2, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            System.err.println("[StudentDAOImpl] Error in emailExists: " + e.getMessage());
        }
        return false;
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new LinkedHashMap<>();
        int totalStudents = 0;
        int activeStudents = 0;
        int inactiveStudents = 0;
        double avgGpa = 0.0;
        Map<String, Integer> courseCounts = new LinkedHashMap<>();

        try (Connection conn = DatabaseConfig.getConnection()) {
            // General metrics
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) AS total, " +
                         "SUM(CASE WHEN status = 'Active' THEN 1 ELSE 0 END) AS active_cnt, " +
                         "SUM(CASE WHEN status = 'Inactive' THEN 1 ELSE 0 END) AS inactive_cnt, " +
                         "AVG(gpa) AS avg_gpa FROM students")) {
                if (rs.next()) {
                    totalStudents = rs.getInt("total");
                    activeStudents = rs.getInt("active_cnt");
                    inactiveStudents = rs.getInt("inactive_cnt");
                    avgGpa = rs.getDouble("avg_gpa");
                }
            }

            // Course breakdown
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT course, COUNT(*) AS count FROM students GROUP BY course ORDER BY count DESC")) {
                while (rs.next()) {
                    courseCounts.put(rs.getString("course"), rs.getInt("count"));
                }
            }
        } catch (Exception e) {
            System.err.println("[StudentDAOImpl] Error in getStatistics: " + e.getMessage());
        }

        stats.put("totalStudents", totalStudents);
        stats.put("activeStudents", activeStudents);
        stats.put("inactiveStudents", inactiveStudents);
        stats.put("averageGpa", Math.round(avgGpa * 100.0) / 100.0);
        stats.put("courseDistribution", courseCounts);

        return stats;
    }
}
