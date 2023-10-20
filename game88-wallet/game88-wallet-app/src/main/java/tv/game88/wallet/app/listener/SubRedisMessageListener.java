package tv.game88.wallet.app.listener;

import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tv.game88.common.utils.StringUtils;
import tv.game88.wallet.api.constants.ConstantsWallet;
import tv.game88.wallet.api.sse.SseStreamService;
import tv.game88.wallet.api.sse.model.SimpleProtocolMessage;
import tv.game88.wallet.api.vo.TransDetailStreamMessage;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * @author meng.jun
 */
@Log4j2
@Component
public class SubRedisMessageListener implements MessageListener {

    @Resource
    private SseStreamService sseStreamService;

    @Override
    public void onMessage( Message message, byte[] pattern ) {
        SimpleProtocolMessage<TransDetailStreamMessage> simpleProtocolMessage = null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream( message.getBody() );
            ObjectInputStream    ois  = new ObjectInputStream( bais );

            simpleProtocolMessage = ( SimpleProtocolMessage<TransDetailStreamMessage> ) ois.readObject();
        } catch ( IOException | ClassNotFoundException e ) {
            throw new RuntimeException( e );
        }
        if ( simpleProtocolMessage == null ) {
            return;
        }

        String messageChannel = new String( message.getChannel() );
        if ( messageChannel.startsWith( ConstantsWallet.MESSAGE_CHANNEL ) ) {
            String userId = messageChannel.replaceFirst( ConstantsWallet.MESSAGE_CHANNEL, "" );
            if ( StringUtils.isBlank( userId ) ) {
                return;
            }
            SseEmitter sseEmitter = ConstantsWallet.MEMBER_SSEEMITTER_MAP.get( userId );
            SseEmitter.SseEventBuilder event = SseEmitter
                    .event()
                    .name( simpleProtocolMessage.getMessageType().toString() )
                    .id( userId )
                    .data( simpleProtocolMessage.getData(), MediaType.APPLICATION_JSON )
                    .reconnectTime( 1000 );
            sseStreamService.sendMessage( sseEmitter, event );
        }
    }
}
