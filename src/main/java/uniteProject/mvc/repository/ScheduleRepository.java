package uniteProject.mvc.repository;

import lombok.RequiredArgsConstructor;
import uniteProject.mvc.model.FeeManagement;
import uniteProject.mvc.model.Schedule;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ScheduleRepository {
    private final DataSource dataSource;

    public List<Schedule> findAllByOrderByStartTime() {
        String sql = "SELECT * FROM schedule ORDER BY start_time";
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            List<Schedule> schedules = new ArrayList<>();
            while (rs.next()) {
                schedules.add(mapResultSetToSchedule(rs));
            }
            return schedules;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find schedules: " + e.getMessage());
        }
    }

    public Schedule save(Schedule schedule) {
        String sql = "INSERT INTO schedule (event_name, start_time, end_time) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, schedule.getEventName());
            stmt.setTimestamp(2, Timestamp.valueOf(schedule.getStartTime()));
            stmt.setTimestamp(3, Timestamp.valueOf(schedule.getEndTime()));

            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                schedule.setId(generatedKeys.getLong(1));
            }
            return schedule;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save schedule: " + e.getMessage());
        }
    }

    private Schedule mapResultSetToSchedule(ResultSet rs) throws SQLException {
        return Schedule.builder()
                .id(rs.getLong("id"))
                .eventName(rs.getString("event_name"))
                .startTime(rs.getTimestamp("start_time").toLocalDateTime())
                .endTime(rs.getTimestamp("end_time").toLocalDateTime())
                .build();
    }
}


