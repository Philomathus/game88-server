package tv.game88.wallet.app.listener;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tv.game88.common.utils.StringUtils;
import tv.game88.wallet.api.constants.ConstantsWallet;
import tv.game88.wallet.api.type.StreamMessageType;
import tv.game88.wallet.app.utils.SseSendMessageUtils;

import java.time.Duration;

/**
 * @author meng.jun
 */
@Log4j2
public class SubRedisMessageListener implements MessageListener {

    @Override
    public void onMessage( Message message, byte[] pattern ) {
        final String messageBody = new String( message.getBody() );
        if ( StringUtils.isBlank( messageBody ) ) {
            return;
        }

        if ( RandomUtils.nextInt( 1, 1000 ) == 99 ) {
            log.warn( "收到消息:{}", messageBody );
        }

        String messageChannel = new String( message.getChannel() );
        if ( messageChannel.startsWith( ConstantsWallet.SSE_NOTIFICATION_CHANNEL ) ) {
            ConstantsWallet.MEMBER_SSEEMITTER_MAP
                    .entrySet()
                    .parallelStream()
                    .forEach( ( entry ) -> SseSendMessageUtils.me.sendMessage( entry.getValue(), entry.getKey(), messageBody,
                            StreamMessageType.NOTIFICATION ) );
        }
        if ( messageChannel.startsWith( ConstantsWallet.SSE_MEMBER_CHANNEL ) ) {
            String memberId = messageChannel.replaceFirst( ConstantsWallet.SSE_MEMBER_CHANNEL, "" );
            if ( StringUtils.isBlank( memberId ) ) {
                return;
            }
            SseEmitter sseEmitter = ConstantsWallet.MEMBER_SSEEMITTER_MAP.get( memberId );
            if ( sseEmitter == null ) {
                try {
                    Thread.sleep( Duration.ofSeconds( 3 ) );
                } catch ( InterruptedException e ) {
                    log.error( e.getMessage(), e );
                }
                sseEmitter = ConstantsWallet.MEMBER_SSEEMITTER_MAP.get( memberId );
                if ( sseEmitter == null ) {
                    log.warn( "会员{}不在线,无法发送个人消息", memberId );
                    return;
                }
            }
            SseSendMessageUtils.me.sendMessage( sseEmitter, memberId, messageBody, StreamMessageType.MEMBER );
        }
    }
}
