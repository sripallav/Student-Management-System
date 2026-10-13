package com.student.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

/**
 * Database Configuration and Connection Provider using JDBC.
 */
public class DatabaseConfig {

    private static String dbUrl = "jdbc:mysql://localhost:3306/student_management_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String dbUser = "root";
    private static String dbPassword = "sripallav@.y";
    private static int serverPort = 8080;

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try (InputStream in = DatabaseConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                if (props.getProperty("db.url") != null) dbUrl = props.getProperty("db.url");
                if (props.getProperty("db.user") != null) dbUser = props.getProperty("db.user");
                if (props.getProperty("db.password") != null) dbPassword = props.getProperty("db.password");
                if (props.getProperty("server.port") != null) {
                    try {
                        serverPort = Integer.parseInt(props.getProperty("server.port"));
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (Exception e) {
            System.out.println("[Config] Using default configuration properties: " + e.getMessage());
        }

        // Environment variables override properties if present
        String envUser = System.getenv("DB_USERNAME");
        if (envUser != null && !envUser.isBlank()) {
            dbUser = envUser;
        }
        String envPass = System.getenv("DB_PASSWORD");
        if (envPass != null && !envPass.isBlank()) {
            dbPassword = envPass;
        }

        // Register MySQL Driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConfig] MySQL Driver class not found in classpath: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    public static int getServerPort() {
        return serverPort;
    }

    public static String getDbUser() {
        return dbUser;
    }

    public static String getDbUrl() {
        return dbUrl;
    }

    /**
     * Initializes database schema and seed data if table does not exist.
     */
    public static void initDatabase() {
        System.out.println("[DatabaseConfig] Verifying database schema and initial data...");
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Create table if not exists
            String createTableSql = "CREATE TABLE IF NOT EXISTS students (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "roll_number VARCHAR(30) NOT NULL UNIQUE," +
                    "first_name VARCHAR(50) NOT NULL," +
                    "last_name VARCHAR(50) NOT NULL," +
                    "email VARCHAR(100) NOT NULL UNIQUE," +
                    "phone VARCHAR(20) NOT NULL," +
                    "gender VARCHAR(10) NOT NULL," +
                    "course VARCHAR(80) NOT NULL," +
                    "year_level INT NOT NULL," +
                    "gpa DECIMAL(3, 2) NOT NULL DEFAULT 3.00," +
                    "city VARCHAR(60) NOT NULL," +
                    "status VARCHAR(20) NOT NULL DEFAULT 'Active'," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "INDEX idx_course (course)," +
                    "INDEX idx_status (status)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            stmt.execute(createTableSql);

            // Check if any records exist
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM students")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("[DatabaseConfig] Seeding default student records...");
                    String seedSql = "INSERT INTO students (roll_number, first_name, last_name, email, phone, gender, course, year_level, gpa, city, status) VALUES " +
                            "('SMS2026-001', 'Sri', 'Pallav', 'sripallav@example.com', '9876543210', 'Male', 'Computer Science', 3, 3.92, 'Hyderabad', 'Active'), " +
                            "('SMS2026-002', 'Aarav', 'Sharma', 'aarav.sharma@example.com', '9123456780', 'Male', 'Artificial Intelligence', 2, 3.85, 'Bengaluru', 'Active'), " +
                            "('SMS2026-003', 'Ananya', 'Reddy', 'ananya.reddy@example.com', '9234567891', 'Female', 'Data Science', 4, 3.98, 'Hyderabad', 'Active'), " +
                            "('SMS2026-004', 'Rohan', 'Verma', 'rohan.verma@example.com', '9345678912', 'Male', 'Information Technology', 1, 3.40, 'Pune', 'Active'), " +
                            "('SMS2026-005', 'Priya', 'Nair', 'priya.nair@example.com', '9456789123', 'Female', 'Computer Science', 3, 3.75, 'Chennai', 'Active'), " +
                            "('SMS2026-006', 'Karthik', 'Iyer', 'karthik.iyer@example.com', '9567891234', 'Male', 'Cyber Security', 2, 3.60, 'Mumbai', 'Active'), " +
                            "('SMS2026-007', 'Sneha', 'Patel', 'sneha.patel@example.com', '9678912345', 'Female', 'Electronics', 4, 3.88, 'Ahmedabad', 'Active'), " +
                            "('SMS2026-008', 'Vikram', 'Rao', 'vikram.rao@example.com', '9789012345', 'Male', 'Data Science', 2, 3.15, 'Delhi', 'Inactive'), " +
                            "('SMS2026-009', 'Meera', 'Joshi', 'meera.joshi@example.com', '9890123456', 'Female', 'Artificial Intelligence', 1, 3.95, 'Pune', 'Active'), " +
                            "('SMS2026-010', 'Arjun', 'Das', 'arjun.das@example.com', '9901234567', 'Male', 'Computer Science', 4, 3.50, 'Kolkata', 'Active')";
                    stmt.executeUpdate(seedSql);
                    System.out.println("[DatabaseConfig] Seed data inserted successfully.");
                } else {
                    System.out.println("[DatabaseConfig] Database already initialized with existing student records.");
                }
            }

            System.out.println("[DatabaseConfig] Database connection and schema verified successfully.");
        } catch (Exception e) {
            System.err.println("[DatabaseConfig] Database initialization warning: " + e.getMessage());
        }
    }
}
