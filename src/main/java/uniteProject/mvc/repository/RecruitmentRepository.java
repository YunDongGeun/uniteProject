package uniteProject.mvc.repository;

import lombok.RequiredArgsConstructor;
import uniteProject.mvc.model.Recruitment;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@RequiredArgsConstructor
public class RecruitmentRepository {
    private final DataSource dataSource;

    public Optional<Recruitment> findByDormName(String dormName) {
        String sql = "SELECT * FROM recruitment WHERE dorm_name = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, dormName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToRecruitment(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find recruitment: " + e.getMessage());
        }
    }

    public Optional<Recruitment> findById(Long id) {
        String sql = "SELECT * FROM recruitment WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToRecruitment(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find recruitment: " + e.getMessage());
        }
    }

    private Recruitment mapResultSetToRecruitment(ResultSet rs) throws SQLException {
        return Recruitment.builder()
                .id(rs.getLong("id"))
                .dormName(rs.getString("dorm_name"))
                .capacity(rs.getInt("capacity"))
                .build();
    }
}