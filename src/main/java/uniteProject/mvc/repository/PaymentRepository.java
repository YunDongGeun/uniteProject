package uniteProject.mvc.repository;

import lombok.RequiredArgsConstructor;
import uniteProject.mvc.model.Payment;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class PaymentRepository {
    private final DataSource dataSource;

    public Optional<Payment> findByApplicationId(Long applicationId) {
        String sql = "SELECT * FROM payment WHERE application_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, applicationId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToPayment(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find payment: " + e.getMessage());
        }
    }

    public List<Payment> findAllByPaymentStatus(String status) {
        String sql = "SELECT * FROM payment WHERE payment_status = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();

            List<Payment> payments = new ArrayList<>();
            while (rs.next()) {
                payments.add(mapResultSetToPayment(rs));
            }
            return payments;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find payments: " + e.getMessage());
        }
    }

    public void save(Payment payment) {
        String sql = """
            INSERT INTO payment (application_id, amount, payment_status, payment_date) VALUES (?, ?, ?, ?)
            """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, payment.getApplicationId());
            stmt.setInt(2, payment.getAmount());
            stmt.setString(3, payment.getPaymentStatus());
            if (payment.getPaymentDate() != null) {
                stmt.setTimestamp(4, Timestamp.valueOf(payment.getPaymentDate()));
            } else {
                stmt.setNull(4, Types.TIMESTAMP);
            }

            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                payment.setId(generatedKeys.getLong(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save payment: " + e.getMessage());
        }
    }

    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        return Payment.builder()
                .id(rs.getLong("id"))
                .applicationId(rs.getLong("application_id"))
                .amount(rs.getInt("amount"))
                .paymentStatus(rs.getString("payment_status"))
                .paymentDate(rs.getTimestamp("payment_date").toLocalDateTime())
                .build();
    }
}