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

    public boolean isValidRoomType(String dormName, int roomType) {
        // 오름관은 2인실만 가능
        if (dormName.startsWith("오름관") && roomType != 2) {
            return false;
        }
        // 푸름관은 2인실, 4인실 가능
        if (dormName.startsWith("푸름관") && (roomType != 2 && roomType != 4)) {
            return false;
        }
        return true;
    }

    public boolean isValidMealType(String dormName, int mealType) {
        // 오름관은 식사 필수 (5일식 또는 7일식)
        if (dormName.startsWith("오름관")) {
            return mealType == 5 || mealType == 7;
        }
        // 푸름관은 식사 선택 가능 (안함(0), 5일식, 7일식)
        if (dormName.startsWith("푸름관")) {
            return mealType == 0 || mealType == 5 || mealType == 7;
        }
        return false;
    }

    public int calculateTotalFee(String dormName, int roomType, int mealType) {
        // 입력값 검증
        if (!isValidRoomType(dormName, roomType)) {
            throw new RuntimeException(String.format(
                    "해당 생활관(%s)에서는 %d인실을 선택할 수 없습니다.",
                    dormName, roomType
            ));
        }

        if (!isValidMealType(dormName, mealType)) {
            throw new RuntimeException(String.format(
                    "해당 생활관(%s)에서는 %d일식을 선택할 수 없습니다.",
                    dormName, mealType
            ));
        }

        StringBuilder sql = new StringBuilder(
                "SELECT fee_type, amount FROM fee_management WHERE dorm_name = ? AND fee_type IN (");

        // ROOM 타입은 항상 포함
        sql.append("'ROOM_").append(roomType).append("'");

        // MEAL 타입은 0이 아닐 때만 포함
        if (mealType > 0) {
            sql.append(", 'MEAL_").append(mealType).append("'");
        }
        sql.append(")");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql.toString())) {

            stmt.setString(1, dormName);
            ResultSet rs = stmt.executeQuery();

            int totalFee = 0;
            boolean hasRoomFee = false;
            boolean hasMealFee = false;

            while (rs.next()) {
                String feeType = rs.getString("fee_type");
                int amount = rs.getInt("amount");

                if (feeType.startsWith("ROOM_")) {
                    hasRoomFee = true;
                    totalFee += amount;
                } else if (feeType.startsWith("MEAL_")) {
                    hasMealFee = true;
                    totalFee += amount;
                }
            }

            // 필수 요금 확인
            if (!hasRoomFee) {
                throw new RuntimeException("방 타입에 대한 요금 정보를 찾을 수 없습니다.");
            }
            if (mealType > 0 && !hasMealFee) {
                throw new RuntimeException("식사 타입에 대한 요금 정보를 찾을 수 없습니다.");
            }

            return totalFee;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to calculate fee: " + e.getMessage());
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

    private FeeManagement mapResultSetToFee(ResultSet rs) throws SQLException {
        return FeeManagement.builder()
                .id(rs.getLong("id"))
                .dormName(rs.getString("dorm_name"))
                .feeType(rs.getString("fee_type"))
                .amount(rs.getInt("amount"))
                .build();
    }
}