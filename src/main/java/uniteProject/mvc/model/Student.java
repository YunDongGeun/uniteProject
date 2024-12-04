package uniteProject.mvc.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {
    private Long id;
    private Long memberId;
    private String name;
    private String studentNumber;
    private String studentType;
    private String major;
    private Double gpa;
    private Double distanceFromSchool;
    private Boolean submitDocument;
}