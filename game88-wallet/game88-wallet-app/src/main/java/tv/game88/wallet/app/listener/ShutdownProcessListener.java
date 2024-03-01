package tv.game88.wallet.app.listener;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tv.game88.wallet.api.constants.ConstantsWallet;

/**
 * @author meng.jun
 */
@Component
@Log4j2
public class ShutdownProcessListener implements ApplicationListener<ContextClosedEvent> {

    @Override
    public void onApplicationEvent( ContextClosedEvent event ) {
        log.warn( "服务关闭或重启, 准备注销所有sse会话" );
        ConstantsWallet.MEMBER_SSEEMITTER_MAP.values().parallelStream().forEach( SseEmitter::complete );
        log.warn( "服务关闭或重启, 已注销所有sse会话" );
    }
}
