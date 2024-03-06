package tv.game88.wallet.app.utils;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tv.game88.common.utils.JsonUtil;
import tv.game88.wallet.api.type.StreamMessageType;

import java.util.concurrent.Executors;

@Component
@Log4j2
public class SseSendMessageUtils {
    public static SseSendMessageUtils me;

    @PostConstruct
    void init() {
        me = this;
    }

    public void sendMessage( SseEmitter sseEmitter, String memberId, String messageBody, StreamMessageType messageType ) {
        if ( sseEmitter == null ) {
            return;
        }
        Executors.newVirtualThreadPerTaskExecutor().execute( () -> {
            try {
                sseEmitter.send( SseEmitter
                        .event()
                        .name( messageType.name() )
                        .id( memberId )
                        .data( messageBody, MediaType.APPLICATION_JSON )
                        .reconnectTime( 1000 ) );
            } catch ( Exception e ) {
                log.error( JsonUtil.object2Json( sseEmitter ) + " ::: " + e.getMessage(), e );
                sseEmitter.completeWithError( e );
            }
        } );
    }
}
