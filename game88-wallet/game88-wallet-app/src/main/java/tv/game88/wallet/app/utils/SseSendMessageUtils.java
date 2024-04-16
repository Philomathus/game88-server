package tv.game88.wallet.app.utils;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tv.game88.common.exception.BusinessException;
import tv.game88.wallet.api.constants.ConstantsWallet;
import tv.game88.wallet.api.type.StreamMessageType;

@Component
@Log4j2
public class SseSendMessageUtils {
    @Retryable( retryFor = Exception.class, backoff = @Backoff( delay = 2000 ), maxAttempts = 3 )
    public void sendMessage( String memberId, String messageBody, StreamMessageType messageType ) {
        SseEmitter sseEmitter = ConstantsWallet.MEMBER_SSEEMITTER_MAP.get( memberId );
        if ( sseEmitter == null ) {
            throw new BusinessException( "SseEmitter is null" );
        }
        Thread.ofVirtual().start( () -> {
            try {
                sseEmitter.send( SseEmitter
                        .event()
                        .name( messageType.name() )
                        .id( memberId )
                        .data( messageBody, MediaType.APPLICATION_JSON )
                        .reconnectTime( 2000L ) );
            } catch ( Exception e ) {
                ConstantsWallet.MEMBER_SSEEMITTER_MAP.remove( memberId );
                sseEmitter.completeWithError( e );
            }
        } );
    }
}
