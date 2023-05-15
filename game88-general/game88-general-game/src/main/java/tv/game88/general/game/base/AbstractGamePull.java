package tv.game88.general.game.base;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.client.RestTemplate;
import tv.game88.common.utils.RedisUtils;

import javax.annotation.Resource;

@Log4j2
public abstract class AbstractGamePull implements BaseGamePull {
    @Resource
    protected RestTemplate restTemplate;
    @Resource
    protected RedisUtils   redisUtils;
}
