package uniteProject.mvc.repository;

import lombok.RequiredArgsConstructor;
import uniteProject.mvc.model.TBCertificate;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@RequiredArgsConstructor
public class DocumentRepository {
    private final DataSource dataSource;

    public boolean existsByApplicationId(Long applicationId) {
        String sql = "SELECT COUNT(*) FROM tb_certificate WHERE application_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, applicationId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check certificate existence: " + e.getMessage());
        }
    }

    public Optional<TBCertificate> findByApplicationId(Long applicationId) {
        String sql = "SELECT * FROM tb_certificate WHERE application_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, applicationId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToTBCertificate(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find certificate: " + e.getMessage());
        }
    }

    public TBCertificate save(TBCertificate certificate) {
        String sql = """
           INSERT INTO tb_certificate (application_id, image, uploaded_at) VALUES (?, ?, ?)
           """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, certificate.getApplicationId());
            if (certificate.getImage() != null) {
                stmt.setBytes(2, certificate.getImage());
            } else {
                stmt.setNull(2, Types.BINARY);
            }
            if (certificate.getUploadedAt() != null) {
                stmt.setTimestamp(3, Timestamp.valueOf(certificate.getUploadedAt()));
            } else {
                stmt.setNull(3, Types.TIMESTAMP);
            }
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                certificate.setId(generatedKeys.getLong(1));
            }
            return certificate;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save certificate: " + e.getMessage());
        }
    }

    private TBCertificate mapResultSetToTBCertificate(ResultSet rs) throws SQLException {
        return TBCertificate.builder()
                .id(rs.getLong("id"))
                .applicationId(rs.getLong("application_id"))
                .image(rs.getBytes("image"))
                .uploadedAt(rs.getTimestamp("uploaded_at").toLocalDateTime())
                .build();
    }
}