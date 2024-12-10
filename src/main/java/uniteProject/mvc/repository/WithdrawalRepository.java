package uniteProject.mvc.repository;

import lombok.RequiredArgsConstructor;
import uniteProject.mvc.model.Withdrawal;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class WithdrawalRepository {
    private final DataSource dataSource;

    public Optional<Withdrawal> findById(Long id) {
        String sql = "SELECT * FROM withdrawal WHERE id = ?";
        return getWithdrawal(id, sql);
    }

    public Optional<Withdrawal> findByStudentId(Long studentId) {
        String sql = "SELECT * FROM withdrawal WHERE student_id = ?";
        return getWithdrawal(studentId, sql);
    }

    private Optional<Withdrawal> getWithdrawal(Long id, String sql) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToWithdrawal(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find withdrawal: " + e.getMessage());
        }
    }
    public List<Withdrawal> findAll() {
        String sql = "SELECT * FROM withdrawal";
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            List<Withdrawal> withdrawals = new ArrayList<>();
            while (rs.next()) {
                withdrawals.add(mapResultSetToWithdrawal(rs));
            }
            return withdrawals;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find withdrawals: " + e.getMessage());
        }
    }

    public Withdrawal save(Withdrawal withdrawal) {
        if (withdrawal.getId() == null) {
            return insert(withdrawal);
        }
        return update(withdrawal);
    }

    private Withdrawal insert(Withdrawal withdrawal) {
        String sql = "INSERT INTO withdrawal (student_id, leave_date, status, refund_amount) VALUES (?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setWithdrawalParameters(stmt, withdrawal);

            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                withdrawal.setId(generatedKeys.getLong(1));
            }
            return withdrawal;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert withdrawal: " + e.getMessage());
        }
    }

    private Withdrawal update(Withdrawal withdrawal) {
        String sql = "UPDATE withdrawal SET student_id = ?, leave_date = ?, status = ?, refund_amount = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            setWithdrawalParameters(stmt, withdrawal);
            stmt.setLong(5, withdrawal.getId());

            stmt.executeUpdate();
            return withdrawal;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update withdrawal: " + e.getMessage());
        }
    }

    private void setWithdrawalParameters(PreparedStatement stmt, Withdrawal withdrawal) throws SQLException {
        stmt.setString(1, withdrawal.getStudentId());
        stmt.setTimestamp(2, Timestamp.valueOf(withdrawal.getLeaveDate()));
        stmt.setString(3, withdrawal.getStatus());
        stmt.setInt(4, withdrawal.getRefundAmount());
    }

    private Withdrawal mapResultSetToWithdrawal(ResultSet rs) throws SQLException {
        return Withdrawal.builder()
                .id(rs.getLong("id"))
                .studentId(rs.getString("student_id"))
                .leaveDate(rs.getTimestamp("leave_date").toLocalDateTime())
                .status(rs.getString("status"))
                .refundAmount(rs.getInt("refund_amount"))
                .build();
    }
}
