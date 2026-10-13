package com.student.dao;

import com.student.model.Student;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object (DAO) Interface for Student operations.
 */
public interface StudentDAO {
    List<Student> getAllStudents();
    List<Student> searchStudents(String query, String course, String status);
    Student getStudentById(int id);
    Student getStudentByRollNumber(String rollNumber);
    boolean addStudent(Student student);
    boolean updateStudent(Student student);
    boolean deleteStudent(int id);
    boolean rollNumberExists(String rollNumber, Integer excludeId);
    boolean emailExists(String email, Integer excludeId);
    Map<String, Object> getStatistics();
}
