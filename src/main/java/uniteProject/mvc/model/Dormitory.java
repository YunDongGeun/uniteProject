package uniteProject.mvc.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dormitory {
    private Long id;
    private String dormName;
    private Character gender;
}