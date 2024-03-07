package tv.game88.wallet.app.see;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tv.game88.common.utils.JsonUtil;
import tv.game88.wallet.api.constants.ConstantsWallet;
import tv.game88.wallet.api.type.StreamMessageType;
import tv.game88.wallet.app.utils.SseSendMessageUtils;

import java.util.function.Consumer;

@Log4j2
@Service
@RequiredArgsConstructor
public class SseStreamService {
    public SseEmitter createEmitter( String memberId ) {
        SseEmitter sseEmitter = new SseEmitter( 60000L );
        sseEmitter.onCompletion( completionCallBack( memberId ) );
        sseEmitter.onTimeout( timeoutCallBack( memberId ) );
        sseEmitter.onError( errorCallBack( memberId ) );
        if ( ConstantsWallet.MEMBER_SSEEMITTER_MAP.containsKey( memberId ) ) {
            ConstantsWallet.MEMBER_SSEEMITTER_MAP.get( memberId ).complete();
        }
        ConstantsWallet.MEMBER_SSEEMITTER_MAP.put( memberId, sseEmitter );

        log.info( "用户:{} 开始连接", memberId );

        SseSendMessageUtils.me.sendMessage( sseEmitter, memberId, JsonUtil.object2Json( ImmutableMap.of( "msg",
                "Connection " + "Success" ) ), StreamMessageType.CONNECTION );
        return sseEmitter;
    }

    private Runnable completionCallBack( String memberId ) {
        return () -> {
            log.info( "用户:{} 连接结束", memberId );
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
        if ( ConstantsWallet.MEMBER_SSEEMITTER_MAP.containsKey( memberId ) ) {
            ConstantsWallet.MEMBER_SSEEMITTER_MAP.get( memberId ).complete();
        }
        ConstantsWallet.MEMBER_SSEEMITTER_MAP.remove( memberId );
    }
}
