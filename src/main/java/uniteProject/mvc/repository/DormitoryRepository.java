package uniteProject.mvc.repository;

import lombok.RequiredArgsConstructor;
import uniteProject.mvc.model.Dormitory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@RequiredArgsConstructor
public class DormitoryRepository {
    private final DataSource dataSource;

    public Optional<Dormitory> findById(Long id) {
        String sql = "SELECT * FROM dormitory WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToDormitory(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find dormitory: " + e.getMessage());
        }
    }

    private Dormitory mapResultSetToDormitory(ResultSet rs) throws SQLException {
        return Dormitory.builder()
                .id(rs.getLong("id"))
                .dormName(rs.getString("dorm_name"))
                .gender(rs.getString("gender").charAt(0))
                .build();
    }
}
