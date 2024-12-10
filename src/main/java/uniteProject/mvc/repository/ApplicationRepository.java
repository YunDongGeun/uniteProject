package uniteProject.mvc.repository;

import lombok.RequiredArgsConstructor;
import uniteProject.mvc.model.Application;
import uniteProject.persistence.PooledDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ApplicationRepository {
    private final DataSource dataSource;

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

    public boolean isApplicationAvailable(String studentId) {
        String sql = "SELECT COUNT(*) FROM application WHERE student_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                return count < 2;  // 2개 미만이면 true, 2개 이상이면 false
            }
            return true;  // 결과가 없으면 신청 가능
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check application count: " + e.getMessage());
        }
    }

    public boolean isValidApplication(Long studentId, Long recruitmentId, int preference) {
        String sql = """
        SELECT COUNT(*) FROM application 
        WHERE student_id = ? AND (
            recruitment_id = ? OR  -- 같은 생활관 체크
            preference = ?         -- 같은 지망 순위 체크
        ) AND status != '거부'     -- 거부된 신청은 제외
    """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, studentId);
            stmt.setLong(2, recruitmentId);
            stmt.setInt(3, preference);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0; // 중복된 신청이 없으면 true
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to validate application: " + e.getMessage());
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
        StringBuilder sql = new StringBuilder(
                "SELECT a.id, a.student_id, a.recruitment_id, a.status, a.is_paid, " +
                        "a.preference, a.priority_score, a.created_at, a.update_at, a.room_type, a.meal_type " +
                        "FROM application a WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND a.status = ?");
            params.add(status);
        }

        if (preference != null && !preference.trim().isEmpty()) {
            sql.append(" AND a.preference = ?");
            params.add(preference);
        }

        sql.append(" ORDER BY a.created_at DESC");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            List<Application> applications = new ArrayList<>();

            while (rs.next()) {
                Application app = mapResultSetToApplication(rs);
                if (app.getCreatedAt() == null) {
                    throw new SQLException("Created date cannot be null for application ID: " + app.getId());
                }
                applications.add(app);
            }

            return applications;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find applications: " + e.getMessage());
        }
    }

    // 특정 생활관의 선발된 지원자 수를 조회하는 메서드
    public int countSelectedApplicationsByRecruitmentId(Long recruitmentId) {
        String sql = "SELECT COUNT(*) FROM application WHERE recruitment_id = ? AND status = '선발'";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, recruitmentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count selected applications: " + e.getMessage());
        }
    }

    public List<Application> findAllByStudentIdAndStatusNot(Long studentId, String status) {
        String sql = "SELECT * FROM application WHERE student_id = ? AND status != ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, studentId);
            stmt.setString(2, status);
            ResultSet rs = stmt.executeQuery();

            List<Application> applications = new ArrayList<>();
            while (rs.next()) {
                applications.add(mapResultSetToApplication(rs));
            }
            return applications;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find applications by student id and not status: " + e.getMessage());
        }
    }

    public List<Application> findAllByStatusAndPreferenceOrderByPriorityScoreDesc(String status, int preference) {
        String sql = """
            SELECT * FROM application 
            WHERE status = ? AND preference = ? 
            ORDER BY priority_score DESC
        """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, preference);
            ResultSet rs = stmt.executeQuery();

            List<Application> applications = new ArrayList<>();
            while (rs.next()) {
                applications.add(mapResultSetToApplication(rs));
            }
            return applications;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find applications by status and preference: " + e.getMessage());
        }
    }

    public Optional<Application> findByStudentIdAndPreference(Long studentId, int preference) {
        String sql = "SELECT * FROM application WHERE student_id = ? AND preference = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, studentId);
            stmt.setInt(2, preference);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToApplication(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find application by student id and preference: " + e.getMessage());
        }
    }

    public List<Application> findAllByOrderByPriorityScoreDesc() {
        String sql = "SELECT * FROM application ORDER BY priority_score DESC";
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            List<Application> applications = new ArrayList<>();
            while (rs.next()) {
                applications.add(mapResultSetToApplication(rs));
            }
            return applications;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find applications: " + e.getMessage());
        }
    }

    public List<Application> findAllByStatus(String status) {
        String sql = "SELECT * FROM application WHERE status = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            List<Application> applications = new ArrayList<>();
            while (rs.next()) {
                applications.add(mapResultSetToApplication(rs));
            }
            return applications;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find applications by status: " + e.getMessage());
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
            INSERT INTO application (student_id, recruitment_id, room_type, meal_type, status, is_paid, preference, 
                                   priority_score, created_at, update_at) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
            UPDATE application 
            SET student_id = ?, recruitment_id = ?, status = ?, is_paid = ?, 
                preference = ?, priority_score = ?, update_at = ? 
            WHERE id = ?
            """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            setUpdateParameters(stmt, application);
            stmt.setLong(8, application.getId());
            stmt.executeUpdate();
            return application;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update application: " + e.getMessage());
        }
    }

    public int countSelectedApplicationsByRecruitmentIdAndRoomType(Long recruitmentId, int roomType) {
        String sql = """
        SELECT COUNT(*) 
        FROM application 
        WHERE recruitment_id = ? 
        AND room_type = ? 
        AND status = '선발'
    """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, recruitmentId);
            stmt.setInt(2, roomType);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count selected applications: " + e.getMessage());
        }
    }

    public void rejectOtherApplications(Long studentId, Long selectedApplicationId) {
        String sql = """
    UPDATE application 
    SET status = '거부', update_at = ? 
    WHERE student_id = ? 
    AND id != ? 
    AND preference > 1
    AND status = '대기'
""";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(2, studentId);
            stmt.setLong(3, selectedApplicationId);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to reject other applications: " + e.getMessage());
        }
    }

    private void setUpdateParameters(PreparedStatement stmt, Application application) throws SQLException {
        stmt.setLong(1, application.getStudentId());
        stmt.setLong(2, application.getRecruitmentId());
        stmt.setString(3, application.getStatus());
        stmt.setBoolean(4, application.getIsPaid());
        stmt.setInt(5, application.getPreference());
        stmt.setInt(6, application.getPriorityScore());
        stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));  // update_at
        stmt.setLong(8, application.getId());
    }

    private void setApplicationParameters(PreparedStatement stmt, Application application) throws SQLException {
        stmt.setLong(1, application.getStudentId());
        stmt.setLong(2, application.getRecruitmentId());
        stmt.setInt(3, application.getRoomType());
        stmt.setInt(4, application.getMealType());
        stmt.setString(5, application.getStatus());
        stmt.setBoolean(6, application.getIsPaid());
        stmt.setInt(7, application.getPreference());
        stmt.setInt(8, application.getPriorityScore());
        stmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
        stmt.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
    }

    private Application mapResultSetToApplication(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt == null) {
            throw new SQLException("Created date is required but was null");
        }

        Timestamp updateAt = rs.getTimestamp("update_at");

        return Application.builder()
                .id(rs.getLong("id"))
                .studentId(rs.getLong("student_id"))
                .recruitmentId(rs.getLong("recruitment_id"))
                .roomType(rs.getInt("room_type"))
                .mealType(rs.getInt("meal_type"))
                .status(rs.getString("status"))
                .isPaid(rs.getBoolean("is_paid"))
                .preference(rs.getInt("preference"))
                .priorityScore(rs.getInt("priority_score"))
                .createdAt(createdAt.toLocalDateTime())
                .updateAt(updateAt != null ? updateAt.toLocalDateTime() : null)
                .build();
    }

}
