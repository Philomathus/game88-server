package tv.game88.wallet.app.listener;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import tv.game88.wallet.api.constants.ConstantsWallet;

import javax.annotation.Resource;

@Component
@Log4j2
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {
    @Resource
    private RedisProperties redisProperties;

    public RedisKeyExpirationListener( RedisMessageListenerContainer listenerContainer ) {
        super( listenerContainer );
    }

    @Override
    protected void doRegister( RedisMessageListenerContainer listenerContainer ) {
        listenerContainer.addMessageListener( this, new PatternTopic(
                "__keyevent@" + redisProperties.getDatabase() + "__:expired" ) );
    }

    /**
     * 监听过期消息
     *
     * @param message key
     * @param pattern 消息事件
     */
    @Override
    public void onMessage( Message message, byte[] pattern ) {
        String expiredKey = message.toString();
        log.info( "过期消息KEY ::: {}", expiredKey );
        if ( expiredKey.startsWith( ConstantsWallet.BUYER_CONFIRM_BUY_ORDER ) ) {

        }
        if ( expiredKey.startsWith( ConstantsWallet.SELLER_CONFIRM_TRANS_ORDER ) ) {

        }
        if ( expiredKey.startsWith( ConstantsWallet.BUYER_CONFIRM_TRANSFER_ORDER ) ) {

        }
    }
}