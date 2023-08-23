package tv.game88.core.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.admin.service.UploadService;

import javax.annotation.Resource;
import java.io.IOException;

@RestController
@RequestMapping( "/upload" )
public class UploadController {
    @Resource
    private UploadService uploadService;

    @PostMapping( "{path}" )
    @Log( title = "oss上传", businessType = BusinessType.UPLOAD )
    public RspBase<?> upload( @RequestParam( "file" ) MultipartFile file, @PathVariable String path ) throws IOException {
        return uploadService.upload( file, path );
    }

    @PreAuthorize( "@ss.hasPermi('config:oss:ossTest')" )
    @PostMapping( "/test/{id}" )
    @Log( title = "oss上传测试", businessType = BusinessType.UPLOAD )
    public RspBase<?> uploadTest( @RequestParam( "file" ) MultipartFile file,
                                  @PathVariable long id ) throws IOException {
        return uploadService.uploadTest( file, id );
    }
}
