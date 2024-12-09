package uniteProject.mvc.repository;

import lombok.RequiredArgsConstructor;
import uniteProject.mvc.model.FeeManagement;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class FeeManagementRepository {
    private final DataSource dataSource;

    public List<FeeManagement> findAll() {
        String sql = "SELECT * FROM fee_management";
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            List<FeeManagement> fees = new ArrayList<>();
            while (rs.next()) {
                fees.add(mapResultSetToFee(rs));
            }
            return fees;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find fees: " + e.getMessage());
        }
    }

    public Optional<FeeManagement> findByDormNameAndFeeType(String dormName, String feeType) {
        String sql = "SELECT * FROM fee_management WHERE dorm_name = ? AND fee_type = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, dormName);
            stmt.setString(2, feeType);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToFee(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find fee by dorm name and fee type: " + e.getMessage());
        }
    }

    public FeeManagement save(FeeManagement fee) {
        String sql = "INSERT INTO fee_management (dorm_name, fee_type, amount) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, fee.getDormName());
            stmt.setString(2, fee.getFeeType());
            stmt.setInt(3, fee.getAmount());

            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                fee.setId(generatedKeys.getLong(1));
            }
            return fee;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save fee: " + e.getMessage());
        }
    }

    private FeeManagement mapResultSetToFee(ResultSet rs) throws SQLException {
        return FeeManagement.builder()
                .id(rs.getLong("id"))
                .dormName(rs.getString("dorm_name"))
                .feeType(rs.getString("fee_type"))
                .amount(rs.getInt("amount"))
                .build();
    }
}