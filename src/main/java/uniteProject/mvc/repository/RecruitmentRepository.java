package uniteProject.mvc.repository;

import lombok.RequiredArgsConstructor;
import uniteProject.mvc.model.Recruitment;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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

    public List<Recruitment> findAll() {
        String sql = "SELECT * FROM recruitment";

        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<Recruitment> recruitments = new ArrayList<>();
            while (rs.next()) {
                recruitments.add(mapResultSetToRecruitment(rs));
            }
            return recruitments;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all recruitments: " + e.getMessage());
        }
    }

    public Recruitment save(Recruitment recruitment) {
        if (recruitment.getId() == null) {
            return insert(recruitment);
        }
        return update(recruitment);
    }

    private Recruitment insert(Recruitment recruitment) {
        String sql = "INSERT INTO recruitment (dorm_name, capacity) VALUES (?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setRecruitmentParameters(stmt, recruitment);
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                recruitment.setId(generatedKeys.getLong(1));
            }
            return recruitment;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert recruitment: " + e.getMessage());
        }
    }

    private Recruitment update(Recruitment recruitment) {
        String sql = "UPDATE recruitment SET dorm_name = ?, capacity = ? WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            setRecruitmentParameters(stmt, recruitment);
            stmt.setLong(3, recruitment.getId());

            int updatedRows = stmt.executeUpdate();
            if (updatedRows == 0) {
                throw new RuntimeException("해당 ID의 모집 정보를 찾을 수 없습니다: " + recruitment.getId());
            }

            return recruitment;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update recruitment: " + e.getMessage());
        }
    }

    private void setRecruitmentParameters(PreparedStatement stmt, Recruitment recruitment) throws SQLException {
        stmt.setString(1, recruitment.getDormName());
        stmt.setInt(2, recruitment.getCapacity());
    }

    private Recruitment mapResultSetToRecruitment(ResultSet rs) throws SQLException {
        return Recruitment.builder()
                .id(rs.getLong("id"))
                .dormName(rs.getString("dorm_name"))
                .capacity(rs.getInt("capacity"))
                .build();
    }
}