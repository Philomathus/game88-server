package tv.game88.game.api.base;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.client.RestTemplate;
import tv.game88.common.utils.RedisUtils;

import jakarta.annotation.Resource;

@Log4j2
public abstract class AbstractGameDock implements BaseGameDock {
    @Resource
    protected RestTemplate restTemplate;
    @Resource
    protected RedisUtils   redisUtils;

    protected void sleep( int sec ) {
        try {
            Thread.sleep( sec * 1000L );
        } catch ( InterruptedException e ) {
            log.error( e.getMessage(), e );
        }
    }
}
