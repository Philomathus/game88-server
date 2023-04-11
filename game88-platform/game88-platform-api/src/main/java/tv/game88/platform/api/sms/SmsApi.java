package tv.game88.platform.api.sms;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.services.sms.SmsClient;
import com.baidubce.services.sms.SmsClientConfiguration;
import com.baidubce.services.sms.model.SendMessageV3Request;
import com.baidubce.services.sms.model.SendMessageV3Response;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.RandomUtils;
import tv.game88.core.config.cache.ConfigSmsCacheUtil;
import tv.game88.core.config.entity.ConfigSms;
import tv.game88.core.config.mapper.ConfigSmsMapper;
import tv.game88.platform.api.entity.ConfigSmsFaillog;
import tv.game88.platform.api.mapper.ConfigSmsFaillogMapper;
import tv.game88.platform.api.mapper.ServerSmsMapper;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Component
public class SmsApi {
    @Resource
    private ConfigSmsCacheUtil     configSmsCacheUtil;
    @Resource
    private ConfigSmsFaillogMapper configSmsFaillogMapper;
    @Resource
    private RestTemplate           restTemplate;
    @Autowired
    private ConfigSmsMapper configSmsMapper;

    //无需修改,用于格式化鉴权头域,给"X-WSSE"参数赋值
    private static final String WSSE_HEADER_FORMAT =
            "UsernameToken Username=\"%s\",PasswordDigest=\"%s\",Nonce=\"%s\"," + "Created=\"%s\"";
    //无需修改,用于格式化鉴权头域,给"Authorization"参数赋值
    private static final String AUTH_HEADER_VALUE  = "WSSE realm=\"SDP\",profile=\"UsernameToken\",type=\"Appkey\"";

    private static String createPhoneCode() {
        StringBuilder code = new StringBuilder();
        for ( int i = 1; i <= 6; i++ ) {
            code.append( RandomUtils.randomIntWithMax( 1, 9 ) );
        }
        return code.toString();
    }

    public String sendSms( String phone, ConfigSms configSms ) {
        String code = createPhoneCode();
        return this.sendSms( phone, code, configSms );
    }

    public String sendSms( String phone, int index, String code ) {
        long countCache = configSmsCacheUtil.countCache();
        if ( index > ( countCache - 1 ) ) {
            index = 0;
        }
        ConfigSms configSms = configSmsCacheUtil.getConfigSmsCache( index );
        if ( StringUtils.isBlank( code ) ) {
            code = createPhoneCode();
        }
        return this.sendSms( phone, code, configSms );
    }

    private String sendSms( String phone, String code, ConfigSms configSms ) {
        return switch ( configSms.getProvider() ) {
            case 0 -> this.sendSmsTencent( configSms, phone, code );
            case 1 -> this.sendSmsAliyun( configSms, phone, code );
            case 2 -> this.sendSmsBaidu( configSms, phone, code );
            case 3 -> this.sendSmsHuawei( configSms, phone, code );
            default -> throw new BusinessException( "not found provider" );
        };
    }

    private String sendSmsHuawei( ConfigSms configSms, String phone, String code ) {
        String receiver      = "+86" + phone;
        String templateParas = "[\"" + code + "\"]";

        Map<String, String> params = new HashMap<>();
        params.put( "from", configSms.getSignature() );
        params.put( "to", receiver );
        params.put( "templateId", configSms.getTemplate() );
        params.put( "templateParas", templateParas );
        params.put( "signature", configSms.getName() );

        StringBuilder sb = new StringBuilder();
        params.forEach( ( k, v ) -> {
            sb.append( k ).append( "=" ).append( URLEncoder.encode( v, StandardCharsets.UTF_8 ) ).append( "&" );
        } );
        String body = sb.substring( 0, sb.length() - 1 );

        //请求Headers中的X-WSSE参数值
        String wsseHeader = buildWsseHeader( configSms.getAppKey(), configSms.getAppAccess() );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        httpHeaders.add( "Authorization", AUTH_HEADER_VALUE );
        httpHeaders.add( "X-WSSE", wsseHeader );
        HttpEntity<String> httpEntity = new HttpEntity<>( body, httpHeaders );

        try {
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(
                    configSms.getEndpoint() + "/sms/batchSendSms/v1", httpEntity, Map.class );
            Map<String, Object> entityBody = responseEntity.getBody();
            if ( responseEntity.getStatusCode().is2xxSuccessful() ) {
                if ( !CollectionUtils.isEmpty( entityBody ) ) {
                    String rspCode = entityBody.getOrDefault( "code", "" ).toString();
                    if ( "000000".equals( rspCode ) ) {
                        return code;
                    }
                }
            }
            log.error( JsonUtil.object2Json( entityBody ) );
        } catch ( Exception e ) {
            log.error( "短信发送失败" + e.getMessage(), e );

        }
        throw new BusinessException( "短信发送失败,请联系客服" );
    }

    /**
     * 构造X-WSSE参数值 Construct X-WSSE parameter value
     *
     * @param appKey
     * @param appSecret
     */
    private static String buildWsseHeader( String appKey, String appSecret ) {
        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern( "yyyy-MM-dd'T'HH:mm:ss'Z'" );
            String            time              = LocalDateTimeUtils.format( LocalDateTime.now(), dateTimeFormatter );
            String            nonce             = IdWorker.get32UUID();
            MessageDigest     md                = MessageDigest.getInstance( "SHA-256" );
            md.update( ( nonce + time + appSecret ).getBytes() );
            String passwordDigestBase64Str = Base64Utils.encodeToString( md.digest() );
            return String.format( WSSE_HEADER_FORMAT, appKey, passwordDigestBase64Str, nonce, time );
        } catch ( NoSuchAlgorithmException e ) {
            e.printStackTrace();
        }
        return null;
    }

    private String sendSmsTencent( ConfigSms configSms, String phone, String msg ) {

        Credential  cred        = new Credential( configSms.getAppKey(), configSms.getAppAccess() );
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setReqMethod( "POST" );
        httpProfile.setEndpoint( "sms.tencentcloudapi.com" );
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile( httpProfile );
        com.tencentcloudapi.sms.v20190711.SmsClient client = new com.tencentcloudapi.sms.v20190711.SmsClient( cred,
                configSms.getRegion(), clientProfile );
        com.tencentcloudapi.sms.v20190711.models.SendSmsRequest req =
                new com.tencentcloudapi.sms.v20190711.models.SendSmsRequest();
        req.setSmsSdkAppid( configSms.getSmsSdkAppid() );
        req.setSign( configSms.getSignature() );
        req.setTemplateID( configSms.getTemplate() );
        /* 下发手机号码，采用 e.164 标准，+[国家或地区码][手机号]
         * 例如+8613711112222， 其中前面有一个+号 ，86为国家码，13711112222为手机号，最多不要超过200个手机号*/
        String[] phoneNumbers = { "+86" + phone };
        req.setPhoneNumberSet( phoneNumbers );
        String[] templateParams = { msg };
        req.setTemplateParamSet( templateParams );
        com.tencentcloudapi.sms.v20190711.models.SendSmsResponse res = null;
        try {
            res = client.SendSms( req );
        } catch ( TencentCloudSDKException e ) {
            throw new RuntimeException( e.getMessage() );
        }
        if ( res.getSendStatusSet() != null && "Ok".equalsIgnoreCase( res.getSendStatusSet()[ 0 ].getCode() ) ) {
            return msg;
        } else {
            String rspCode    = res.getSendStatusSet()[ 0 ].getCode();
            String rspMessage = res.getSendStatusSet()[ 0 ].getMessage();
            String smsName    = "腾讯云";
            String subname    = configSms.getName();
            errorLog( rspCode, rspMessage, phone, smsName, subname );
            log.warn( "短信发送失败:{}", JsonUtil.object2Json( res ) );

            if ( "LimitExceeded.PhoneNumberDailyLimit".equals( rspCode ) ) {
                throw new BusinessException( "今日发送短信过多，请明日重试" );
            } else if ( "FailedOperation.PhoneNumberOnBlacklist".equals( rspCode )
                    || "FailedOperation.PhoneNumberInBlacklist".equals( rspCode ) ) {
                throw new BusinessException("您的号码在黑名单库中，请联系客服");
            } else if ("insufficient balance in SMS package".equals( rspCode )
                    || "FailedOperation.InsufficientBalanceInSmsPackage".equals( rspCode ) ) {
                throw new BusinessException("短信包中的余额不足");
            } else {
                throw new BusinessException( "发送短信失败，请联系客服" );
            }
        }

    }

    private String sendSmsAliyun( ConfigSms configSms, String phone, String msg ) {
        System.setProperty( "sun.net.client.defaultConnectTimeout", "10000" );
        System.setProperty( "sun.net.client.defaultReadTimeout", "10000" );
        final String   regionId = configSms.getRegion();
        IClientProfile profile  = DefaultProfile.getProfile( regionId, configSms.getAppKey(), configSms.getAppAccess() );
        DefaultProfile.addEndpoint( regionId, "Dysmsapi", "dysmsapi.aliyuncs.com" );
        IAcsClient acsClient = new DefaultAcsClient( profile );

        //组装请求对象
        SendSmsRequest smsRequest = new SendSmsRequest();
        smsRequest.setSysMethod( MethodType.POST );
        smsRequest.setPhoneNumbers( phone );
        smsRequest.setSignName( configSms.getSignature() );
        smsRequest.setTemplateCode( configSms.getTemplate() );
        smsRequest.setTemplateParam( "{\"code\":" + msg + "}" );

        SendSmsResponse sendSmsResponse = null;
        try {
            sendSmsResponse = acsClient.getAcsResponse( smsRequest );
        } catch ( ClientException e ) {
            throw new RuntimeException( e.getErrMsg() );
        }
        if ( sendSmsResponse.getCode() != null && "OK".equals( sendSmsResponse.getCode() ) ) {
            return msg;
        } else {
            String rspCode    = sendSmsResponse.getCode();
            String rspMessage = sendSmsResponse.getMessage();
            String smsName    = "阿里云";
            String subname    = configSms.getName();
            errorLog( rspCode, rspMessage, phone, smsName, subname );

            log.warn( "阿里云短信发送失败:{}", JsonUtil.object2Json( sendSmsResponse ) );

            if ( "isv.BUSINESS_LIMIT_CONTROL".equals( rspCode ) ) {
                throw new BusinessException( "今日发送短信过多，请明日重试" );
            } else {
                throw new BusinessException( "发送短信失败，请联系客服" );
            }
        }

    }

    private String sendSmsBaidu( ConfigSms configSms, String phone, String msg ) {
        SmsClientConfiguration config = new SmsClientConfiguration();
        config.setCredentials( new DefaultBceCredentials( configSms.getAppKey(), configSms.getAppAccess() ) );
        config.setEndpoint( configSms.getRegion() );
        SmsClient client = new SmsClient( config );

        SendMessageV3Request request = new SendMessageV3Request();
        request.setMobile( phone );
        request.setSignatureId( configSms.getSignature() );
        request.setTemplate( configSms.getTemplate() );
        Map<String, String> contentVar = new HashMap<>();
        contentVar.put( "code", msg );
        contentVar.put( "minute", "1" );
        request.setContentVar( contentVar );
        try {
            SendMessageV3Response sendSmsResponse = client.sendMessage( request );
            // 解析请求响应 response.isSuccess()为true 表示成功
            if ( sendSmsResponse != null && sendSmsResponse.isSuccess() ) {
                return msg;
            } else {
                String rspCode    = sendSmsResponse.getCode();
                String rspMessage = sendSmsResponse.getMessage();
                String smsName    = "百度云";
                String subname    = configSms.getName();
                errorLog( rspCode, rspMessage, phone, smsName, subname );
                log.warn( "百度云短信发送失败:{}", JsonUtil.object2Json( sendSmsResponse ) );
                throw new BusinessException( JsonUtil.object2Json( sendSmsResponse ) );
            }
        } catch ( BusinessException e ) {
            throw new BusinessException( e.getMessage() );
        }
    }

    //记录短信登录异常日志
    private void errorLog( String rspCode, String rspMessage, String phone, String smsName, String subname ) {
        ConfigSmsFaillog smsFainLog = new ConfigSmsFaillog();
        smsFainLog.setErrorCode( rspCode );
        smsFainLog.setErrorMessage( rspMessage );
        smsFainLog.setPhone( phone );
        smsFainLog.setSmsName( smsName );
        smsFainLog.setSmsSubname( subname );
        smsFainLog.setCreateTime( LocalDateTime.now() );
        configSmsFaillogMapper.insert( smsFainLog );
    }

    public String sendMemSms( String phone, String msg ) {
        System.out.println("INSIDE sendMemSms");
        if (StringUtils.isEmpty(phone)) {
            throw new BusinessException( "手机号不能为空" );
        }
        if (StringUtils.isEmpty(msg)) {
            throw new BusinessException( "发送信息不能为空" );
        }
        System.out.println("Breakpoint");
        ConfigSms configSms1 = new ConfigSms();
        configSms1.setName("会员通知");
        List<ConfigSms> serverSmsList = configSmsMapper.selectConfigSmsList( configSms1 );
        if (serverSmsList.isEmpty()) {
            throw new BusinessException( "会员sms通道不存在,无法发送" );
        }else {
            ConfigSms configSms = serverSmsList.get(0);
            switch ( configSms.getProvider() ) {
                case 0:
                    msg = this.sendSmsTencent( configSms, phone, msg );
                    break;
                case 1:
                    msg = this.sendSmsAliyun( configSms, phone, msg );
                    break;
                case 2:
                    msg = this.sendSmsBaidu( configSms, phone,msg );
                    break;
                default:
                    throw new BusinessException( "不支持的短信运营商类型" );
            }
        }
        return msg;
    }
}
