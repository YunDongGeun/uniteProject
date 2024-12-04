package uniteProject.mvc.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    private Long id;
    private Long applicationId;
    private Integer amount;
    private String paymentStatus;
    private LocalDateTime paymentDate;
}
