package tv.game88.core.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigOssCacheUtil;
import tv.game88.core.config.entity.ConfigOss;
import tv.game88.core.config.mapper.ConfigOssMapper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@Log4j2
@Component
public class OssApi {
    @Resource
    private ConfigOssMapper    configOssMapper;
    @Resource
    private ConfigOssCacheUtil configOssCacheUtil;

    public RspBase<String> upload( MultipartFile file, String path ) throws IOException {
        ConfigOss configOss = configOssCacheUtil.getEffect();
        return this.updateByProvider( file, path, configOss );
    }

    public RspBase<String> uploadTest( MultipartFile file, long id ) throws IOException {
        ConfigOss configOss = configOssMapper.selectById( id );
        return this.updateByProvider( file, "test", configOss );
    }

    private RspBase<String> updateByProvider( MultipartFile file, String path, ConfigOss configOss ) throws IOException {
        String       fileName        = file.getOriginalFilename();
        String       extension       = FilenameUtils.getExtension( fileName );
        InputStream  inputStream     = file.getInputStream();
        File         newFile         = new File( System.getProperty( "java.io.tmpdir" ) + fileName );
        Path         newFileToPath   = newFile.toPath();
        OutputStream newOutputStream = Files.newOutputStream( newFileToPath );
        IOUtils.copy( inputStream, newOutputStream );
        InputStream newInputStream = Files.newInputStream( newFileToPath );
        String      rFileName      = DigestUtils.md5Hex( newInputStream );
        String      fileKey        = "88lm/" + path + "/" + rFileName + "." + extension;
        String url = switch ( configOss.getProvider() ) {
            case 0 -> this.uploadAliyun( configOss, fileKey, newFile );
            case 1 -> this.uploadAmazon( configOss, fileKey, newFile );
            case 2 -> this.uploadKuaiKuai( configOss, fileKey, newFile );
            default -> {
                log.error( "未找到相应的服务商" );
                yield "";
            }
        };
        newFile.delete();
        IOUtils.closeQuietly( inputStream, newInputStream, newOutputStream );
        if ( StringUtils.isBlank( url ) ) {
            return RspBase.businessError( "上传失败,请联系技术人员" );
        }
        RspBase<String> rspBase = RspBase.ok( "上传成功", url );
        rspBase.setOtherData( rFileName + "." + extension );
        return rspBase;
    }

    private String uploadKuaiKuai( ConfigOss configOss, String fileKey, File newFile ) {
        try {
            byte[] bytes = new byte[ 0 ];
            try {
                bytes = FileUtils.readFileToByteArray( newFile );
            } catch ( IOException e ) {
                log.error( e.getMessage(), e );
            }
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength( bytes.length );
            //设置加密  加密算法为  AES256
            objectMetadata.setSSEAlgorithm( ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION );
            PutObjectRequest putObjectRequest = new PutObjectRequest( configOss.getBucket(), fileKey,
                    new ByteArrayInputStream( bytes ), objectMetadata ).withCannedAcl( CannedAccessControlList.PublicRead );
            //设置文件图片上传读写权限。访问继承桶的权限，不设置则单个图片无法显示。权限默认私有。
            BasicAWSCredentials creds = new BasicAWSCredentials( configOss.getAccessKey(), configOss.getAccessSecret() );
            //创建安全证书注册
            AmazonS3 s3Client = AmazonS3ClientBuilder
                    .standard()
                    .withCredentials( new AWSStaticCredentialsProvider( creds ) )
                    .withEndpointConfiguration( new AwsClientBuilder.EndpointConfiguration( configOss.getEndpoint(),
                            "oss-cn" + "-quanzhou.kz.cc" ) )//上传地址和区域
                    .build();
            //通过访问第三方，将文件上传到亚马逊
            s3Client.putObject( putObjectRequest );
            s3Client.shutdown();
            return "/" + fileKey;
        } catch ( AmazonServiceException e ) {
            log.error( e.getMessage(), e );
        }
        return null;
    }

    private String uploadAmazon( ConfigOss configOss, String fileKey, File newFile ) {
        Regions clientRegion = null;//地区
        if ( StringUtils.isNotBlank( configOss.getRegion() ) ) {
            try {
                clientRegion = Regions.fromName( configOss.getRegion() );
            } catch ( Exception e ) {
                log.error( e.getMessage(), e );
            }
        }
        if ( clientRegion == null ) {
            log.error( "亚马逊上传,地区未配置或配置错误,默认配置香港地区 - ossId:{};Region:{}", configOss.getId(), configOss.getRegion() );
            clientRegion = Regions.AP_EAST_1;
        }
        String bucketName = configOss.getBucket();//桶的名称
        try {
            BasicAWSCredentials creds = new BasicAWSCredentials( configOss.getAccessKey(), configOss.getAccessSecret() );
            AmazonS3 s3Client = AmazonS3ClientBuilder
                    .standard()
                    .withRegion( clientRegion )
                    .withCredentials( new AWSStaticCredentialsProvider( creds ) )
                    .build();//创建证书及注册地址
            s3Client.putObject( bucketName, fileKey, newFile );
            s3Client.shutdown();
            return "/" + fileKey;
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
        return null;
    }

    private String uploadAliyun( ConfigOss configOss, String fileKey, File newFile ) {
        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build( configOss.getEndpoint(), configOss.getAccessKey(),
                configOss.getAccessSecret() );
        try {
            // 上传文件流
            ossClient.putObject( configOss.getBucket(), fileKey, newFile );
        } catch ( OSSException oe ) {
            log.error( "Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason." );
            log.error( "Error Message:" + oe.getErrorMessage() );
            log.error( "Error Code:" + oe.getErrorCode() );
            log.error( "Request ID:" + oe.getRequestId() );
            log.error( "Host ID:" + oe.getHostId() );
        } catch ( ClientException ce ) {
            log.error( "Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network." );
            log.error( "Error Message:" + ce.getMessage() );
        } finally {
            if ( ossClient != null ) {
                ossClient.shutdown();
            }
        }
        return "/" + fileKey;
    }
}
