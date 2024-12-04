package uniteProject.mvc.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomStatus {
    private Long id;
    private Long roomId;
    private Long studentId;
    private String bedNumber;
}