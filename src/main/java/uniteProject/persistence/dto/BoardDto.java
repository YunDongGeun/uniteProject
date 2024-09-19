package uniteProject.persistence.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class BoardDto {
    private Long id;
    private String title;
    private String writer;
    private String contents;
    private LocalDateTime regdate;
    private int hit;
}
