package uniteProject.mvc.repository;

import lombok.RequiredArgsConstructor;
import uniteProject.mvc.model.Account;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@RequiredArgsConstructor
public class AccountRepository {
    private final DataSource dataSource;

    public Optional<Account> findByStudentId(Long studentId) {
        String sql = "SELECT * FROM account WHERE student_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, studentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToAccount(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find account: " + e.getMessage());
        }
    }

    public Account save(Account account) {
        if (existsByStudentId(account.getStudentId())) {
            return update(account);
        }
        return insert(account);
    }

    private boolean existsByStudentId(Long studentId) {
        String sql = "SELECT COUNT(*) FROM account WHERE student_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, studentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check account existence: " + e.getMessage());
        }
    }

    private Account insert(Account account) {
        String sql = "INSERT INTO account (student_id, bank_name, account_number) VALUES (?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setAccountParameters(stmt, account);
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                account.setId(generatedKeys.getLong(1));
            }
            return account;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save account: " + e.getMessage());
        }
    }

    private Account update(Account account) {
        String sql = "UPDATE account SET bank_name = ?, account_number = ? WHERE student_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, account.getBankName());
            stmt.setString(2, account.getAccountNumber());
            stmt.setLong(3, account.getStudentId());

            stmt.executeUpdate();
            return account;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update account: " + e.getMessage());
        }
    }

    private void setAccountParameters(PreparedStatement stmt, Account account) throws SQLException {
        stmt.setLong(1, account.getStudentId());
        stmt.setString(2, account.getBankName());
        stmt.setString(3, account.getAccountNumber());
    }

    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        return Account.builder()
                .id(rs.getLong("id"))
                .studentId(rs.getLong("student_id"))
                .bankName(rs.getString("bank_name"))
                .accountNumber(rs.getString("account_number"))
                .build();
    }
}
