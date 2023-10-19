package tv.game88.wallet.api.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tv.game88.wallet.api.sse.model.SimpleProtocolMessage;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.function.Function;

import static tv.game88.wallet.api.constants.ConstantsWallet.USER_ID_EMITTERS_KEY;
import static tv.game88.wallet.api.sse.model.StreamMessageType.CONNECTION;

@Service
@RequiredArgsConstructor
public class SseStreamService {

    @Resource
    public ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public SseEmitter createEmitter( String memberId ) {
        if ( memberId == null ) {
            throw new RuntimeException( "No member id received" );
        }
        SseEmitter                 emitter       = new SseEmitter( -1L );
        Function<String, Runnable> removeEmitter = id -> () -> redisTemplate.opsForHash().delete( USER_ID_EMITTERS_KEY, id );

        emitter.onCompletion( removeEmitter.apply( memberId ) );
        emitter.onTimeout( removeEmitter.apply( memberId ) );

        redisTemplate.opsForHash().put( USER_ID_EMITTERS_KEY, memberId, emitter );
        sendMessage( memberId, SimpleProtocolMessage
                .<String>builder()
                .messageType( CONNECTION )
                .data( "Connection successful" )
                .build() );
        return emitter;
    }

    public void sendMessage( String receiverId, SimpleProtocolMessage<?> message ) {
        SseEmitter emitter = ( SseEmitter ) redisTemplate.opsForHash().get( USER_ID_EMITTERS_KEY, receiverId );
        if ( emitter != null ) {
            SseEmitter.SseEventBuilder event = SseEmitter
                    .event()
                    .name( message.getMessageType().toString() )
                    .id( receiverId )
                    .data( message.getData(), MediaType.APPLICATION_JSON )
                    .reconnectTime( 1000 );
            sendMessage( emitter, event );
        }
    }

    private void sendMessage( SseEmitter emitter, SseEmitter.SseEventBuilder event ) {
        threadPoolTaskExecutor.execute( () -> {
            try {
                emitter.send( event );
            } catch ( IOException ex ) {
                emitter.completeWithError( ex );
            }
        } );
    }
}
