package uniteProject.mvc.repository;

import lombok.RequiredArgsConstructor;
import uniteProject.mvc.model.Room;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class RoomRepository {
    private final DataSource dataSource;

    public Optional<Room> findById(Long id) {
        String sql = "SELECT * FROM room WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToRoom(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find room: " + e.getMessage());
        }
    }

    public List<Room> findAllAvailableRooms() {
        String sql = """
      SELECT r.* FROM room r LEFT JOIN room_status rs ON r.id = rs.room_id
      GROUP BY r.id, r.dormitory_id, r.room_number, r.room_type
      HAVING COUNT(rs.id) < r.room_type
     """;

        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            List<Room> rooms = new ArrayList<>();
            while (rs.next()) {
                rooms.add(mapResultSetToRoom(rs));
            }
            return rooms;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find available rooms: " + e.getMessage());
        }
    }

    private Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        return Room.builder()
                .id(rs.getLong("id"))
                .dormitoryId(rs.getLong("dormitory_id"))
                .roomNumber(rs.getInt("room_number"))
                .roomType(rs.getString("room_type"))
                .build();
    }
}

