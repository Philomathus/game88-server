package tv.game88.core.sse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tv.game88.core.sse.model.SimpleProtocolMessage;

import java.io.IOException;
import java.util.function.Function;

import static tv.game88.core.sse.constant.SseConstants.USER_ID_EMITTERS;
import static tv.game88.core.sse.model.StreamMessageType.CONNECTION;

@Service
@RequiredArgsConstructor
public class SseStreamService {

    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;

    public SseEmitter createEmitter(String memberId) {
        if (memberId == null) {
            throw new RuntimeException("No member id received");
        }
        SseEmitter emitter = new SseEmitter(-1L);
        Function<String, Runnable> removeEmitter = id -> () -> USER_ID_EMITTERS.remove(id);

        emitter.onCompletion(removeEmitter.apply(memberId));
        emitter.onTimeout(removeEmitter.apply(memberId));

        USER_ID_EMITTERS.put(memberId, emitter);
        sendMessage(emitter, memberId, SimpleProtocolMessage.<String>builder()
                .messageType(CONNECTION)
                .data("Connection successful").build());
        return emitter;
    }

    public void sendMessage(SseEmitter emitter, String receiverId, SimpleProtocolMessage<?> message) {
        if (emitter != null && receiverId != null) {
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .name(message.getMessageType().toString())
                    .id(receiverId)
                    .data(message.getData(), MediaType.APPLICATION_JSON)
                    .reconnectTime(1000);
            sendMessage(emitter, event);
        }
    }

    private void sendMessage(SseEmitter emitter, SseEmitter.SseEventBuilder event) {
        threadPoolTaskExecutor.execute(() -> {
            try {
                emitter.send(event);
            } catch (IOException ex) {
                emitter.completeWithError(ex);
            }
        });
    }
}
