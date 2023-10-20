package tv.game88.wallet.api.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tv.game88.wallet.api.constants.ConstantsWallet;
import tv.game88.wallet.api.sse.model.SimpleProtocolMessage;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.function.Function;

import static tv.game88.wallet.api.sse.model.StreamMessageType.CONNECTION;

@Service
@RequiredArgsConstructor
public class SseStreamService {
    @Resource
    public  ThreadPoolTaskExecutor        threadPoolTaskExecutor;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public SseEmitter createEmitter( String memberId ) {
        if ( memberId == null ) {
            throw new RuntimeException( "No member id received" );
        }
        SseEmitter                 emitter       = new SseEmitter( -1L );
        Function<String, Runnable> removeEmitter = id -> () -> ConstantsWallet.MEMBER_SSEEMITTER_MAP.remove( id );

        emitter.onCompletion( removeEmitter.apply( memberId ) );
        emitter.onTimeout( removeEmitter.apply( memberId ) );

        ConstantsWallet.MEMBER_SSEEMITTER_MAP.put( memberId, emitter );

        SimpleProtocolMessage<String> message = SimpleProtocolMessage
                .<String>builder()
                .messageType( CONNECTION )
                .data( "Connection successful" )
                .build();
        SseEmitter.SseEventBuilder event = SseEmitter
                .event()
                .name( message.getMessageType().toString() )
                .id( memberId )
                .data( message.getData(), MediaType.APPLICATION_JSON )
                .reconnectTime( 1000 );
        sendMessage( emitter, event );
        return emitter;
    }

    public void sendMessage( SseEmitter emitter, SseEmitter.SseEventBuilder event ) {
        if ( emitter == null ) {
            return;
        }
        threadPoolTaskExecutor.execute( () -> {
            try {
                emitter.send( event );
            } catch ( IOException ex ) {
                emitter.completeWithError( ex );
            }
        } );
    }
}
