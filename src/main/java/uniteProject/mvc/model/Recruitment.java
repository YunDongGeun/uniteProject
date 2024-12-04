package uniteProject.mvc.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recruitment {
    private Long id;
    private String dormName;
    private Integer capacity;
}