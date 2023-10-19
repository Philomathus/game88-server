package tv.game88.wallet.app.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tv.game88.core.sse.model.SimpleProtocolMessage;
import tv.game88.core.sse.service.ServerStreamMessageService;
import tv.game88.wallet.app.utils.MemberSecurityUtils;

import java.util.function.Function;

import static tv.game88.core.sse.constant.SseConstants.USER_ID_EMITTERS;
import static tv.game88.core.sse.model.StreamMessageType.CONNECTION;


@Log4j2
@RestController
@RequestMapping( "/api/stream" )
@RequiredArgsConstructor
public class ServerStreamController {

    private final ServerStreamMessageService serverStreamMessageService;

    @GetMapping( value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE )
    public SseEmitter streamSubscribe() {
        String memberId = MemberSecurityUtils.getUserId();
        SseEmitter emitter = new SseEmitter( - 1L );
        Function<String, Runnable> removeEmitter = id -> () -> USER_ID_EMITTERS.remove( id );

        emitter.onCompletion( removeEmitter.apply( memberId ) );
        emitter.onTimeout( removeEmitter.apply( memberId ) );

        USER_ID_EMITTERS.put( memberId, emitter );
        serverStreamMessageService.sendMessage( emitter, memberId, SimpleProtocolMessage.<String>builder()
                        .messageType(CONNECTION)
                        .data("Connection successful").build());
        return emitter;
    }
}
