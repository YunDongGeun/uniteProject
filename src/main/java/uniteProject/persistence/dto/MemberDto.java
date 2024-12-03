package uniteProject.persistence.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
public class MemberDto {

    private Long id;
    private String username;
    private String password;
    private String role;
    private LocalDateTime createAt;
}
