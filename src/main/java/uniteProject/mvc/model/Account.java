package uniteProject.mvc.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {
    private Long id;
    private Long studentId;
    private String accountNumber;
    private String bankName;
}
