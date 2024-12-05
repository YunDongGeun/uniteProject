package uniteProject.mvc.repository;

import lombok.RequiredArgsConstructor;
import uniteProject.mvc.model.RoomStatus;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@RequiredArgsConstructor
public class RoomStatusRepository {
    private final DataSource dataSource;

    public Optional<RoomStatus> findByStudentId(Long studentId) {
        String sql = "SELECT * FROM room_status WHERE student_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, studentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToRoomStatus(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find room status: " + e.getMessage());
        }
    }

    public RoomStatus save(RoomStatus roomStatus) {
        String sql = "INSERT INTO room_status (room_id, student_id, bed_number) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, roomStatus.getRoomId());
            stmt.setLong(2, roomStatus.getStudentId());
            stmt.setString(3, roomStatus.getBedNumber());

            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                roomStatus.setId(generatedKeys.getLong(1));
            }
            return roomStatus;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save room status: " + e.getMessage());
        }
    }

    private RoomStatus mapResultSetToRoomStatus(ResultSet rs) throws SQLException {
        return RoomStatus.builder()
                .id(rs.getLong("id"))
                .roomId(rs.getLong("room_id"))
                .studentId(rs.getLong("student_id"))
                .bedNumber(rs.getString("bed_number"))
                .build();
    }
}