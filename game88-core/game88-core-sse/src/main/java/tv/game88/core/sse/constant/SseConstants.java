package tv.game88.core.sse.constant;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SseConstants {
    public static final Map<String, SseEmitter> USER_ID_EMITTERS = new ConcurrentHashMap<>();
}
