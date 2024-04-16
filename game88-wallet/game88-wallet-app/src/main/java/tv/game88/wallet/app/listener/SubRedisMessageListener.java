package tv.game88.wallet.app.listener;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.StringUtils;
import tv.game88.wallet.api.constants.ConstantsWallet;
import tv.game88.wallet.api.type.StreamMessageType;
import tv.game88.wallet.app.utils.SseSendMessageUtils;

/**
 * @author meng.jun
 */
@Log4j2
@Component
public class SubRedisMessageListener implements MessageListener {
    private final SseSendMessageUtils sseSendMessageUtils;

    @Autowired
    public SubRedisMessageListener( SseSendMessageUtils sseSendMessageUtils ) {
        this.sseSendMessageUtils = sseSendMessageUtils;
    }

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
                    .keySet()
                    .parallelStream()
                    .forEach( ( key ) -> sseSendMessageUtils.sendMessage( key, messageBody, StreamMessageType.NOTIFICATION ) );
        }
        if ( messageChannel.startsWith( ConstantsWallet.SSE_MEMBER_CHANNEL ) ) {
            String memberId = messageChannel.replaceFirst( ConstantsWallet.SSE_MEMBER_CHANNEL, "" );
            if ( StringUtils.isBlank( memberId ) ) {
                return;
            }
            sseSendMessageUtils.sendMessage( memberId, messageBody, StreamMessageType.MEMBER );
        }
    }
}
