package tv.game88.wallet.app.see;

import com.google.common.collect.ImmutableMap;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tv.game88.common.utils.JsonUtil;
import tv.game88.wallet.api.constants.ConstantsWallet;
import tv.game88.wallet.api.type.StreamMessageType;
import tv.game88.wallet.app.utils.SseSendMessageUtils;

import java.util.function.Consumer;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class SseStreamService {
    @Resource
    private SseSendMessageUtils sseSendMessageUtils;

    public SseEmitter createEmitter( String memberId ) {
        if ( ConstantsWallet.MEMBER_SSEEMITTER_MAP.containsKey( memberId ) ) {
            return ConstantsWallet.MEMBER_SSEEMITTER_MAP.get( memberId );
        }
        SseEmitter sseEmitter = new SseEmitter( 55000L );
        sseEmitter.onCompletion( completionCallBack( memberId ) );
        sseEmitter.onTimeout( timeoutCallBack( memberId ) );
        sseEmitter.onError( errorCallBack( memberId ) );
        ConstantsWallet.MEMBER_SSEEMITTER_MAP.put( memberId, sseEmitter );

        sseSendMessageUtils.sendMessage( memberId, JsonUtil.object2Json( ImmutableMap.of( "msg", "Connection Success" ) ),
                StreamMessageType.CONNECTION );
        return sseEmitter;
    }

    private Runnable completionCallBack( String memberId ) {
        return () -> {
            // log.info( "用户:{} 连接结束", memberId );
            this.removeMemberSseEmitter( memberId );
        };
    }

    private Runnable timeoutCallBack( String memberId ) {
        return () -> {
            // log.info( "用户:{} 连接超时", memberId );
            this.removeMemberSseEmitter( memberId );
        };
    }

    private Consumer<Throwable> errorCallBack( String memberId ) {
        return throwable -> {
            log.error( "用户:{} 连接异常:{}", memberId, throwable.getMessage() );
            this.removeMemberSseEmitter( memberId );
        };
    }

    private void removeMemberSseEmitter( String memberId ) {
        SseEmitter sseEmitter = ConstantsWallet.MEMBER_SSEEMITTER_MAP.remove( memberId );
        if ( sseEmitter != null ) {
            sseEmitter.complete();
        }
    }

    @ExceptionHandler
    @ResponseBody
    public String handleAsyncRequestTimeoutException( AsyncRequestTimeoutException e ) {
        // log.warn( "timeout error is occurred." );
        return SseEmitter
                .event()
                .name( StreamMessageType.MEMBER.name() )
                .data( JsonUtil.object2Json( ImmutableMap.of( "msg", "timeout" ) ), MediaType.APPLICATION_JSON )
                .build()
                .stream()
                .map( d -> d.getData().toString() )
                .collect( Collectors.joining() );
    }
}
