package tv.game88.wallet.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.core.utils.OssApi;
import tv.game88.wallet.api.util.ZXingUtil;

import java.io.IOException;
import java.io.InputStream;

@RestController
@Tag( name = "图片上传接口" )
@Log4j2
public class UploadPicController {
    @Resource
    private OssApi ossApi;

    @Operation( summary = "微信个人收款码上传" )
    @PostMapping( "/api/uploadWxp" )
    public RspBase<?> uploadWxp( @RequestParam( "file" ) MultipartFile file ) throws IOException {
        InputStream inputStream = file.getInputStream();
        boolean     isWxp       = false;
        try {
            String url = ZXingUtil.decodeImg( inputStream );
            if ( StringUtils.isNotBlank( url ) && url.startsWith( "wxp://" ) ) {
                isWxp = true;
            }
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
        if ( !isWxp ) {
            return RspBase.businessError( "请上传正确的微信个人收款码" );
        }
        RspBase<String> rspBase = ossApi.upload( file, "wallet" );
        if ( rspBase.getData() != null ) {
            rspBase.setData( ConfigDomainCacheUtil.me.getDomainOssValue() + rspBase.getData() );
        }
        return rspBase;
    }

    @Operation( summary = "支付宝个人收款码上传" )
    @PostMapping( "/api/uploadAlipay" )
    public RspBase<?> uploadAlipay( @RequestParam( "file" ) MultipartFile file ) throws IOException {
        InputStream inputStream = file.getInputStream();
        boolean     isWxp       = false;
        try {
            String url = ZXingUtil.decodeImg( inputStream );
            if ( StringUtils.isNotBlank( url ) && url.startsWith( "https://qr.alipay.com/" ) ) {
                isWxp = true;
            }
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
        if ( !isWxp ) {
            return RspBase.businessError( "请上传正确的支付宝个人收款码" );
        }
        RspBase<String> rspBase = ossApi.upload( file, "wallet" );
        if ( rspBase.getData() != null ) {
            rspBase.setData( ConfigDomainCacheUtil.me.getDomainOssValue() + rspBase.getData() );
        }
        return rspBase;
    }

    @Operation( summary = "其它类型图片上传" )
    @PostMapping( "/api/uploadOther" )
    public RspBase<?> uploadOther( @RequestParam( "file" ) MultipartFile file ) throws IOException {
        if (  file.getSize() > 5 * 1024 * 1024 ) {
            return RspBase.businessError( "错误:文件大小超过5mb" );
        }
        RspBase<String> rspBase = ossApi.upload( file, "wallet" );
        if ( rspBase.getData() != null ) {
            rspBase.setData( ConfigDomainCacheUtil.me.getDomainOssValue() + rspBase.getData() );
        }
        return rspBase;
    }
}
