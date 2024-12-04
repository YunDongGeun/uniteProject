package uniteProject.persistence.dto.req_res;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Response {
    private final byte type;
    private final byte code;
    private final byte[] data;
}