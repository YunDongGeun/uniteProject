package uniteProject.mvc.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TBCertificate {
    private Long id;
    private Long applicationId;
    private byte[] image;
    private LocalDateTime uploadedAt;
}