package tv.game88.wallet.app.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import tv.game88.wallet.api.constants.ConstantsWallet;
import tv.game88.wallet.app.listener.SubRedisMessageListener;

import java.util.Arrays;

/**
 * @author meng.jun
 */
@Log4j2
@Configuration
public class RedisSubConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer( RedisConnectionFactory redisConnectionFactory,
                                                                        SubRedisMessageListener subRedisMessageListener ) {
        RedisMessageListenerContainer listenerContainer = new RedisMessageListenerContainer();
        listenerContainer.setConnectionFactory( redisConnectionFactory );
        listenerContainer.addMessageListener( subRedisMessageListener,
                Arrays.asList( new PatternTopic( ConstantsWallet.MESSAGE_CHANNEL + "*" ),
                        new PatternTopic( ConstantsWallet.MESSAGE_SSEEMITTER_REMOVE_CHANNEL + "*" ) ) );
        return listenerContainer;
    }
}
