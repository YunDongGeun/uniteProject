package uniteProject.persistence.dao;

import uniteProject.persistence.PooledDataSource;
import uniteProject.persistence.dto.MemberDto;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

public class MemberDao {
    private final DataSource dataSource;

    public MemberDao() {
        this.dataSource = PooledDataSource.getDataSource();
    }

    public Optional<MemberDto> findByUsername(String username) {
        String sql = "SELECT * FROM members WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapToMemberDto(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public boolean createUser(MemberDto user) {
        String sql = "INSERT INTO members (username, password, role, student_number, student_type, major) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            pstmt.setString(4, user.getStudentNumber());
            pstmt.setString(5, user.getStudentType());
            pstmt.setString(6, user.getMajor());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private MemberDto mapToMemberDto(ResultSet rs) throws SQLException {
        return MemberDto.builder()
                .id((long) rs.getInt("id"))
                .username(rs.getString("username"))
                .password(rs.getString("password"))
                .role(rs.getString("role"))
                .studentNumber(rs.getString("student_number"))
                .studentType(rs.getString("student_type"))
                .major(rs.getString("major"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build();
    }
}