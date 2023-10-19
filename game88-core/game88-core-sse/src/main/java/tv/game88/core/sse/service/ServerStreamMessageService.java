package tv.game88.core.sse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tv.game88.core.sse.model.SimpleProtocolMessage;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ServerStreamMessageService {

    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;

    public void sendMessage(SseEmitter emitter, String receiverId, SimpleProtocolMessage<?> message ) {
        if(emitter != null && receiverId != null) {
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .name( message.getMessageType().toString() )
                    .id( receiverId )
                    .data( message, MediaType.APPLICATION_JSON )
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
