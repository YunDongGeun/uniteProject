package uniteProject.mvc.model;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {
    private Long id;
    private String username;
    private String password;
    private String role;
    private LocalDateTime createdAt;
}