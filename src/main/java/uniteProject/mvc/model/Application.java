package uniteProject.mvc.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {
    private Long id;
    private Long studentId;
    private Long recruitmentId;
    private String status;
    private Boolean isPaid;
    private Integer preference;
    private Integer priorityScore;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
}
