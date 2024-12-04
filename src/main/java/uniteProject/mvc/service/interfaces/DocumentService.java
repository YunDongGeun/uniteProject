package uniteProject.mvc.service.interfaces;

import uniteProject.global.Protocol;
import uniteProject.persistence.dto.req_res.Response;

public interface DocumentService {
    Protocol submitTBCertificate(byte[] data);
    Protocol checkSubmissionStatus(byte[] data);

    byte[] handleDocumentRequest(byte code, byte[] data);
}
