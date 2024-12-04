package uniteProject.mvc.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {
    private Long id;
    private String eventName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
