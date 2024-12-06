package uniteProject.mvc.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {
    private Long id;
    private Long dormitoryId;
    private Integer roomNumber;
    private Integer roomType;
}