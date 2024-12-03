package uniteProject.persistence.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class StudentDto {
    private int id;
    private String username;
    private String password;
    private String role;
    private String studentNumber;
    private String studentType;  // 학부생/대학원생
    private String major;
    private LocalDateTime createdAt;
}