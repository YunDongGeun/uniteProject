package uniteProject.mvc.repository;

import lombok.RequiredArgsConstructor;
import uniteProject.mvc.model.Room;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
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

    public int countAvailableCapacityByDormitoryAndType(Long dormitoryId, int roomType) {
        String sql = """
            SELECT COUNT(*) * room_type as total_capacity
            FROM room r
            WHERE r.dormitory_id = ? AND r.room_type = ?
        """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, dormitoryId);
            stmt.setInt(2, roomType);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total_capacity");
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count available capacity: " + e.getMessage());
        }
    }

    public String findAvailableBed(Room room) {
        String sql = "SELECT bed_number FROM room_status WHERE room_id = ? ORDER BY bed_number";
        List<String> occupiedBeds = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, room.getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                occupiedBeds.add(rs.getString("bed_number"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find occupied beds: " + e.getMessage());
        }

        List<String> availableBeds = room.getRoomType() == 2 ?
                Arrays.asList("A", "B") :
                Arrays.asList("A", "B", "C", "D");

        for (String bed : availableBeds) {
            if (!occupiedBeds.contains(bed)) {
                return bed;
            }
        }

        throw new RuntimeException("사용 가능한 침대가 없습니다.");
    }


    public List<Room> findAvailableRoomsByDormitoryAndType(Long dormitoryId, int roomType) {
        String sql = """
            SELECT r.* 
            FROM room r 
            LEFT JOIN room_status rs ON r.id = rs.room_id
            WHERE r.dormitory_id = ? 
            AND r.room_type = ?
            GROUP BY r.id, r.dormitory_id, r.room_number, r.room_type
            HAVING COUNT(rs.id) < r.room_type
            ORDER BY r.room_number
        """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, dormitoryId);
            stmt.setInt(2, roomType);
            ResultSet rs = stmt.executeQuery();

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
                .roomType(rs.getInt("room_type"))
                .build();
    }
}

