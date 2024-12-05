package uniteProject.mvc.repository;

import uniteProject.mvc.model.Application;
import uniteProject.persistence.PooledDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ApplicationRepository {
    private final DataSource dataSource;

    public ApplicationRepository() {
        this.dataSource = PooledDataSource.getDataSource();
    }

    public boolean existsByStudentId(Long studentId) {
        String sql = "SELECT COUNT(*) FROM application WHERE student_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, studentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check application existence: " + e.getMessage());
        }
    }

    public Optional<Application> findById(Long Id) {
        String sql = "SELECT * FROM application WHERE id = ?";

        return getApplication(Id, sql);
    }

    public Optional<Application> findByStudentId(Long studentId) {
        String sql = "SELECT * FROM application WHERE student_id = ?";

        return getApplication(studentId, sql);
    }

    private Optional<Application> getApplication(Long id, String sql) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToApplication(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find application: " + e.getMessage());
        }
    }

    public List<Application> findAllBySearchCriteria(String status, String preference) {
        StringBuilder sql = new StringBuilder("SELECT * FROM application WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status);
        }

        if (preference != null && !preference.trim().isEmpty()) {
            sql.append(" AND preference = ?");
            params.add(preference);
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            List<Application> applications = new ArrayList<>();

            while (rs.next()) {
                applications.add(mapResultSetToApplication(rs));
            }

            return applications;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find applications: " + e.getMessage());
        }
    }

    public Application save(Application application) {
        if (application.getId() == null) {
            return insert(application);
        }
        return update(application);
    }

    private Application insert(Application application) {
        String sql = """
            INSERT INTO application (student_id, status, is_paid, preference, priority_score, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setApplicationParameters(stmt, application);
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                application.setId(generatedKeys.getLong(1));
            }
            return application;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save application: " + e.getMessage());
        }
    }

    private Application update(Application application) {
        String sql = """
            UPDATE application SET student_id = ?, status = ?, is_paid = ?, preference = ?, priority_score = ?, updated_at = ? WHERE id = ?
            """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            setApplicationParameters(stmt, application);
            stmt.setLong(7, application.getId());
            stmt.executeUpdate();
            return application;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update application: " + e.getMessage());
        }
    }

    private void setApplicationParameters(PreparedStatement stmt, Application application) throws SQLException {
        stmt.setLong(1, application.getStudentId());
        stmt.setString(2, application.getStatus());
        stmt.setBoolean(3, application.getIsPaid());
        stmt.setString(4, application.getPreference());
        stmt.setInt(5, application.getPriorityScore());
        stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
        stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
    }

    private Application mapResultSetToApplication(ResultSet rs) throws SQLException {
        return Application.builder()
                .id(rs.getLong("id"))
                .studentId(rs.getLong("student_id"))
                .status(rs.getString("status"))
                .isPaid(rs.getBoolean("is_paid"))
                .preference(rs.getString("preference"))
                .priorityScore(rs.getInt("priority_score"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                .build();
    }
}
