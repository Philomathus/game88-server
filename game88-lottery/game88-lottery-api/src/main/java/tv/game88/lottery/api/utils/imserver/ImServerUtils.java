package tv.game88.lottery.api.utils.imserver;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.vo.RspBase;

import javax.annotation.Resource;
import java.util.Map;

@Log4j2
@Component
public class ImServerUtils {
    @Resource
    private RestTemplate restTemplate;

    @Value( "${im.sendGroupMsg:null}" )
    private String imSendGroupMsgUrl;
    @Value( "${im.sendMsg:null}" )
    private String imSendMsgUrl;

    @Value( "${spring.profiles.active}" )
    private String profile;

    /**
     * 发送在线群消息
     *
     * @param ext 消息map
     */
    @Async
    public void sendOnlineGroupMessage( Map<String, Object> ext ) {
        ext.put( "groupId", profile );
        ext.put( "uuid", IdWorker.get32UUID() );
        RspBase<?> rspBase = this.sendGroupMessage( profile, ext, 3 );
        if ( rspBase != null && rspBase.getCode() == 200 ) {
            // log.info( "新IM - 在线群组im消息发送成功" );
        }
    }

    /**
     * 发送群消息
     *
     * @param roomId 主播ID
     * @param ext    消息map
     */
    @Async
    public void sendGroupMessage( String roomId, Map<String, Object> ext ) {
        String groupId = profile + "@" + roomId.replaceAll( "#", "" ).replaceAll( "@", "" );
        // 设置群组ID
        ext.put( "groupId", groupId );
        ext.put( "uuid", IdWorker.get32UUID() );
        RspBase<?> rspBase = this.sendGroupMessage( groupId, ext, 3 );
        if ( rspBase != null && rspBase.getCode() == 200 ) {
            // log.info( "新IM - 群组{}im消息发送成功", groupId );
        }
    }

    private RspBase<?> sendGroupMessage( String groupId, Map<String, Object> messageMap, int retryNum ) {
        if ( StringUtils.isBlank( this.imSendGroupMsgUrl ) || !this.imSendGroupMsgUrl.startsWith( "http" ) ) {
            //log.error( "新IM - 未初始化参数, IM消息无法发送" );
            return null;
        }
        if ( retryNum <= 0 ) {
            log.error( "新IM - IM访问失败,message:{}", JsonUtil.object2Json( messageMap ) );
            return null;
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>( messageMap, httpHeaders );

        try {
            return restTemplate.postForObject( this.imSendGroupMsgUrl + groupId, httpEntity, RspBase.class );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
        try {
            Thread.sleep( 999L );
        } catch ( InterruptedException ex ) {
            ex.printStackTrace();
        }
        retryNum--;
        return this.sendGroupMessage( groupId, messageMap, retryNum );
    }

    /**
     * 发送单会员消息
     *
     * @param memberId   会员ID
     * @param messageMap 消息map
     */
    public void sendMessage( String memberId, Map<String, Object> messageMap ) {
        if ( StringUtils.isBlank( this.imSendMsgUrl ) || !this.imSendMsgUrl.startsWith( "http" ) ) {
            //log.error( "新IM - 未初始化参数, IM消息无法发送" );
            return;
        }

        messageMap.put( "groupId", profile );
        messageMap.put( "uuid", IdWorker.get32UUID() );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>( messageMap, httpHeaders );

        try {
            RspBase<?> rspBase = restTemplate.postForObject( this.imSendMsgUrl + memberId, httpEntity, RspBase.class );
            if ( rspBase != null && rspBase.getCode() == 200 ) {
                // log.info( "新IM - 单会员{}im消息发送成功", memberId );
                return;
            }
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
        log.error( "新IM - 单会员{}im消息发送失败", memberId );
    }
}
