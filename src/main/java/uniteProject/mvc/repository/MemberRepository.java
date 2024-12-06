package uniteProject.mvc.repository;

import lombok.RequiredArgsConstructor;
import uniteProject.mvc.model.Member;
import uniteProject.persistence.PooledDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@RequiredArgsConstructor
public class MemberRepository {
    private final DataSource dataSource;

    public Optional<Member> findByUsername(String username) {
        String sql = "SELECT * FROM members WHERE username = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToMember(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find member: " + e.getMessage());
        }
    }

    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM members WHERE username = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check username existence: " + e.getMessage());
        }
    }

    public Member save(Member member) {
        if (member.getId() == null) {
            return insert(member);
        }
        return update(member);
    }

    private Member insert(Member member) {
        String sql = "INSERT INTO members (username, password, role, created_at) VALUES (?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, member.getUsername());
            stmt.setString(2, member.getPassword());
            stmt.setString(3, member.getRole());
            stmt.setTimestamp(4, Timestamp.valueOf(member.getCreatedAt()));

            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                member.setId(generatedKeys.getLong(1));
            }
            return member;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save member: " + e.getMessage());
        }
    }

    private Member update(Member member) {
        String sql = "UPDATE members SET username = ?, password = ?, role = ? WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, member.getUsername());
            stmt.setString(2, member.getPassword());
            stmt.setString(3, member.getRole());
            stmt.setLong(4, member.getId());

            stmt.executeUpdate();
            return member;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update member: " + e.getMessage());
        }
    }

    private Member mapResultSetToMember(ResultSet rs) throws SQLException {
        return Member.builder()
                .id(rs.getLong("id"))
                .username(rs.getString("username"))
                .password(rs.getString("password"))
                .role(rs.getString("role"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build();
    }
}