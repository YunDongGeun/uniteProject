package uniteProject.mvc.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Withdrawal {
    private Long id;
    private Long studentId;
    private LocalDateTime leaveDate;
    private String status;
    private Integer refundAmount;
}
