package uniteProject.mvc.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeManagement {
    private Long id;
    private String dormName;
    private String feeType;
    private Integer amount;
}
