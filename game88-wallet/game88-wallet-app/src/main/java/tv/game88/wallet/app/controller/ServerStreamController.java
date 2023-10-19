package tv.game88.wallet.app.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tv.game88.core.sse.service.SseStreamService;
import tv.game88.wallet.app.utils.MemberSecurityUtils;


@Log4j2
@RestController
@RequestMapping( "/api/stream" )
@RequiredArgsConstructor
public class ServerStreamController {

    private final SseStreamService sseStreamService;

    @GetMapping( value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE )
    public SseEmitter streamSubscribe() {
        return sseStreamService.createEmitter(MemberSecurityUtils.getUserId());
    }
}
