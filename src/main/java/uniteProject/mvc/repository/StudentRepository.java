package uniteProject.mvc.repository;

import lombok.RequiredArgsConstructor;
import uniteProject.mvc.model.Student;
import uniteProject.persistence.PooledDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@RequiredArgsConstructor
public class StudentRepository {
    private final DataSource dataSource;

    public Optional<Student> findByStudentNumber(String studentNumber) {
        String sql = "SELECT * FROM students WHERE student_number = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, studentNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToStudent(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find student: " + e.getMessage());
        }
    }

    public Optional<Student> findById(Long id) {
        String sql = "SELECT * FROM students WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToStudent(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find student: " + e.getMessage());
        }
    }

    public Student save(Student student) {
        if (student.getId() == null) {
            return insert(student);
        }
        return update(student);
    }

    private Student insert(Student student) {
        String sql = """
            INSERT INTO students 
            (member_id, name, student_number, student_type, major, gpa, 
             distance_from_school, submit_document) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setStudentParameters(stmt, student);
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                student.setId(generatedKeys.getLong(1));
            }
            return student;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save student: " + e.getMessage());
        }
    }

    private Student update(Student student) {
        String sql = """
            UPDATE students 
            SET member_id = ?, name = ?, student_number = ?, student_type = ?, 
                major = ?, gpa = ?, distance_from_school = ?, submit_document = ? 
            WHERE id = ?
            """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            setStudentParameters(stmt, student);
            stmt.setLong(9, student.getId());
            stmt.executeUpdate();
            return student;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update student: " + e.getMessage());
        }
    }

    private void setStudentParameters(PreparedStatement stmt, Student student) throws SQLException {
        stmt.setLong(1, student.getMemberId());
        stmt.setString(2, student.getName());
        stmt.setString(3, student.getStudentNumber());
        stmt.setString(4, student.getStudentType());
        stmt.setString(5, student.getMajor());
        stmt.setDouble(6, student.getGpa() != null ? student.getGpa() : 0.0);
        stmt.setDouble(7, student.getDistanceFromSchool() != null ? student.getDistanceFromSchool() : 0.0);
        stmt.setBoolean(8, student.getSubmitDocument() != null && student.getSubmitDocument());
    }

    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        return Student.builder()
                .id(rs.getLong("id"))
                .memberId(rs.getLong("member_id"))
                .name(rs.getString("name"))
                .studentNumber(rs.getString("student_number"))
                .studentType(rs.getString("student_type"))
                .major(rs.getString("major"))
                .gpa(rs.getDouble("gpa"))
                .distanceFromSchool(rs.getDouble("distance_from_school"))
                .submitDocument(rs.getBoolean("submit_document"))
                .build();
    }
}
