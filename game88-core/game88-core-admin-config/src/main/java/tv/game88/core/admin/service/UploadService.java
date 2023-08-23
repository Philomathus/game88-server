package tv.game88.core.admin.service;

import org.springframework.web.multipart.MultipartFile;
import tv.game88.common.vo.RspBase;

import java.io.IOException;

public interface UploadService {
    RspBase<?> upload( MultipartFile file, String path ) throws IOException;

    RspBase<?> uploadTest( MultipartFile file, long id ) throws IOException;
}
